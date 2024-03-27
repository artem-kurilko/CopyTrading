package com.copytrading.service;

import com.copytrading.model.PositionSide;
import com.copytrading.model.UniPosition;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.*;

import static com.copytrading.util.ConfigUtils.getProperty;

/**
 * Service class for storing orders info with trader id.
 */
public class TestnetDatabaseService {
    private final boolean isProd;

    public TestnetDatabaseService(boolean isProd) {
        this.isProd = isProd;
    }

    /*public Set<String> retrieveTradersIds() {
        Set<String> ids = new HashSet<>();
        List<UniPosition> orders = retrieveOrdersState();
        orders.forEach(ord -> ids.add(ord.getTraderId()));
        return ids;
    }

    @SafeVarargs
    public final void saveOrderState(List<UniPosition>... orders) {
        try {
            MongoClient mongo = new MongoClient( "localhost" , 27017 );
            MongoCollection<Document> collection = getCollection(mongo);
            // delete all
            collection.deleteMany(new Document());
            // save all
            List<Document> documentList = new ArrayList<>();
            for (List<UniPosition> orderStateList : orders) {
                orderStateList.forEach(ord -> documentList.add(formatToDocument(ord)));
            }
            collection.insertMany(documentList);
            mongo.close();
        } catch (Exception e) {
            System.out.println("EXCEPTION SAVING ORDER STATE. " + Arrays.toString(orders));
            throw e;
        }
    }

    public List<UniPosition> retrieveOrdersState() {
        List<UniPosition> orderStatesList = new LinkedList<>();
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCollection<Document> collection = getCollection(mongo);

        // find all
        FindIterable<Document> iterDoc = collection.find();
        for (Document document : iterDoc) {
            orderStatesList.add(formatFromDocument(document));
        }
        mongo.close();
        return orderStatesList;
    }

    private UniPosition formatFromDocument(Document document) {
        return UniPosition.builder()
                .traderId(document.getString("traderId"))
                .symbol(document.getString("symbol"))
                .side(PositionSide.valueOf(document.getString("side")))
                .leverage(document.getInteger("leverage"))
                .entryPrice(document.getDouble("entryPrice"))
                .size(document.getDouble("size"))
                .leadTraderEntryPrice(document.getDouble("leadTraderEntryPrice"))
                .closePrice(document.getDouble("closePrice"))
                .closePnl(document.getDouble("closePnl"))
                .updateTime(document.getLong("updateTime"))
                .time(document.getLong("time"))
                .build();
    }

    private Document formatToDocument(UniPosition orderState) {
        Document document = new Document();
        document.append("traderId", orderState.getTraderId());
        document.append("symbol", orderState.getSymbol());
        document.append("side", orderState.getSide().name());
        document.append("leverage", orderState.getLeverage());
        document.append("entryPrice", orderState.getEntryPrice());
        document.append("size", orderState.getSize());
        document.append("leadTraderEntryPrice", orderState.getLeadTraderEntryPrice());
        document.append("closePrice", orderState.getClosePrice());
        document.append("closePnl", orderState.getClosePnl());
        document.append("updateTime", orderState.getUpdateTime());
        document.append("time", orderState.getTime());
        return document;
    }

    private MongoCollection<Document> getCollection(MongoClient mongo) {
        String database, collection;
        if (isProd) {
            database = getProperty("mongo.database");
            collection = getProperty("mongo.collection");
        } else {
            database = getProperty("test.mongo.database");
            collection = getProperty("test.mongo.collection");
        }
        return mongo.getDatabase(database).getCollection(collection);
    }*/

}
