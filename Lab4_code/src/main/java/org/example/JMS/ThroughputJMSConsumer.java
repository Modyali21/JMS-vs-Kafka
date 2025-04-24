package org.example.JMS;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;
public class ThroughputJMSConsumer {

    public static boolean validateReceivedCount(int expectedCount) throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("TEST.QUEUE");
        MessageConsumer consumer = session.createConsumer(queue);

        long timeoutMs = 5000;
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (counter.get() < expectedCount && System.currentTimeMillis() < deadline) {
            Message msg = consumer.receive(100); // Wait max 100ms per msg
            if (msg != null) counter.incrementAndGet();
        }

        connection.close();
        System.out.println("Consumer received " + counter.get() + " / " + expectedCount + " messages.");
        return counter.get() == expectedCount;
    }

    public static void main(String[] args) throws Exception {
        // Standalone run (for debug)
        validateReceivedCount(10000);
    }
}
