package org.example.JMS;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class ResponseTimeProducer {
    public static void main(String[] args) throws JMSException, IOException, InterruptedException {

//         === Read content from file ===
        String filePath = "message.txt";  // or use an absolute path like "C:/files/message.txt"
        String messageContent = new String(Files.readAllBytes(Paths.get(filePath)));


        ArrayList<Long> responseTimes = new ArrayList<>();

//        Connect to apache activemq
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("TEST.QUEUE");
        MessageProducer producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = session.createTextMessage(messageContent);
        // Benchmark
        for (int run = 0; run < 1000; run++) {
            long produceStart = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                producer.send(message);
            }
            long produceEnd = System.nanoTime();
            responseTimes.add(produceEnd - produceStart);
        }
        Collections.sort(responseTimes);
        long medianResponseTime = responseTimes.get(responseTimes.size() / 2);
        System.out.println("Sent 10000 runs from file content.");
        System.out.println("Median response time for 1k messages (ms): " + medianResponseTime/1_000_000);

        connection.close();
    }
}
