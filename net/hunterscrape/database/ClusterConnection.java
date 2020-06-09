package net.hunterscrape.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ClusterConnection {
    private static MongoDatabase db;

    public static void setup() {
        try {
            String address = InetAddress.getLocalHost().getHostAddress();
            Document document = (Document)ClusterConnection.getCollection().find(Filters.eq("address", address)).first();
            if (document == null) {
                Document local = new Document("address", address);
                local.put("blacklisted", (Object)true);
                ClusterConnection.getCollection().insertOne(local);
                System.err.println("[HunterScrape] Restart.");
                System.exit(0);
            } else if (document.getBoolean("blacklisted").equals(true)) {
                System.err.println("[HunterScrape] You are blacklisted from the HunterNetwork.");
                System.exit(0);
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void connect() {
        MongoClientURI uri = new MongoClientURI("mongodb+srv://hunter:UsNVjQcAqBmJBrzca7abfLH5SxtHAmAXtF2Qkx5k9vT4P2Jf93Qh4sx8389Lxcep@cluster0-k5hat.mongodb.net/test?retryWrites=true&w=majority");
        MongoClient mongoClient = new MongoClient(uri);
        db = mongoClient.getDatabase("hunter");
    }

    public static MongoCollection<Document> getCollection() {
        return db.getCollection("userDatabase");
    }
}

