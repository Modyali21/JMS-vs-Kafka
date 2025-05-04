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
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        double maxSuccessfulThroughput = 0;
        int count = 100;
        double throughput;

        while (true) {
            System.out.println("\n🔄 Testing with count = " + count + " messages...");
            TextMessage message = session.createTextMessage(messageContent);

            // Calculate time spacing to keep sending at a fixed rate
            double periodNs = 1000.0 / count;

            // Start measuring time
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                producer.send(message);
                Thread.sleep((long) (periodNs - 0.2 * periodNs)); // slightly faster than expected rate
            }
            long duration = System.currentTimeMillis() - startTime;

            throughput = count / (duration / 1000.0);
            System.out.println("⏱️ Duration: " + duration + " ms");
            System.out.println("📊 Producer Throughput: " + throughput + " msg/s");

            // Give time for consumer to process
//            Thread.sleep(3000);

            // Ask consumer if all messages received
            if (ThroughputConsumer.validateReceivedCount(count, throughput)) {
                System.out.println("✅ Test passed.");
                maxSuccessfulThroughput = throughput;
                count *= 2; // Increase for next round
            } else {
                System.out.println("❌ Test failed. Messages may be lost or delayed.");
                break;
            }
        }

        System.out.println("\n✅ Maximum sustainable throughput: " + maxSuccessfulThroughput + " msg/s");
        connection.close();
    }
}
