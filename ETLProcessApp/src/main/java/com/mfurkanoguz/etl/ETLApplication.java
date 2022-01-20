package com.mfurkanoguz.etl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;

public class ETLApplication {
    public static void main(String[] args) throws IOException, SQLException {
        //Veritabanı bağlantı bilgileri
        final String MONGODBHOST="localhost";
        final int MONGODBPORT=27017;
        String mongoDBName="recommendationDB";
        String url = "jdbc:postgresql://localhost/data-db";
        String user = "postgres";
        String password = "123456";
        Connection conn = null;
        Statement statement=null;
        MongoClient mongoClient=null;
        MongoCollection<Document> collection=null;
        //Çalıştırılmak istenen .sql dosyasının path üzerinden alınarak parse edilmesi
        //.sql dosyası ve script aynı isimde olmak zorunda değildir
        //Örnek : a.sql All şeklinde bir girdi a.sql dosyasının mevcut olması halinde geçerlidir
        Scanner scanner = new Scanner(Paths.get(args[0]), StandardCharsets.UTF_8.name());
        String sql = scanner.useDelimiter("\\A").next();

        try {
            mongoClient = new MongoClient(MONGODBHOST, MONGODBPORT);
            MongoDatabase database = mongoClient.getDatabase(mongoDBName);
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL successfully.");
            statement = conn.createStatement();

            //Script dosyasındaki sql sorgusu çalıştırılır
            ResultSet resultSet = statement.executeQuery(sql);

            //Çalıştırılan sorgu sonucunda elde edilen kolonlar seçilen script'e göre MongoDB'ye kaydedilir
            while(resultSet.next()){
                //Her categori için ayrı bir collection yaratılır ve sıralanmış ürünler kendi
                //kategorisine göre kaydedilir
                if(args[1].equalsIgnoreCase("categoryBased")) {
                    collection = database.getCollection(resultSet.getString("category_id"));
                    Document document = new Document();
                    document.put("category_id", resultSet.getString("category_id"));
                    document.put("product_id", resultSet.getString("product_id"));
                    document.put("order_count", resultSet.getString("order_count"));
                    collection.insertOne(document);
                }
                //Her ürün en çok satılma sırasına göre All isimli collectiona kaydedilir
                else if(args[1].equalsIgnoreCase("All")){
                    collection = database.getCollection("All");
                    Document document = new Document();
                    document.put("category_id", resultSet.getString("category_id"));
                    document.put("product_id", resultSet.getString("product_id"));
                    document.put("order_count", resultSet.getString("order_count"));
                    collection.insertOne(document);
                }
                //Ürünler ve bulundukları kategori Products isimli collectiona kaydedilir
                else if(args[1].equalsIgnoreCase("Product")){
                    collection = database.getCollection("Products");
                    Document document = new Document();
                    document.put("category_id", resultSet.getString("category_id"));
                    document.put("product_id", resultSet.getString("product_id"));
                    collection.insertOne(document);
                }
                else{
                    System.out.println("Geçersiz Seçim!");
                    System.exit(1);
                }

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        finally {
            scanner.close();
            statement.close();
            conn.close();
            mongoClient.close();
        }
    }
}
