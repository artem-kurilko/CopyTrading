package com.copytrading.service;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.*;

import static com.copytrading.util.ConfigUtils.getProperty;

/**
 * Simple service class to store lead trader id and ids of orders copied from him.
 */
public class LeadTraderDatabaseService {
    private final boolean isProd;

    public LeadTraderDatabaseService(boolean isProd) {
        this.isProd = isProd;
    }

    public List<String> getLeaderIds() {
        Set<String> ids = new HashSet<>();
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeadTraderCollection(mongo);

        FindIterable<Document> iterDoc = collection.find();
        for (Document document : iterDoc) {
            ids.add(document.getString("id"));
        }
        mongo.close();
        return new ArrayList<>(ids);
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

    public void saveOrderToTrader(String id, String symbol) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeadTraderCollection(mongo);
        FindIterable<Document> iterDoc = collection.find();
        for (Document document : iterDoc) {
            if (document.getString("id").equals(id)) {
                List<String> positions = document.getList("orders", String.class);
                List<String> updatePositions = new ArrayList<>(positions);
                updatePositions.add(symbol);
                Document updatedDocument = new Document()
                        .append("id", document.getString("id"))
                        .append("orders", updatePositions);
                collection.replaceOne(document, updatedDocument);
                mongo.close();
                return;
            }
        }
        throw new IllegalArgumentException("Trader with symbol: " + symbol + " not found.");
    }

    public void saveNewTrader(String id, List<String> symbols) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeadTraderCollection(mongo);
        Document document = new Document();
        document.append("id", id);
        document.append("orders", symbols);
        collection.insertOne(document);
        mongo.close();
    }

    public void removeOrderFromTrader(String symbol) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeadTraderCollection(mongo);
        FindIterable<Document> iterDoc = collection.find();
        for (Document document : iterDoc) {
            List<String> positions = document.getList("orders", String.class);
            if (positions.contains(symbol)) {
                List<String> updatePositions = new ArrayList<>(positions);
                updatePositions.remove(symbol);
                Document updatedDocument = new Document()
                        .append("id", document.getString("id"))
                        .append("orders", updatePositions);
                collection.replaceOne(document, updatedDocument);
                mongo.close();
                return;
            }
        }
        throw new IllegalArgumentException("Trader with symbol: " + symbol + " not found.");
    }

    public List<String> getAndRemoveTradersSymbols(String id) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getLeadTraderCollection(mongo);

        FindIterable<Document> iterDoc = collection.find();
        for (Document document : iterDoc) {
            if (document.getString("id").equals(id)) {
                List<String> symbols = document.getList("orders", String.class);
                collection.deleteOne(document);
                return symbols;
            }
        }
        mongo.close();
        return Collections.emptyList();
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

    public List<String> getUnmarkedOrders() {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getUnmarkedOrdersCollection(mongo);
        Document document = collection.find().first();
        if (document != null) {
            List<String> leftOrders = new ArrayList<>(document.getList("orders", String.class));
            mongo.close();
            return leftOrders;
        } else {
            mongo.close();
            return Collections.emptyList();
        }
    }

    public void saveUnmarkedOrders(List<String> symbols) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getUnmarkedOrdersCollection(mongo);
        Document document = collection.find().first();

        if (document != null) {
            List<String> currentSymbols = document.getList("orders", String.class);
            currentSymbols.addAll(symbols);
            collection.deleteOne(new Document());
            Document updated = new Document().append("orders", currentSymbols);
            collection.insertOne(updated);
            mongo.close();
        } else {
            mongo.close();
            System.out.println("UNMARKED ORDERS DOCUMENT NOT FOUND");
        }
    }

    public void removeOrderFromUnmarkedOrders(String symbol) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getUnmarkedOrdersCollection(mongo);
        Document document = collection.find().first();

        if (document != null) {
            List<String> currentSymbols = document.getList("orders", String.class);
            currentSymbols.remove(symbol);
            collection.deleteOne(new Document());
            Document updated = new Document().append("orders", currentSymbols);
            collection.insertOne(updated);
            mongo.close();
        } else {
            mongo.close();
            System.out.println("UNMARKED ORDERS DOCUMENT NOT FOUND");
        }
    }

    /**
     * Removes all records and sets the passed ones.
     * @param leftOrders list of orderId, it is copied orders of traders that no longer show their positions
     */
    public void resetUnmarkedOrders(List<String> leftOrders) {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getUnmarkedOrdersCollection(mongo);
        collection.deleteMany(new Document());
        Document document = new Document();
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

    private MongoCollection<Document> getUnmarkedOrdersCollection(MongoClient mongo) {
        String database, collection;
        if (isProd) {
            database = getProperty("mongo.database");
            collection = getProperty("mongo.collection.simple.unmarked");
        } else {
            database = getProperty("test.mongo.database");
            collection = getProperty("test.mongo.collection.simple.unmarked");
        }
        return mongo.getDatabase(database).getCollection(collection);
    }
}
