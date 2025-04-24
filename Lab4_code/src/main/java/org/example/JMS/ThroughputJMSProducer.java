package org.example.JMS;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class ThroughputJMSProducer {
    public static void main(String[] args) throws Exception {
        String filePath = "message.txt";
        String messageContent = new String(Files.readAllBytes(Paths.get(filePath)));

        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("TEST.QUEUE");
        MessageProducer producer = session.createProducer(queue);
        TextMessage message = session.createTextMessage(messageContent);

        int maxSuccessfulThroughput = 0;
        int throughput = 10;

        while (true) {
            System.out.println("\nTesting throughput: " + throughput + " msg/s");
            long periodNs = 1_000_000_000L / throughput;
            long adjustedSleepNs = (long) (periodNs * 0.8);

            long testStart = System.nanoTime();
            for (int i = 0; i < throughput; i++) {
                long sendStart = System.nanoTime();
                producer.send(message);
                long sendEnd = System.nanoTime();

                long sleepTime = adjustedSleepNs - (sendEnd - sendStart);
                if (sleepTime > 0) {
                    TimeUnit.NANOSECONDS.sleep(sleepTime);
                }
            }
            long testDurationMs = (System.nanoTime() - testStart) / 1_000_000;
            System.out.println("Sent " + throughput + " messages in " + testDurationMs + " ms");

            Thread.sleep(3000); // Wait for consumer

            if (testDurationMs <= 1000 && ThroughputJMSConsumer.validateReceivedCount(throughput)) {
                maxSuccessfulThroughput = throughput;
                throughput *= 2;
            } else {
                System.out.println("❌ Failed at throughput: " + throughput);
                break;
            }
        }

        System.out.println("✅ Maximum sustainable throughput: " + maxSuccessfulThroughput + " msg/s");
        connection.close();
    }
}