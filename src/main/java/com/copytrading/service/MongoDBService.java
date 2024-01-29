package com.copytrading.service;

import com.copytrading.copytradingleaderboard.model.response.positions.active.PositionData;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service for working with mongodb.
 * Is supposed to store trader's ids, divided balance and other information when application starts,
 * so it can take and use info from where it stopped.
 */
public class MongoDBService {
    private static final String tradingInfoCollection = "TradingInfo";

    public static List<Document> getActiveOrders() {
        List<Document> orders = new ArrayList<>();
        try (MongoClient mongo = new MongoClient( "localhost" , 27017 )) {
            MongoDatabase database = mongo.getDatabase("CopyTrading");
            MongoCollection<Document> collection = database.getCollection(tradingInfoCollection);
            FindIterable<Document> iterDoc = collection.find();

            for (Document document : iterDoc) {
                orders.add(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } return orders;
    }

    public static void saveOrder(JSONObject jsonObject) {
        try (MongoClient mongo = new MongoClient( "localhost" , 27017 )) {
            MongoDatabase database = mongo.getDatabase("CopyTrading");
            MongoCollection<Document> collection = database.getCollection(tradingInfoCollection);

            Document document = new Document();

            Iterator<String> keys = jsonObject.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                if (jsonObject.get(key) instanceof JSONObject) {
                    document.append(key, jsonObject.get(key));
                }
            }

           /* Document document = new Document("title", "MongoDB")
                    .append("id", 1)
                    .append("symbol", "BTCUSDT")
                    .append("likes", 100)
                    .append("url", "http://www.tutorialspoint.com/mongodb/")
                    .append("by", "tutorials point");*/
            collection.insertOne(document);
            System.out.println("Document inserted successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteOrder() {

    }

    public List<Document> getOrdersHistory() {
        return null;
    }

    public PositionData convert(Document document) {
        return null;
    }

}
