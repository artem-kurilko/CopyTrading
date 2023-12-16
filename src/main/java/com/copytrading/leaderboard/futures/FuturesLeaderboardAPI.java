package com.copytrading.leaderboard.futures;

import com.copytrading.leaderboard.futures.model.LeaderboardParams;
import com.copytrading.leaderboard.futures.model.TraderPositionsParams;
import com.copytrading.leaderboard.futures.model.response.FuturesLeaderboard;
import com.copytrading.leaderboard.futures.model.response.TraderPositions;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.util.Map;

public interface FuturesLeaderboardAPI {

    @POST("/bapi/futures/v2/public/future/leaderboard/getLeaderboardRank")
    Call<FuturesLeaderboard> futuresLeaderboard(@Body LeaderboardParams params);

    @Headers({
            "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0",
            "Content-Type: application/json",
            "device-info: eyJzY3JlZW5fcmVzb2x1dGlvbiI6IjgwMCwxMjgwIiwiYXZhaWxhYmxlX3NjcmVlbl9yZXNvbHV0aW9uIjoiNzUyLDEyODAiLCJzeXN0ZW1fdmVyc2lvbiI6IldpbmRvd3MgMTAiLCJicmFuZF9tb2RlbCI6InVua25vd24iLCJzeXN0ZW1fbGFuZyI6ImVuLVVTIiwidGltZXpvbmUiOiJHTVQrMDI6MDAiLCJ0aW1lem9uZU9mZnNldCI6LTEyMCwidXNlcl9hZ2VudCI6Ik1vemlsbGEvNS4wIChXaW5kb3dzIE5UIDEwLjA7IFdpbjY0OyB4NjQ7IHJ2OjEyMC4wKSBHZWNrby8yMDEwMDEwMSBGaXJlZm94LzEyMC4wIiwibGlzdF9wbHVnaW4iOiJQREYgVmlld2VyLENocm9tZSBQREYgVmlld2VyLENocm9taXVtIFBERiBWaWV3ZXIsTWljcm9zb2Z0IEVkZ2UgUERGIFZpZXdlcixXZWJLaXQgYnVpbHQtaW4gUERGIiwiY2FudmFzX2NvZGUiOiJkYjQ5NzZiOCIsIndlYmdsX3ZlbmRvciI6Ikdvb2dsZSBJbmMuIChJbnRlbCkiLCJ3ZWJnbF9yZW5kZXJlciI6IkFOR0xFIChJbnRlbCwgSW50ZWwoUikgSEQgR3JhcGhpY3MgRGlyZWN0M0QxMSB2c181XzAgcHNfNV8wKSIsImF1ZGlvIjoiMzUuNzQ5OTY4MjIzMjczNzU0IiwicGxhdGZvcm0iOiJXaW4zMiIsIndlYl90aW1lem9uZSI6IkV1cm9wZS9LeWl2IiwiZGV2aWNlX25hbWUiOiJGaXJlZm94IFYxMjAuMCAoV2luZG93cykiLCJmaW5nZXJwcmludCI6IjJkZTA1NTJkZDI1NDFhZDM4YWU5MDdkOTBjNGY2MzdmIiwiZGV2aWNlX2lkIjoiIiwicmVsYXRlZF9kZXZpY2VfaWRzIjoiIn0=",
            "Cookie: bnc-uuid=5479519b-922b-4027-b382-c1c1e9651633; source=web; campaign=www.google.com; __BNC_USER_DEVICE_ID__={\"c3508c5ce64b615279eb64b805ec2d23\":{\"date\":1702575395413,\"value\":\"\"}}; OptanonConsent=isGpcEnabled=0&datestamp=Sat+Dec+16+2023+22%3A12%3A37+GMT%2B0200+(Eastern+European+Standard+Time)&version=202303.2.0&isIABGlobal=false&hosts=&consentId=0d40cfb8-607b-4d83-836f-7acdc5cdc169&interactionCount=2&landingPath=NotLandingPage&groups=C0001%3A1%2CC0003%3A0%2CC0004%3A0%2CC0002%3A0&AwaitingReconsent=false&browserGpcFlag=0&geolocation=UA%3B12; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%2239079923%22%2C%22first_id%22%3A%22189c0e04e615c8-0befeaa62bf74a8-d505429-1024000-189c0e04e62849%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E7%9B%B4%E6%8E%A5%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC_%E7%9B%B4%E6%8E%A5%E6%89%93%E5%BC%80%22%2C%22%24latest_referrer%22%3A%22%22%2C%22%24latest_utm_source%22%3A%22web%22%2C%22%24latest_utm_medium%22%3A%22dropdown%22%7D%2C%22identities%22%3A%22eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMTg5YzBlMDRlNjE1YzgtMGJlZmVhYTYyYmY3NGE4LWQ1MDU0MjktMTAyNDAwMC0xODljMGUwNGU2Mjg0OSIsIiRpZGVudGl0eV9sb2dpbl9pZCI6IjM5MDc5OTIzIn0%3D%22%2C%22history_login_id%22%3A%7B%22name%22%3A%22%24identity_login_id%22%2C%22value%22%3A%2239079923%22%7D%2C%22%24device_id%22%3A%22189c0e04e615c8-0befeaa62bf74a8-d505429-1024000-189c0e04e62849%22%7D; userPreferredCurrency=USD_USD; BNC_FV_KEY=3365a86b00d7a827a9abdf1ca915103153252fdf; BNC_FV_KEY_EXPIRE=1702775839431; changeBasisTimeZone=; fiat-prefer-currency=EUR; camp-key=; pl-id=39079923; BNC_FV_KEY_T=101-BfyOl7Jc%2Fl3GTn%2BwWwuVwHPX8xAFdHPLenK8cL%2ByuLOCDkYtm7i6mPLQZfeFTgDz74mJb2uSCtSrGF5%2BMm16HA%3D%3D-gJO4BNLJGFRm1aM4XOpy%2Bg%3D%3D-be; se_gd=hYMVABBxXGRFgJVhVGg9gZZUwVgkFBUUVML5YUE91RcUAWlNXW9W1; se_gsd=WjQkCjhhIzAnIxo7NCUhCjooFg8NBQsIU1RAUF1SWlRaAlNS1; g_state={\"i_l\":2,\"i_p\":1702416457299}; BNC-Location=BINANCE; OptanonAlertBoxClosed=2023-10-13T14:42:36.915Z; d1og=web.39079923.D240B8CB04BAE9DBEE7A66FA2E270A5F; r2o1=web.39079923.520C4B52EA62C9832176E94A4E822308; f30l=web.39079923.75E32FC41C3C72486D4244D45E385223; cr00=32F610C8195DFD5D99C2E23673D12A38; logined=y; se_sd=BdVEhXwoLGWVwIIgHBgAgZZEwBlMFEUUVMG5YUE91RcUABlNXW0A1; _h_desk_key=f7f5def8b9a04fc89774510ac63666a5; s9r1=003EE36A334079217F3CC4C53030AAE1; p20t=web.39079923.CCD05073941912E5B01CF3E7A38410D6; futures-layout=pro; lang=en; theme=dark"
    })
    @POST("/bapi/futures/v2/private/future/leaderboard/getOtherPosition")
    Call<TraderPositions> tradersPositions(@Body TraderPositionsParams params,
                                           @HeaderMap Map<String, String> headers);
}
