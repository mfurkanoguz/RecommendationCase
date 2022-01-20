package com.mfurkanoguz.eticaretapi.controller;

import com.mfurkanoguz.eticaretapi.dbconnections.MongoDBConnection;
import com.mfurkanoguz.eticaretapi.model.User;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@RestController
public class BestSellerController {
    final String MONGODBHOST="localhost";
    final int MONGODBPORT=27017;
    //Mongo Db için database ve collection isimleri
    String mongoDBName="recommendationDB";      //Database Name
    String mongoHistoryCollectionName="ProductView";
    String mongoProductCollectionName="Products";
    String mongoAllCollectionName="All";

    @GetMapping("/bestseller")
    public ResponseEntity<User> getBestSeller(@RequestParam String userid) throws Exception {
        int productCount=0;
        int totalProductCount=0;
        float maxProductCount=10;
        float categorySize=0;
        User user = new User();

        Set<String> history = getUserHistory(userid);
        Set<String> productCategory = getProductCategory(history);
        Set<String> products = new HashSet<>();

        if(productCategory.size() > 3){
            categorySize=3.0f;
        }
        else{
            categorySize=productCategory.size();
        }
        for(String category : productCategory){
            if(totalProductCount < maxProductCount){
                MongoCollection<Document> collectionCategory= MongoDBConnection.MongoDBUtil
                        .getConnect(MONGODBHOST,MONGODBPORT,mongoDBName,category);
                MongoCursor<Document> productIterator = collectionCategory.find().iterator();
                productCount=0;
                while(productIterator.hasNext()){
                    if((productCount<(Math.ceil(maxProductCount/categorySize)))&&(totalProductCount<10)){
                        ArrayList<Object> values = new ArrayList<>(productIterator.next().values());
                        products.add((String) values.get(2));
                        //System.out.println((String) values.get(2));
                    }
                    else{
                        break;
                    }
                    productCount++;
                    totalProductCount++;
                }
                user.setType("personalized");
            }
        }
        if(totalProductCount==0){
            user.setType("non-personalized");
            MongoCollection<Document> collectionAll= MongoDBConnection.MongoDBUtil
                    .getConnect(MONGODBHOST,MONGODBPORT,mongoDBName,mongoAllCollectionName);
            MongoCursor<Document> allProductIterator = collectionAll.find().iterator();
            while(allProductIterator.hasNext()){
                if(totalProductCount<10){
                    ArrayList<Object> values = new ArrayList<>(allProductIterator.next().values());
                    products.add((String) values.get(2));
                    //System.out.println((String) values.get(2));
                }
                else{
                    break;
                }
                totalProductCount++;
            }
        }
        user.setUserid(userid);
        user.setProducts(products);
        MongoDBConnection.MongoDBUtil.mongoClose();
        return ResponseEntity.ok(user);
    }

    public Set<String> getUserHistory(String userId) throws Exception {
        MongoCollection<Document> collectionHistory= MongoDBConnection.MongoDBUtil
                .getConnect(MONGODBHOST,MONGODBPORT,mongoDBName,mongoHistoryCollectionName);

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("user_id",userId);
        Set<String> history = new HashSet<>();
        MongoCursor<Document> iterator = collectionHistory.find(searchQuery).iterator();
        while(iterator.hasNext()){
            ArrayList<Object> values = new ArrayList<>(iterator.next().values());
            history.add((String) values.get(2));
            //System.out.println(values);
        }
        MongoDBConnection.MongoDBUtil.mongoClose();
        return history;
    }


    public Set<String> getProductCategory(Set<String> products) throws Exception {
        Set<String> category = new HashSet<>();

        MongoCollection<Document> collectionProduct= MongoDBConnection.MongoDBUtil
                .getConnect(MONGODBHOST,MONGODBPORT,mongoDBName,mongoProductCollectionName);
        for(String product : products){
            BasicDBObject searchQueryProduct = new BasicDBObject();
            searchQueryProduct.put("product_id",product);
            MongoCursor<Document> productIterator = collectionProduct.find(searchQueryProduct).iterator();
            while(productIterator.hasNext()){
                ArrayList<Object> values = new ArrayList<>(productIterator.next().values());
                category.add((String) values.get(1));
            }
        }
        MongoDBConnection.MongoDBUtil.mongoClose();
        return category;
    }
}
