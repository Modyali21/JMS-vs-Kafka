package org.example.Kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class KafkaProducerTest {
    public static void main(String[] args) throws Exception {
        // Producer properties
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        int numberOfMessages = 1000;
        int loops = 1000;
        long[] produceTimes = new long[loops];

        // Read message from message.txt file
        //         === Read content from file ===
        String filePath = "message.txt";  // or use an absolute path like "C:/files/message.txt"
        String messageContent = new String(Files.readAllBytes(Paths.get(filePath)));
        String value = messageContent;

        // Calculate the median response time (when sending 1000 messages) in 1000 runs
        for (int i = 0; i < loops; i++) {
            long startTime = System.nanoTime();
            for (int j = 0; j < numberOfMessages; j++) {
                producer.send(new ProducerRecord<>("test-topic", Integer.toString(j), value)).get();
            }
            long endTime = System.nanoTime();
            produceTimes[i] = endTime - startTime;
            System.out.println(i);
        }
        producer.close();

        java.util.Arrays.sort(produceTimes);
        System.out.println("Median produce time (ns): " + produceTimes[loops / 2]);
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
