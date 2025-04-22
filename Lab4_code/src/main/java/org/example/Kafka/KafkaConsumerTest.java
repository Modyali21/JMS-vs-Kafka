package org.example.Kafka;

import org.apache.kafka.clients.consumer.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerTest {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test-topic"));

        // Benchmark
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            long consumeStart = System.nanoTime();
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                // Process record
            }
            long consumeEnd = System.nanoTime();
            System.out.println("Consume Latency (ns): " + (consumeEnd - consumeStart));
        }
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Total Consume Time (ms): " + totalTime);
        System.out.println("Throughput (msg/s): " + (1000 / (totalTime / 1000.0)));

        consumer.close();
    }
}
