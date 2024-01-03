package com.copytrading.service;

import com.copytrading.leaderboard.copytrading.model.response.positions.active.PositionData;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


public class MongoDBService {

    public static List<Document> getActiveOrders() {
        List<Document> orders = new ArrayList<>();
        try (MongoClient mongo = new MongoClient( "localhost" , 27017 )) {
            MongoDatabase database = mongo.getDatabase("CopyTrading");
            MongoCollection<Document> collection = database.getCollection("Orders");
            FindIterable<Document> iterDoc = collection.find();

            for (Document document : iterDoc) {
                orders.add(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } return orders;
    }

    public static void saveOrder() {
        try (MongoClient mongo = new MongoClient( "localhost" , 27017 )) {
            MongoDatabase database = mongo.getDatabase("CopyTrading");
            MongoCollection<Document> collection = database.getCollection("Orders");

            Document document = new Document("title", "MongoDB")
                    .append("id", 1)
                    .append("symbol", "BTCUSDT")
                    .append("likes", 100)
                    .append("url", "http://www.tutorialspoint.com/mongodb/")
                    .append("by", "tutorials point");
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
