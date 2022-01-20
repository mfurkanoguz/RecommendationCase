package com.mfurkanoguz.consumerapp;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.Map;
import java.util.Properties;

public class ConsumerApplication {

    public static void main(String[] args) throws Exception {
        //Tüm veri akışını içeren environment tanımı
        //Veri kafkadan akacağı ve sürekli olacağı için StreamExecutionEnvironment kullanıldı
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //Kafka için host ve port adresleri
        final String KAFKAHOST="localhost";
        final String KAFKAPORT="9092";
        //Kafka için topic ismi parametre olarak alınır
        final String CONSUMERTOPIC=args[0];
        //Mongo Db için host ve port adresleri.
        final String MONGODBHOST="localhost";
        final int MONGODBPORT=27017;
        //Mongo Db için database ve collection isimleri
        String mongoDBName="recommendationDB";      //Database Name
        String mongoCollectionName="ProductView";     //Collection Name

        //Kafka için bağlantı ayaları
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", KAFKAHOST+":"+KAFKAPORT);

        //Kafka consumer initilaze
        FlinkKafkaConsumer<String> kafkaSource =
                new FlinkKafkaConsumer<>(CONSUMERTOPIC, new SimpleStringSchema(), properties);

        DataStream<String> mainStream = env //kaynak olarak verilen topic ismindeki kafka kullanılır
                .addSource(kafkaSource);

        //Bu pipeline ile kafkadan gelen akış json formatına map edilir
        //ve mongodb ye sink edilir
        mainStream.map(new MapFunction<String, JSONObject>() {
            @Override
            public JSONObject map(String value) throws Exception {
                JSONParser jsonParser = new JSONParser();
                JSONObject json = (JSONObject) jsonParser.parse(value);
                return json;
            }
        })
        .addSink(new SinkFunction<JSONObject>() {
            @Override
            public void invoke(JSONObject json, Context context) throws Exception {
                SinkFunction.super.invoke(json, context);
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
        });

        env.execute();  //Akan veriyi sürekli dinlemesi için
    }
}
