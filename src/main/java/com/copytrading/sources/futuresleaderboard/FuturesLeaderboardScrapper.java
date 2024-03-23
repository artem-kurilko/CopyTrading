package com.copytrading.sources.futuresleaderboard;

import com.copytrading.sources.futuresleaderboard.model.response.leaderboard.FuturesLeaderboard;
import com.copytrading.sources.futuresleaderboard.model.response.leaderboard.Leader;
import com.copytrading.sources.futuresleaderboard.model.response.position.Position;
import com.copytrading.sources.futuresleaderboard.model.response.position.TraderPositions;
import com.copytrading.sources.futuresleaderboard.model.response.trader.TraderBaseInfo;
import com.copytrading.sources.futuresleaderboard.model.response.trader.TraderInfo;
import com.copytrading.sources.futuresleaderboard.model.response.trader.performance.PerformanceDto;
import com.copytrading.sources.futuresleaderboard.model.response.trader.performance.TraderPerformance;
import com.copytrading.sources.futuresleaderboard.model.response.trader.performance.TraderPerformanceResponse;
import com.copytrading.sources.futuresleaderboard.model.request.*;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class FuturesLeaderboardScrapper {
    private static final FuturesLeaderboardAPI client = getFuturesLeaderboardClient();

    public static List<Leader> futuresLeaderboard(PeriodType period, StatisticsType type, int limit) throws IOException {
        List<Leader> leadersList = new LinkedList<>();
        FuturesLeaderboard leaderboard = getFuturesLeaderboard(period, type);
        for (Leader leader : leaderboard.getData()) {
            if (limit == 0) {
                break;
            }
            leadersList.add(leader);
            limit--;
        }
        return leadersList;
    }

    @SneakyThrows
    public static List<Position> getTopTradersPositions(int limit) {
        List<Leader> leadersList = new LinkedList<>();
        FuturesLeaderboard leaderboard = getFuturesLeaderboard(PeriodType.MONTHLY, StatisticsType.PNL);
        for (Leader leader : leaderboard.getData()) {
            if (limit == 0) {
                break;
            }
            if (isPositionShred(leader.getEncryptedUid())) {
                leadersList.add(leader);
                limit--;
            }
        }
        return leadersList.stream().map(leader -> {
            try {
                return getTraderPositions(leader.getEncryptedUid()).getData().getOtherPositionRetList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).flatMap(Collection::stream).toList();
    }

    /**
     * Returns futures leaderboard with filters {@link #isLeadTraderValid(String)}
     * @param period period to filter by
     * @param type type of statistics to filter by
     * @param limit amount of lead traders to return
     * @return list of {@link Leader} instances
     * @throws IOException if exception thrown
     */
    public static List<Leader> validFuturesLeaderboard(PeriodType period, StatisticsType type, int limit) throws IOException {
        List<Leader> leadersList = new LinkedList<>();
        FuturesLeaderboard leaderboard = getFuturesLeaderboard(period, type);
        for (Leader leader : leaderboard.getData()) {
            if (limit == 0) {
                break;
            }
            if (isLeadTraderValid(leader.getEncryptedUid())) {
                leadersList.add(leader);
                limit--;
            }
        }
        return leadersList;
    }

    @SneakyThrows
    public static List<String> getInvalidTradersWithClosedPositions(int num) {
        List<String> res = new LinkedList<>();
        LeaderboardParams param = LeaderboardParams.builder()
                .isShared(false)
                .periodType(PeriodType.MONTHLY)
                .statisticsType(StatisticsType.PNL)
                .build();
        Call<FuturesLeaderboard> response = client.futuresLeaderboard(param);
        FuturesLeaderboard leaderboard = response.execute().body();
        for (Leader leader : leaderboard.getData()) {
            if (num == 0) {
                break;
            }
            if (!isPositionShred(leader.getEncryptedUid())) {
                res.add(leader.getEncryptedUid());
                num--;
            }
        } return res;
    }

    private static FuturesLeaderboard getFuturesLeaderboard(PeriodType period, StatisticsType type) throws IOException {
        LeaderboardParams param = LeaderboardParams.builder()
                .isShared(true)
                .periodType(period)
                .statisticsType(type)
                .build();
        Call<FuturesLeaderboard> response = client.futuresLeaderboard(param);
        return response.execute().body();
    }

    /**
     * Get next top trader by params, excluding the specified ones.
     * @param ids lead trader ids
     * @return lead trader id
     */
    public static String getNextTopTrader(List<String> ids, PeriodType period, StatisticsType type) throws IOException {
        FuturesLeaderboard leaderboard = getFuturesLeaderboard(period, type);
        for (Leader leader : leaderboard.getData()) {
            String encryptedUuid = leader.getEncryptedUid();
            if (!ids.contains(encryptedUuid)) {
                if (isLeadTraderValid(encryptedUuid)) {
                    return encryptedUuid;
                }
            }
        }
        throw new IllegalArgumentException("Exception getNextTopTrader, trader not found.");
    }

    /**
     * Checks that lead trader:
     * - had positions last month
     * - month roi and pnl > 0
     * - total roi > 0 & total pnl >= 400000
     * - copy traders >= 100
     * - position shared = true
     * @return boolean value
     */
    public static boolean isLeadTraderValid(String id) throws IOException {
        TraderPerformance performanceResponse = getTraderPerformance(id).getData();
        if (performanceResponse == null) {
            return false;
        }

        List<PerformanceDto> performance = performanceResponse.getPerformanceRetList();
        for (PerformanceDto perf : performance) {
            if (perf.getPeriodType() == null)
                continue;
            if (perf.getPeriodType().equals(PeriodType.MONTHLY)) {
                if (perf.getValue() <= 0) {
                    return false;
                }
            }
            if (perf.getPeriodType().equals(PeriodType.ALL)) {
                if (perf.getStatisticsType().equals(StatisticsType.PNL) && perf.getValue() < 400000) {
                    return false;
                }
                if (perf.getStatisticsType().equals(StatisticsType.ROI) && perf.getValue() <= 0) {
                    return false;
                }
            }
        }
        return isPositionShred(id);
    }

    public static boolean isPositionShred(String id) throws IOException {
        TraderBaseInfo traderInfo = getTradersBaseInfo(id).getData();
        return traderInfo.isPositionShared();
    }

    public static TraderInfo getTradersBaseInfo(String encryptedUid) throws IOException {
        Call<TraderInfo> response = client.tradersBaseInfo(new TraderId(encryptedUid));
        return response.execute().body();
    }

    public static TraderPerformanceResponse getTraderPerformance(String encryptedUid) throws IOException {
        Call<TraderPerformanceResponse> response = client.traderPerformance(new TradePerformanceParams(encryptedUid));
        return response.execute().body();
    }

    /**
     * Returns trader's active positions
     * @param encryptedUid trader id
     * @return trader's positions
     * @throws IOException if exception occurs
     */
    public static TraderPositions getTraderPositions(String encryptedUid) throws IOException {
        URL url = new URL("https://www.binance.com/bapi/futures/v2/private/future/leaderboard/getOtherPosition");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0");
        httpConn.setRequestProperty("Accept", "*/*");
        httpConn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        httpConn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        httpConn.setRequestProperty("Referer", "https://www.binance.com/en/futures-activity/leaderboard/user/um?encryptedUid=ACD6F840DE4A5C87C77FB7A49892BB35");
        httpConn.setRequestProperty("lang", "en");
        httpConn.setRequestProperty("x-ui-request-trace", "03eb95ff-bbb6-465d-89c4-72afd6fab9f7");
        httpConn.setRequestProperty("x-trace-id", "03eb95ff-bbb6-465d-89c4-72afd6fab9f7");
        httpConn.setRequestProperty("bnc-uuid", "5479519b-922b-4027-b382-c1c1e9651633");
        httpConn.setRequestProperty("content-type", "application/json");
        httpConn.setRequestProperty("device-info", "eyJzY3JlZW5fcmVzb2x1dGlvbiI6IjgwMCwxMjgwIiwiYXZhaWxhYmxlX3NjcmVlbl9yZXNvbHV0aW9uIjoiNzUyLDEyODAiLCJzeXN0ZW1fdmVyc2lvbiI6IldpbmRvd3MgMTAiLCJicmFuZF9tb2RlbCI6InVua25vd24iLCJzeXN0ZW1fbGFuZyI6ImVuLVVTIiwidGltZXpvbmUiOiJHTVQrMDI6MDAiLCJ0aW1lem9uZU9mZnNldCI6LTEyMCwidXNlcl9hZ2VudCI6Ik1vemlsbGEvNS4wIChXaW5kb3dzIE5UIDEwLjA7IFdpbjY0OyB4NjQ7IHJ2OjEyMy4wKSBHZWNrby8yMDEwMDEwMSBGaXJlZm94LzEyMy4wIiwibGlzdF9wbHVnaW4iOiJQREYgVmlld2VyLENocm9tZSBQREYgVmlld2VyLENocm9taXVtIFBERiBWaWV3ZXIsTWljcm9zb2Z0IEVkZ2UgUERGIFZpZXdlcixXZWJLaXQgYnVpbHQtaW4gUERGIiwiY2FudmFzX2NvZGUiOiJkYjQ5NzZiOCIsIndlYmdsX3ZlbmRvciI6Ikdvb2dsZSBJbmMuIChJbnRlbCkiLCJ3ZWJnbF9yZW5kZXJlciI6IkFOR0xFIChJbnRlbCwgSW50ZWwoUikgSEQgR3JhcGhpY3MgRGlyZWN0M0QxMSB2c181XzAgcHNfNV8wKSwgb3Igc2ltaWxhciIsImF1ZGlvIjoiMzUuNzQ5OTY4MjIzMjczNzU0IiwicGxhdGZvcm0iOiJXaW4zMiIsIndlYl90aW1lem9uZSI6IkV1cm9wZS9LeWl2IiwiZGV2aWNlX25hbWUiOiJGaXJlZm94IFYxMjMuMCAoV2luZG93cykiLCJmaW5nZXJwcmludCI6IjEzZTZjMTk1MDFjMDEzNWY4NWE1N2NmZmMxZTA2NjIwIiwiZGV2aWNlX2lkIjoiIiwicmVsYXRlZF9kZXZpY2VfaWRzIjoiIn0=");
        httpConn.setRequestProperty("clienttype", "web");
        httpConn.setRequestProperty("fvideo-id", "3365a86b00d7a827a9abdf1ca915103153252fdf");
        httpConn.setRequestProperty("fvideo-token", "k4ckW80HJn2PZUOyJ9zy5IgjmmFV0oxFn0OMGbWzDE7vhcoTWxATR2QKN/gXwMHx4mLH+1CCJIkG4NZXK+QITKbcYceaI7brdWG8vdRIv8cKANnFjpV0AXqEuYXLx/HJUYQE/9+FBPfXWKF1Hc6bNZv6C0mHNftA0utRsoUFe9oHqmS1UfTNSqDJ2aAKSPAsU=49");
        httpConn.setRequestProperty("csrftoken", "ee9d1bafddea9fa00a2f685ac919813a");
        httpConn.setRequestProperty("Origin", "https://www.binance.com");
        httpConn.setRequestProperty("DNT", "1");
        httpConn.setRequestProperty("Connection", "keep-alive");
        httpConn.setRequestProperty("Cookie", "bnc-uuid=5479519b-922b-4027-b382-c1c1e9651633; source=CRM; campaign=www.google.com; __BNC_USER_DEVICE_ID__={\"c3508c5ce64b615279eb64b805ec2d23\":{\"date\":1711136628440,\"value\":\"\"}}; OptanonConsent=isGpcEnabled=0&datestamp=Fri+Mar+22+2024+21%3A59%3A14+GMT%2B0200+(Eastern+European+Standard+Time)&version=202402.1.0&isIABGlobal=false&hosts=&consentId=0d40cfb8-607b-4d83-836f-7acdc5cdc169&interactionCount=2&landingPath=NotLandingPage&groups=C0001%3A1%2CC0003%3A0%2CC0004%3A0%2CC0002%3A0&AwaitingReconsent=true&browserGpcFlag=0&geolocation=&isAnonUser=1; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%2239079923%22%2C%22first_id%22%3A%22189c0e04e615c8-0befeaa62bf74a8-d505429-1024000-189c0e04e62849%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E7%9B%B4%E6%8E%A5%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC_%E7%9B%B4%E6%8E%A5%E6%89%93%E5%BC%80%22%2C%22%24latest_referrer%22%3A%22%22%2C%22%24latest_utm_source%22%3A%22CRM%22%2C%22%24latest_utm_medium%22%3A%22Email%22%7D%2C%22identities%22%3A%22eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMTg5YzBlMDRlNjE1YzgtMGJlZmVhYTYyYmY3NGE4LWQ1MDU0MjktMTAyNDAwMC0xODljMGUwNGU2Mjg0OSIsIiRpZGVudGl0eV9sb2dpbl9pZCI6IjM5MDc5OTIzIn0%3D%22%2C%22history_login_id%22%3A%7B%22name%22%3A%22%24identity_login_id%22%2C%22value%22%3A%2239079923%22%7D%2C%22%24device_id%22%3A%22189c0e04e615c8-0befeaa62bf74a8-d505429-1024000-189c0e04e62849%22%7D; userPreferredCurrency=USD_USD; BNC_FV_KEY=3365a86b00d7a827a9abdf1ca915103153252fdf; BNC_FV_KEY_EXPIRE=1711158239797; changeBasisTimeZone=; fiat-prefer-currency=UAH; camp-key=; pl-id=39079923; BNC_FV_KEY_T=101-MaFSFJpTmVf3nUmU7veAo8%2FzOyxF8QS%2BHeCzIRqaE3RsgNJv0cyZzouajDuxjNVwoB%2F4CEpqXko2wCxZtyklEA%3D%3D-0JzUag6od50VqXogzFFuCQ%3D%3D-67; se_gd=hYMVABBxXGRFgJVhVGg9gZZUwVgkFBUUVML5YUE91RcUAWlNXW9W1; se_gsd=WjQkCjhhIzAnIxo7NCUhCjooFg8NBQsIU1RAUF1SWlRaAlNS1; g_state={\"i_l\":4,\"i_p\":1713044069556}; BNC-Location=BINANCE; OptanonAlertBoxClosed=2023-10-13T14:42:36.915Z; _ga=GA1.2.1353433018.1705945987; language=en; futures-layout=pro; se_sd=BdVEhXwoLGWVwIIgHBgAgZZEwBlMFEUUVMG5YUE91RcUABlNXW0A1; _h_desk_key=5b2ea216a8a946b8a5bb52325095eed5; p20t=web.39079923.60C3CBA4F00A99381D3E66093FC71BB7; s9r1=E9576639C34A8F9932E343EB9E763301; d1og=web.39079923.8C64F76DA166743F5EFF1BD7C2531852; r2o1=web.39079923.05C5B0C17869AC1283C00F0BA3531F9B; f30l=web.39079923.902D953FFE030A213616DC8D73C07B74; cr00=F1E772CCE45B33A3599BED71120EF304; logined=y; lang=en; theme=dark");
        httpConn.setRequestProperty("Sec-Fetch-Dest", "empty");
        httpConn.setRequestProperty("Sec-Fetch-Mode", "cors");
        httpConn.setRequestProperty("Sec-Fetch-Site", "same-origin");
        httpConn.setRequestProperty("TE", "trailers");

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        writer.write("{\"encryptedUid\":\"" + encryptedUid + "\",\"tradeType\":\"PERPETUAL\"}");
        writer.flush();
        writer.close();
        httpConn.getOutputStream().close();

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        if ("gzip".equals(httpConn.getContentEncoding())) {
            responseStream = new GZIPInputStream(responseStream);
        }
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        Gson gson = new Gson();
        TraderPositions positions = gson.fromJson(response, TraderPositions.class);
        List<Position> positionsList =
                positions.getData().getOtherPositionRetList().stream().peek(pos -> pos.setTraderId(encryptedUid)).toList();
        positions.getData().setOtherPositionRetList(positionsList);
        return positions;
    }

    /**
     * Unfortunately this method is @deprecated
     * As updating date doesn't work, cookies still invalid
     * @param cookies string value
     * @return updated cookies
     */
    @Deprecated
    private static String updateCookies(String cookies) {
        final Date date = new Date();
        final long currentTimeMillis = date.getTime();
        String target;

        // update date key
        target = "\"date\":";
        String[] dateString = cookies.split(target);
        String res = dateString[1].substring(13);
        cookies = dateString[0] + target + currentTimeMillis + res;

        // update datestamp key
        target = "datestamp=";
        String[] datestampString = cookies.split(target);
        String[] dateValues = date.toString().split(" ");
        String month = dateValues[1];
        String dayOfTheWeek = dateValues[0];
        String day = dateValues[2];
        String[] time = dateValues[3].split(":");
        String hours = time[0];
        String minutes = time[1];
        String seconds = time[2];
        String year = dateValues[5];
        String updatedDatestamp = dayOfTheWeek + "+" + month + "+" + day + "+" + year + "+" + hours + "%3A" + minutes + "%3A" + seconds;
        cookies = datestampString[0] + target + updatedDatestamp + datestampString[1];

        // update BNC_FV_KEY_EXPIRE key
        target = "BNC_FV_KEY_EXPIRE=";
        String[] expireTimeValues = cookies.split(target);
        int amountOfHours = 17;
        int amountOfMinutes = 40;
        long updatedExpireTime = currentTimeMillis + amountOfHours * 60 * 60 * 1000 + amountOfMinutes * 60 * 1000;
        cookies = expireTimeValues[0] + target + updatedExpireTime + expireTimeValues[1];
        return cookies;
    }

    public static String getLink(String id) {
        return "https://www.binance.com/en/futures-activity/leaderboard/user/um?encryptedUid=" + id;
    }

    private static FuturesLeaderboardAPI getFuturesLeaderboardClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient
                .Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.binance.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(FuturesLeaderboardAPI.class);
    }

}
