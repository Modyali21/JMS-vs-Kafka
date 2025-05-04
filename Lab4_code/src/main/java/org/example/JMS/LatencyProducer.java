package org.example.JMS;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LatencyProducer {
    public static void main(String[] args) throws JMSException, IOException, InterruptedException {

//         === Read content from file ===
        String filePath = "message.txt";  // or use an absolute path like "C:/files/message.txt"
        String messageContent = new String(Files.readAllBytes(Paths.get(filePath)));

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
        for (int i = 0; i < 10000; i++) {
            long timestamp = System.nanoTime();  // Capture timestamp of when message was sent
            message.setLongProperty("timestamp", timestamp); // Add timestamp to message
            producer.send(message);
        }
        System.out.println("Sent 10000 messages from file content.");
        connection.close();
    }
}