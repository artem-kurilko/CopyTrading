package com.copytrading.service;

import com.copytrading.model.LeadTraderState;
import com.copytrading.model.TradingState;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Service for working with mongodb.
 * Is supposed to store trader's ids, divided balance and other information when application starts,
 * so it can take and use info from where it stopped.
 */
public class MongoDBService {
    private static final String tradingInfoCollection = "TradingInfo";
    private static final String documentKey = "AppState";

    public static TradingState getLastTradingState() {
        TradingState tradingState = new TradingState();
        List<LeadTraderState> leadTraderStateList = new LinkedList<>();
        MongoCollection<Document> collection = getCollection();
        Document document = collection.find().sort(new Document("_id", -1)).first();
        JSONArray tradersStateArray = new JSONObject(document.toJson()).getJSONArray(documentKey);
        for (int i = 0; i < tradersStateArray.length(); i++) {
            JSONObject tradersStateObject = tradersStateArray.getJSONObject(i);
            leadTraderStateList.add(new LeadTraderState(
                    tradersStateObject.getString("traderId"),
                    tradersStateObject.getDouble("balance")
            ));
        }
        tradingState.setLeadTraderStates(leadTraderStateList);
        return tradingState;
    }

    public static void saveApplicationState(TradingState tradingState) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<Document> tradersState = new ArrayList<>();
            tradingState.getLeadTraderStates().forEach(state -> tradersState.add(new Document().append("traderId", state.getTraderId()).append("balance", state.getBalance())));
            Document document = new Document();
            document.append(documentKey, tradersState);
            collection.insertOne(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MongoCollection<Document> getCollection() {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoDatabase database = mongo.getDatabase("CopyTrading");
        return database.getCollection(tradingInfoCollection);
    }

}
