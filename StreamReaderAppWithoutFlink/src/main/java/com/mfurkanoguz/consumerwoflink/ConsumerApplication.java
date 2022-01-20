package com.mfurkanoguz.consumerwoflink;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.json.simple.parser.ParseException;
import java.time.Duration;
import java.util.Arrays;


public class ConsumerApplication {
    public static void main(String[] args) throws ParseException {
        //Mongo Db için host ve port adresleri.
        final String MONGODBHOST="localhost";
        final int MONGODBPORT=27017;
        //Mongo Db için database ve collection isimleri
        String mongoDBName="recommendationDB";      //Database Name
        String mongoCollectionName="ProductView";     //Collection Name
        //Kafka host,port,topc bilgileri ve property ayarları
        final String KAFKAHOST="localhost";
        final String KAFKAPORT="9092";
        final String CONSUMERTOPIC=args[0];
        Properties properties=new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,KAFKAHOST+":"+KAFKAPORT);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"eticaret");
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG,"mfurkanoguz");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(CONSUMERTOPIC));   //hangi topicten alacağını belirtir

        while(true){
            ConsumerRecords<String, String> record = consumer.poll(Duration.ZERO);
            for(ConsumerRecord<String, String> rec : record){
                //String olarak gelen mesaj Json formatına çevirilir
                JSONParser jsonParser = new JSONParser();
                JSONObject json = (JSONObject) jsonParser.parse(rec.value());
                //System.out.println(json);//Gelen veriyi yazdırmak için
                MongoClient mongoClient = new MongoClient(MONGODBHOST, MONGODBPORT);
                MongoDatabase database = mongoClient.getDatabase(mongoDBName);
                MongoCollection<Document> collection = database.getCollection(mongoCollectionName);
                //Mongo document tipinde kayıt aldığı için verimizi document içerisine put ederiz
                Document document = new Document();
                document.put("user_id", json.get("userid"));
                document.put("product_id", new JSONObject((Map) json.get("properties")).get("productid"));
                document.put("timestamp", json.get("timestamp"));
                collection.insertOne(document); //MongoDb Sink işlemi
                mongoClient.close();

            }

        }




    }
}
