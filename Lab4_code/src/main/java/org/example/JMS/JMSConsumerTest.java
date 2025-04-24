package org.example.JMS;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JMSConsumerTest {
    public static void main(String[] args) throws JMSException, InterruptedException {

        List<Long> latencies = new ArrayList<>();

        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("TEST.QUEUE");

        MessageConsumer consumer = session.createConsumer(queue);

        // Benchmark
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {

            Message msg = consumer.receive();
            // Extract the timestamp property from the message
            long receiveTimestamp = System.nanoTime(); // Current time when message is received
            long sentTimestamp = msg.getLongProperty("timestamp");

            // Calculate latency and store it
            long latency = receiveTimestamp - sentTimestamp;
            latencies.add(latency);
        }

        Collections.sort(latencies);
        long medianLatency = latencies.get(latencies.size() / 2);

        System.out.println("Consumed 10000 messages from the queue.");
        System.out.println("Median Produce Latency (ns): " + medianLatency);

        connection.close();
    }
}