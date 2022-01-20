package com.mfurkanoguz.eticaretapi.dbconnections;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConnection {
    public static class MongoDBUtil   {
        static MongoClient mongoClient=null;
        public static MongoCollection<Document> getConnect(String host, int port, String mongoDBName, String mongoCollectionName) throws Exception{
            mongoClient = new MongoClient(host, port);
            MongoDatabase database = mongoClient.getDatabase(mongoDBName);
            MongoCollection<Document> collection = database.getCollection(mongoCollectionName);
            return collection;
        }
        public static void mongoClose(){
            if (mongoClient!=null)
                mongoClient.close();
        }

    }

}
