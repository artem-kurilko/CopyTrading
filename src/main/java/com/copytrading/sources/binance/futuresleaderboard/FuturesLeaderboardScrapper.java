package com.copytrading.sources.binance.futuresleaderboard;

import com.copytrading.sources.binance.futuresleaderboard.model.request.*;
import com.copytrading.sources.binance.futuresleaderboard.model.response.leaderboard.FuturesLeaderboard;
import com.copytrading.sources.binance.futuresleaderboard.model.response.leaderboard.Leader;
import com.copytrading.sources.binance.futuresleaderboard.model.response.position.Position;
import com.copytrading.sources.binance.futuresleaderboard.model.response.position.TraderPositions;
import com.copytrading.sources.binance.futuresleaderboard.model.response.trader.TraderBaseInfo;
import com.copytrading.sources.binance.futuresleaderboard.model.response.trader.TraderInfo;
import com.copytrading.sources.binance.futuresleaderboard.model.response.trader.performance.PerformanceDto;
import com.copytrading.sources.binance.futuresleaderboard.model.response.trader.performance.TraderPerformance;
import com.copytrading.sources.binance.futuresleaderboard.model.response.trader.performance.TraderPerformanceResponse;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
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
                if (perf.getValue() == 0 || perf.getValue() < 0) {
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
        httpConn.setRequestProperty("Referer", "https://www.binance.com/en/futures-activity/leaderboard/user/um?encryptedUid=3BAFAFCA68AB85929DF777C316F18C54");
        httpConn.setRequestProperty("lang", "en");
        httpConn.setRequestProperty("x-ui-request-trace", "0fe7e70f-ae64-436c-9156-6c87133fb6e4");
        httpConn.setRequestProperty("x-trace-id", "0fe7e70f-ae64-436c-9156-6c87133fb6e4");
        httpConn.setRequestProperty("bnc-uuid", "5479519b-922b-4027-b382-c1c1e9651633");
        httpConn.setRequestProperty("content-type", "application/json");
        httpConn.setRequestProperty("device-info", "eyJzY3JlZW5fcmVzb2x1dGlvbiI6Ijg4MCwxNDA4IiwiYXZhaWxhYmxlX3NjcmVlbl9yZXNvbHV0aW9uIjoiODI3LDE0MDgiLCJzeXN0ZW1fdmVyc2lvbiI6IldpbmRvd3MgMTAiLCJicmFuZF9tb2RlbCI6InVua25vd24iLCJzeXN0ZW1fbGFuZyI6ImVuLVVTIiwidGltZXpvbmUiOiJHTVQrMDI6MDAiLCJ0aW1lem9uZU9mZnNldCI6LTEyMCwidXNlcl9hZ2VudCI6Ik1vemlsbGEvNS4wIChXaW5kb3dzIE5UIDEwLjA7IFdpbjY0OyB4NjQ7IHJ2OjEyMy4wKSBHZWNrby8yMDEwMDEwMSBGaXJlZm94LzEyMy4wIiwibGlzdF9wbHVnaW4iOiJQREYgVmlld2VyLENocm9tZSBQREYgVmlld2VyLENocm9taXVtIFBERiBWaWV3ZXIsTWljcm9zb2Z0IEVkZ2UgUERGIFZpZXdlcixXZWJLaXQgYnVpbHQtaW4gUERGIiwiY2FudmFzX2NvZGUiOiIxMmZjOTYxMyIsIndlYmdsX3ZlbmRvciI6Ikdvb2dsZSBJbmMuIChJbnRlbCkiLCJ3ZWJnbF9yZW5kZXJlciI6IkFOR0xFIChJbnRlbCwgSW50ZWwoUikgSEQgR3JhcGhpY3MgRGlyZWN0M0QxMSB2c181XzAgcHNfNV8wKSwgb3Igc2ltaWxhciIsImF1ZGlvIjoiMzUuNzQ5OTY4MjIzMjczNzU0IiwicGxhdGZvcm0iOiJXaW4zMiIsIndlYl90aW1lem9uZSI6IkV1cm9wZS9LeWl2IiwiZGV2aWNlX25hbWUiOiJGaXJlZm94IFYxMjMuMCAoV2luZG93cykiLCJmaW5nZXJwcmludCI6Ijg2NDVlNmZmZmI4ZjViZWVmZGY0YzE4ODdhMWMwODIyIiwiZGV2aWNlX2lkIjoiIiwicmVsYXRlZF9kZXZpY2VfaWRzIjoiIn0=");
        httpConn.setRequestProperty("clienttype", "web");
        httpConn.setRequestProperty("fvideo-id", "3365a86b00d7a827a9abdf1ca915103153252fdf");
        httpConn.setRequestProperty("fvideo-token", "fAUyDMGEM+oxM1P7aWYkKzi+HvEYSUaxBNVgclkKlh9A8F8w1RbUp5t0jwwrT417wd/0kCpFiEhI0zy4sSYPvDPxCXd7CdnC76/d1SaxBqc/sCyLc+RUzUWI9Nrrv1hXB3tKMBDCn/43uuVompj9NtNbmGC5jZV8X9OR9Vh8E1+1sG5y+gyUMY94kHOghVDdU=3a");
        httpConn.setRequestProperty("csrftoken", "e88dee4dce7e57d1363e2bb21592a325");
        httpConn.setRequestProperty("Origin", "https://www.binance.com");
        httpConn.setRequestProperty("DNT", "1");
        httpConn.setRequestProperty("Connection", "keep-alive");
        httpConn.setRequestProperty("Cookie", "bnc-uuid=5479519b-922b-4027-b382-c1c1e9651633; source=CRM; campaign=www.google.com; __BNC_USER_DEVICE_ID__={\"c3508c5ce64b615279eb64b805ec2d23\":{\"date\":1709909803885,\"value\":\"\"}}; OptanonConsent=isGpcEnabled=0&datestamp=Fri+Mar+08+2024+20%3A49%3A26+GMT%2B0200+(Eastern+European+Standard+Time)&version=202310.2.0&isIABGlobal=false&hosts=&consentId=0d40cfb8-607b-4d83-836f-7acdc5cdc169&interactionCount=2&landingPath=NotLandingPage&groups=C0001%3A1%2CC0003%3A0%2CC0004%3A0%2CC0002%3A0&AwaitingReconsent=true&browserGpcFlag=0&geolocation=; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%2239079923%22%2C%22first_id%22%3A%22189c0e04e615c8-0befeaa62bf74a8-d505429-1024000-189c0e04e62849%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E7%9B%B4%E6%8E%A5%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC_%E7%9B%B4%E6%8E%A5%E6%89%93%E5%BC%80%22%2C%22%24latest_referrer%22%3A%22%22%2C%22%24latest_utm_source%22%3A%22CRM%22%2C%22%24latest_utm_medium%22%3A%22Email%22%7D%2C%22identities%22%3A%22eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMTg5YzBlMDRlNjE1YzgtMGJlZmVhYTYyYmY3NGE4LWQ1MDU0MjktMTAyNDAwMC0xODljMGUwNGU2Mjg0OSIsIiRpZGVudGl0eV9sb2dpbl9pZCI6IjM5MDc5OTIzIn0%3D%22%2C%22history_login_id%22%3A%7B%22name%22%3A%22%24identity_login_id%22%2C%22value%22%3A%2239079923%22%7D%2C%22%24device_id%22%3A%22189c0e04e615c8-0befeaa62bf74a8-d505429-1024000-189c0e04e62849%22%7D; userPreferredCurrency=USD_USD; BNC_FV_KEY=3365a86b00d7a827a9abdf1ca915103153252fdf; BNC_FV_KEY_EXPIRE=1709931350963; changeBasisTimeZone=; fiat-prefer-currency=UAH; camp-key=; pl-id=39079923; BNC_FV_KEY_T=101-XR5GMEFbB1ouVQVFrSN18RoLT2AvxuWGpCpqzWQIhZ5KDIxKX5evCLZxdGNxzfzsNgvAIq%2Bto%2FJ7ktDSrepwjw%3D%3D-oXowYP%2BFdCtyIClTbdb9AQ%3D%3D-2a; se_gd=hYMVABBxXGRFgJVhVGg9gZZUwVgkFBUUVML5YUE91RcUAWlNXW9W1; se_gsd=WjQkCjhhIzAnIxo7NCUhCjooFg8NBQsIU1RAUF1SWlRaAlNS1; g_state={\"i_l\":4,\"i_p\":1708807731479}; BNC-Location=BINANCE; OptanonAlertBoxClosed=2023-10-13T14:42:36.915Z; _ga=GA1.2.1353433018.1705945987; language=en; futures-layout=pro; se_sd=BdVEhXwoLGWVwIIgHBgAgZZEwBlMFEUUVMG5YUE91RcUABlNXW0A1; _h_desk_key=5b2ea216a8a946b8a5bb52325095eed5; p20t=web.39079923.91E94592427EBACC8694D115B544BA61; d1og=web.39079923.C08A4C00680B39A6E4403AAFD8F9DFD7; r2o1=web.39079923.468AF3CEBFEFCD949016251EC3C3DB0D; f30l=web.39079923.7FA4B2AE38E95237B9CFBE2F1D52BD0E; s9r1=38BBFF21CF25820166F00A693FCE66FF; cr00=6CDD3E2E4BF7FFFFA5BA647CEE757C3F; logined=y; lang=en; theme=dark");
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
