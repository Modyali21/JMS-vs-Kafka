package org.example.JMS;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class JMSProducerTest {
    public static void main(String[] args) throws JMSException, IOException, InterruptedException {

//         === Read content from file ===
        String filePath = "message.txt";  // or use an absolute path like "C:/files/message.txt"
        String messageContent = new String(Files.readAllBytes(Paths.get(filePath)));


        ArrayList<Long> responseTimes = new ArrayList<>();

//        preparing the throughput parameters
        int throughput = 1000; // messages per second
        long periodNs = 1_000_000_000L / throughput; // nanoseconds per message
        long adjustedSleepNs = (long)(periodNs * 0.8); // account for thread switching

//        Connect to apache activemq
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("TEST.QUEUE");

        MessageProducer producer = session.createProducer(queue);
        // Benchmark
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            TextMessage message = session.createTextMessage(messageContent);
            long timestamp = System.nanoTime();  // Capture timestamp of when message was sent
            message.setLongProperty("timestamp", timestamp); // Add timestamp to message

            long produceStart = System.nanoTime();
            producer.send(message);
            long produceEnd = System.nanoTime();
            responseTimes.add(produceEnd - produceStart);

            // Sleep to throttle
            long sleepTime = adjustedSleepNs - (System.nanoTime() - produceStart);
            if (sleepTime > 0) {
                TimeUnit.NANOSECONDS.sleep(sleepTime);
            }
        }
        long totalTime = System.currentTimeMillis() - startTime;
        Collections.sort(responseTimes);
        long medianResponseTime = responseTimes.get(responseTimes.size() / 2);
        System.out.println("Sent 1000 messages from file content.");
        System.out.println("Median response time for 1k messages (ns): " + medianResponseTime);
        System.out.println("Total Produce Time (ms): " + totalTime);
        System.out.println("Throughput (msg/s): " + (1000 / (totalTime / 1000.0)));

        connection.close();
    }
}