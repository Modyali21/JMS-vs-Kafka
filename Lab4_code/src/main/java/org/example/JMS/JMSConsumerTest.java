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
        ArrayList<Long> responseTimes = new ArrayList<>();

        //        preparing the throughput parameters
        int throughput = 1000; // messages per second
        long periodNs = 1_000_000_000L / throughput; // nanoseconds per message
        long adjustedSleepNs = (long)(periodNs * 0.8); // account for thread switching



        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("TEST.QUEUE");

        MessageConsumer consumer = session.createConsumer(queue);

        // Benchmark
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {

            long consumeStart = System.nanoTime();
            Message msg = consumer.receive();
            long consumeEnd = System.nanoTime();
            responseTimes.add(consumeEnd - consumeStart);

            // Extract the timestamp property from the message
            long sentTimestamp = msg.getLongProperty("timestamp");
            long receiveTimestamp = System.nanoTime(); // Current time when message is received

            // Calculate latency and store it
            long latency = receiveTimestamp - sentTimestamp;
            latencies.add(latency);

            // Sleep to throttle
            long sleepTime = adjustedSleepNs - (System.nanoTime() - consumeStart);
            if (sleepTime > 0) {
                TimeUnit.NANOSECONDS.sleep(sleepTime);
            }
//            System.out.println("Consume Latency (ns): " + (consumeEnd - consumeStart));
        }
        long totalTime = System.currentTimeMillis() - startTime;

        Collections.sort(latencies);
        Collections.sort(responseTimes);
        long medianResponseTime = responseTimes.get(responseTimes.size() / 2);
        long medianLatency = latencies.get(latencies.size() / 2);

        System.out.println("Consumed 1000 messages from the queue.");
        System.out.println("Median response time for 1k messages (ns): " + medianResponseTime);
        System.out.println("Median Produce Latency (ns): " + medianLatency);
        System.out.println("Total Consume Time (ms): " + totalTime);
        System.out.println("Throughput (msg/s): " + (1000 / (totalTime / 1000.0)));

        connection.close();
    }
}
