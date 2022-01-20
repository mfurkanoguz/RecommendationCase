package com.mfurkanoguz.producerapp;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ProducerApplication {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException, ParseException {
        //Kafka host,port,topc bilgileri ve property ayarları
        final String KAFKAHOST="localhost";
        final String KAFKAPORT="9092";
        final String PRODUCERTOPIC=args[1];
        Properties properties=new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,KAFKAHOST+":"+KAFKAPORT);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        //kafka producer property ayarlarıyla oluşturulur
        Producer producer=new KafkaProducer<String,String>(properties);
        JSONParser jsonParser = new JSONParser();   //String olarak gelen kafka mesajlarını json haline getirmek için

        //Verilen dosya yolundan json dosyasını alır ve okur
        File myObj = new File(args[0]);
        Scanner myReader = new Scanner(myObj);

        //Saniyede bir olarak json dosyasından gelen product event bilgilerine timestamp ekler ve
        //kafkaya produce eder
        while (myReader.hasNextLine()) {
            Timestamp timestamp=new Timestamp(System.currentTimeMillis());
            String data = myReader.nextLine();
            JSONObject json = (JSONObject) jsonParser.parse(data);
            json.put("timestamp",timestamp.toString());
            //System.out.println(json); //Kafkaya yazılacak değeri console da görmek için
            ProducerRecord<String, String> record = new ProducerRecord<String,String>(PRODUCERTOPIC, json.toJSONString());
            producer.send(record);
            TimeUnit.SECONDS.sleep(1);
        }

        producer.close();

    }
}
