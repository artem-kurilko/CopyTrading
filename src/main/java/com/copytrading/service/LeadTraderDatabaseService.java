package com.copytrading.service;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.copytrading.util.ConfigUtils.getProperty;

/**
 * Simple service class to store lead trader id and ids of orders copied from him.
 */
public class LeadTraderDatabaseService {
    private final boolean isProd;

    public LeadTraderDatabaseService(boolean isProd) {
        this.isProd = isProd;
    }

    public HashMap<String, List<String>> getLeaderIdsAndOrders() {
        HashMap<String, List<String>> tradersIds = new HashMap<>();
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeadTraderCollection(mongo);

        FindIterable<Document> iterDoc = collection.find();
        for (Document document : iterDoc) {
            if (document.getString("id").isEmpty()) {
                continue;
            }
            tradersIds.put(document.getString("id"), document.getList("orders", String.class));
        }
        mongo.close();
        return tradersIds;
    }

    public List<String> getLeftOrders() {
        List<String> leftOrders = new ArrayList<>();
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeftOrdersCollection(mongo);
        FindIterable<Document> iterDoc = collection.find();
        for (Document document : iterDoc) {
            if (document.getString("id").isEmpty()) {
                leftOrders.addAll(document.getList("orders", String.class));
                break;
            }
        }
        mongo.close();
        return leftOrders;
    }

    public void saveLeftOrders(List<String> leftOrders) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeftOrdersCollection(mongo);
        FindIterable<Document> iterDoc = collection.find();
        List<String> oldLeftOrders = new ArrayList<>();
        for (Document document : iterDoc) {
            if (document.getString("id").isEmpty()) {
                oldLeftOrders = document.getList("orders", String.class);
                break;
            }
        }
        collection.deleteMany(new Document());
        Document document = new Document();
        document.append("id", "");
        document.append("orders", oldLeftOrders.addAll(leftOrders));
        collection.insertOne(document);
        mongo.close();
    }

    /**
     * Removes all records and sets the passed ones.
     * @param traderIds hashmap of lead trader id as key, and copied orders as list of orderId
     */
    public void resetLeaderIdsAndOrders(HashMap<String, List<String>> traderIds) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeadTraderCollection(mongo);
        collection.deleteMany(new Document());
        List<Document> documentsList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : traderIds.entrySet()) {
            Document document = new Document();
            document.append("id", entry.getKey());
            document.append("orders", entry.getValue());
            documentsList.add(document);
        }
        collection.insertMany(documentsList);
        mongo.close();
    }

    /**
     * Removes all records and sets the passed ones.
     * @param leftOrders list of orderId, it is copied orders of traders that no longer show their positions
     */
    public void resetLeftOrders(List<String> leftOrders) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeftOrdersCollection(mongo);
        collection.deleteMany(new Document());
        Document document = new Document();
        document.append("id", "");
        document.append("orders", leftOrders);
        collection.insertOne(document);
        mongo.close();
    }

    private MongoCollection<Document> getLeadTraderCollection(MongoClient mongo) {
        String database, collection;
        if (isProd) {
            database = getProperty("mongo.database");
            collection = getProperty("mongo.collection.simple");
        } else {
            database = getProperty("test.mongo.database");
            collection = getProperty("test.mongo.collection.simple");
        }
        return mongo.getDatabase(database).getCollection(collection);
    }

    private MongoCollection<Document> getLeftOrdersCollection(MongoClient mongo) {
        String database, collection;
        if (isProd) {
            database = getProperty("mongo.database");
            collection = getProperty("mongo.collection.simple.left");
        } else {
            database = getProperty("test.mongo.database");
            collection = getProperty("test.mongo.collection.simple.left");
        }
        return mongo.getDatabase(database).getCollection(collection);
    }
}
