package org.example.Kafka;

import org.apache.kafka.clients.producer.*;
import java.util.Properties;

public class KafkaProducerTest {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);

        // Warm-up (optional)
        for (int i = 0; i < 100; i++) {
            producer.send(new ProducerRecord<>("test-topic", "Warm-up " + i));
        }

        // Benchmark
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            long produceStart = System.nanoTime();
            producer.send(new ProducerRecord<>("test-topic", "Message " + i));
            long produceEnd = System.nanoTime();
            System.out.println("Produce Latency (ns): " + (produceEnd - produceStart));
        }
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Total Produce Time (ms): " + totalTime);
        System.out.println("Throughput (msg/s): " + (1000 / (totalTime / 1000.0)));

        producer.close();
    }
}
