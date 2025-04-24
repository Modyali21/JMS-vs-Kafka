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
        TextMessage message = session.createTextMessage(messageContent);

        // Benchmark
        long startTime = System.currentTimeMillis();

            for (int i = 0; i < 10000; i++) {
                long produceStart = System.nanoTime();
                producer.send(message);
                long produceEnd = System.nanoTime();
                responseTimes.add(produceEnd - produceStart);
            }
        Collections.sort(responseTimes);
        long medianResponseTime = responseTimes.get(responseTimes.size() / 2);
        System.out.println("Sent 10000 runs from file content.");
        System.out.println("Median response time of consumer  for 1k messages (ns): " + medianResponseTime);

        connection.close();

}
}
