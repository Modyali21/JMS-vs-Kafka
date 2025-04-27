package org.example.JMS;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
public class ResponseTimeConsumer {
    public static void main(String[] args) throws JMSException, IOException, InterruptedException {

        ArrayList<Long> responseTimes = new ArrayList<>();

//        Connect to apache activemq
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("TEST.QUEUE");

        MessageConsumer consumer = session.createConsumer(queue);

        // Benchmark
            for (int i = 0; i < 1000; i++) {
                long consumerStart = System.nanoTime();
                consumer.receive();
                long consumerEnd = System.nanoTime();
                responseTimes.add(consumerEnd - consumerStart);
            }
        Collections.sort(responseTimes);
        long medianResponseTime = responseTimes.get(responseTimes.size() / 2);
        System.out.println("received 1000 messages.");
        System.out.println("Median response time of consumer  for 1k messages (ns): " + medianResponseTime);

        connection.close();

}
}
