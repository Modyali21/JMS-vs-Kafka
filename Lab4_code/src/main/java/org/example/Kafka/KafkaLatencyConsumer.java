package org.example.Kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaLatencyConsumer {
    public static void main(String[] args) throws Exception {
        // Consumer setup
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "latency-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("test-topic"));

        int numberOfMessages = 10000;
        long[] latencies = new long[numberOfMessages];

        int count = 0;
        while (count < numberOfMessages) {
            // Poll for records, it will keep waiting for new messages until all are consumed
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            long receivedTime = System.nanoTime();
            for (ConsumerRecord<String, String> record : records) {
                String[] parts = record.value().split(":", 2);
                long sentTime = Long.parseLong(parts[0]);
                latencies[count++] = receivedTime - sentTime;
                System.out.println(count);
                if (count >= numberOfMessages) {
                    break;
                }
            }
        }

        consumer.close();

        java.util.Arrays.sort(latencies);
        System.out.println("Median end-to-end latency (ns): " + latencies[numberOfMessages / 2]);
        System.out.println("Consumer finished consuming all messages.");
    }
}
