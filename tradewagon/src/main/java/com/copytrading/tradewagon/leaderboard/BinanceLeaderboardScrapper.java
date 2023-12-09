package com.copytrading.tradewagon.leaderboard;

import com.copytrading.tradewagon.leaderboard.model.Position;
import com.copytrading.tradewagon.leaderboard.rest.DefaultClient;
import com.copytrading.tradewagon.leaderboard.model.Leader;
import com.copytrading.tradewagon.leaderboard.model.PeriodType;
import com.copytrading.tradewagon.leaderboard.model.StatisticsType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

//TODO: look trade type perpetual, options and what are the difference
public class BinanceLeaderboardScrapper {
    private static final DefaultClient client = new DefaultClient();
    private static final Logger logger = LogManager.getLogger(BinanceLeaderboardScrapper.class);

    public static void main(String[] args) {
        PeriodType period = PeriodType.MONTHLY;
        StatisticsType type = StatisticsType.FOLLOWERS;
        int limit = 10;
        List<Leader> leaders = getLeaderBoard(period, type, limit);
        for (Leader lead : leaders) {
            System.out.println(lead);
        }
    }

    public static List<Leader> getLeaderBoard(PeriodType period, StatisticsType type, int limit) {
        String requestUrl = "https://www.binance.com/bapi/futures/v2/public/future/leaderboard/getLeaderboardRank";
        String postString = "{\"isShared\":true,\"periodType\":\"" + period + "\",\"statisticsType\":\"" + type
                + "\",\"tradeType\":\"PERPETUAL\"}";
        try {
            String data = client.doPost(requestUrl, postString, getHeaders()).getResponse();
            data = data.substring(data.indexOf("[{"));
            data = data.substring(0, data.indexOf("}],\"") + 2);
            ObjectMapper mapper = new ObjectMapper();
            List<Leader> leaders = mapper.readValue(data,
                    mapper.getTypeFactory().constructCollectionType(CopyOnWriteArrayList.class, Leader.class));
            return leaders.subList(0, limit);
        } catch (Exception e) {
            logger.error("Error when trying get leaders ->" + e.getMessage());
            return new ArrayList<>();
        }
    }

    //FIXME: doesn't work
    public static List<Position> getLeadersPositions(String leaderId, AtomicInteger index) throws Exception {
        if (index.get() > 100)
            index.set(0);
        int urlIndex = index.getAndIncrement();
//        String url = "https://app-server-binance-" + urlIndex
//                + ".herokuapp.com/?command=leader-board-positions&leaderId=" + leaderId;

        String url = "https://www.binance.com/en/futures-activity/leaderboard/user/um?encryptedUid=" + leaderId;

        String data = client.doGet(url, getHeaders()).getResponse();
        if (data.contains("otherPositionRetList")) {
            ObjectMapper mapper = new ObjectMapper();
            List<Position> positions = mapper.readValue(
                    mapper.readTree(data).get("data").get("otherPositionRetList").toString(),
                    mapper.getTypeFactory().constructCollectionType(CopyOnWriteArrayList.class, Position.class));
            return positions == null ? new ArrayList<>()
                    : positions.parallelStream().peek(position -> position.setLeader(leaderId))
                    .peek(position -> position.setId(position.hashCode())).collect(Collectors.toList());
        } else {
            String message = "Error in url " + url + " with response \n" + data;
            logger.error(message);
            return getLeadersPositions(leaderId, index);
        }
    }

    private static Map<String, String> getHeaders() {
        Map<String, String> headers = new Hashtable<>();
        headers.put("Accept", "*/*");
        headers.put("Accept-Language", "en-GB,en;q=0.9,en-US;q=0.8,fr;q=0.7,ar;q=0.6,es;q=0.5,de;q=0.4");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Connection", "keep-alive");
        headers.put("DNT", "1");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-site");
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0");
        return headers;
    }
}
