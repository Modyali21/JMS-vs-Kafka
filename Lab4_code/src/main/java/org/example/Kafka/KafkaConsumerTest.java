package org.example.Kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerTest {
    public static void main(String[] args) throws IOException {
        // Producer properties
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
        String topic = "test-topic";

        // Read message from message.txt file
        String message = readMessageFromFile("message.txt");

        // Produce 1000 messages (send the same message 1000 times)
        int numberOfMessages = 1000;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(new ProducerRecord<>(topic, Integer.toString(i), message));
        }
        producer.close();

        // Consumer properties
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(topic));

        long[] consumeTimes = new long[numberOfMessages];
        int count = 0;

        // Consume 1000 messages
        while (count < numberOfMessages) {
            long startTime = System.nanoTime();
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
            long endTime = System.nanoTime();
            for (ConsumerRecord<String, String> record : records) {
                consumeTimes[count++] = endTime - startTime;
                if (count >= numberOfMessages) {
                    break;
                }
            }
        }

        consumer.close();

        java.util.Arrays.sort(consumeTimes);
        System.out.println("Median consume time (ns): " + consumeTimes[numberOfMessages / 2]);
    }

    // Method to read the content of message.txt file
    private static String readMessageFromFile(String filename) throws IOException {
        StringBuilder messageBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                messageBuilder.append(line).append("\n");
            }
        }
        return messageBuilder.toString();
    }
}
