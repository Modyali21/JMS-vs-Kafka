package org.example.JMS;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThroughputConsumer {

    public static boolean validateReceivedCount(int expectedCount, double throughput) throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("TEST.QUEUE");
        MessageConsumer consumer = session.createConsumer(queue);
        long timeoutMs = (long) Math.max(5000, (expectedCount * 100) / throughput);
        long deadline = System.currentTimeMillis() + timeoutMs;

        // Phase 1: Consume expected messages
        long startTime = System.currentTimeMillis();
        while (counter.get() < expectedCount && System.currentTimeMillis() < deadline) {
            Message msg = consumer.receive(100);
            if (msg != null) counter.incrementAndGet();
        }
        long duration = System.currentTimeMillis() - startTime;
        throughput = counter.get() / (duration / 1000.0);
        System.out.println("📊 Consumer Throughput: " + throughput + " msg/s");

        // Phase 2: Purge remaining messages (NEW)
        System.out.println("Purging remaining messages...");
        Message remainingMsg;
        do {
            remainingMsg = consumer.receiveNoWait(); // Non-blocking check
        } while (remainingMsg != null);

        connection.close();
        System.out.println("Consumer received " + counter.get() + " / " + expectedCount + " messages.");
        return counter.get() == expectedCount;
    }

    public static void main(String[] args) throws Exception {
        validateReceivedCount(10000, 100);
    }
}