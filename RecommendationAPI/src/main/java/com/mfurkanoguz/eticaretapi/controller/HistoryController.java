package com.mfurkanoguz.eticaretapi.controller;

import com.mfurkanoguz.eticaretapi.dbconnections.MongoDBConnection;
import com.mfurkanoguz.eticaretapi.model.User;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class HistoryController {

    final String MONGODBHOST="localhost";
    final int MONGODBPORT=27017;
    //Mongo Db için database ve collection isimleri
    String mongoDBName="recommendationDB";      //Database Name
    String mongoCollectionName="ProductView";     //Patternleri tuttuğumuz Collection Name

    @GetMapping("/history")
    public ResponseEntity<User> getHistory(@RequestParam String userid) throws Exception {
        MongoCollection<Document> collection= MongoDBConnection.MongoDBUtil
                .getConnect(MONGODBHOST,MONGODBPORT,mongoDBName,mongoCollectionName);

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("user_id",userid);
        if(collection.find(searchQuery).first() == null){
            System.out.println("Name is not found!");
            //System.exit(1);
            return null;
        }
        Set<String> history = new HashSet<>();
        MongoCursor<Document> iterator = collection.find(searchQuery).iterator();
        while(iterator.hasNext()){
            ArrayList<Object> values = new ArrayList<>(iterator.next().values());
            history.add((String) values.get(2));
            System.out.println(values);
        }
        if(history.size()<5){
            history.clear();
        }
        User user = new User(userid, "personalized", history);
        MongoDBConnection.MongoDBUtil.mongoClose();
        return ResponseEntity.ok(user);

    }
    @DeleteMapping("/history")
    public ResponseEntity<String> deleteHistory(@RequestParam String userid, String productid) throws Exception {
        MongoCollection<Document> collection= MongoDBConnection.MongoDBUtil
                .getConnect(MONGODBHOST,MONGODBPORT,mongoDBName,mongoCollectionName);
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("user_id",userid);
        searchQuery.put("product_id",productid);
        DeleteResult deleteResult = collection.deleteMany(searchQuery);
        MongoDBConnection.MongoDBUtil.mongoClose();
        if(deleteResult.getDeletedCount()<1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or product not found in database");
        }
        return ResponseEntity.ok().body(productid+" deleted from database");

    }

}
