package org.example.JMS;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ThroughputProducer {

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
        double maxSuccessfulThroughput = 0;
        int count = 100;
        double throughput = 0;

        while (true) {
            System.out.println("Testing count: " + count + " msg");
            double periodNs = 1000.0 / count;
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                producer.send(message);
                Thread.sleep((long) (periodNs - 0.2 * periodNs));
            }
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("Sent " + count + " messages in " + (duration) + " ms");
            throughput = count/(duration/1_000.0);
            System.out.println("Throughput: " + throughput);

            // Let consumer validate
            Thread.sleep(3000); // Wait for consumer

            if (ThroughputConsumer.validateReceivedCount(count, throughput)) {
                maxSuccessfulThroughput = throughput;
                count *= 2;
            } else {
                System.out.println("Failed at throughput: " + count);
                break;
            }
        }

        System.out.println("✅ Maximum sustainable throughput: " + maxSuccessfulThroughput + " msg/s");
        connection.close();
    }
}