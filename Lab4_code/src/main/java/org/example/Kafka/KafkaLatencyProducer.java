package org.example.Kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

public class KafkaLatencyProducer {
    public static void main(String[] args) throws Exception {
        // Producer setup
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        int numberOfMessages = 10000;

        // Read message from message.txt file
        String message = readMessageFromFile("message.txt");

        // Send all messages
        for (int i = 0; i < numberOfMessages; i++) {
            long timestamp = System.nanoTime();
            String messageWithTimestamp = timestamp + ":" + message;
            producer.send(new ProducerRecord<>("test-topic", Integer.toString(i), messageWithTimestamp));
            Thread.sleep(1); // slight delay to help with consumption
        }

        producer.close();
        System.out.println("Producer finished sending all messages.");
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

