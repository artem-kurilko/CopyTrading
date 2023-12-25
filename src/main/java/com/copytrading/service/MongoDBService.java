package com.copytrading.service;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import com.mongodb.MongoClient;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class MongoDBService {

    public static void main(String[] args) {
        System.out.println(someshit());
    }

    public static List<Document> getOrders() {
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

    }

    public static void deleteOrder() {

    }

}
