import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Hospital {
    public static void main(String[] argv) throws InterruptedException, IOException, TimeoutException {
        System.out.println("Hospital");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(Values.DOCTORS_RECIEVER_QUEUE_NAME, false, false, false, null);

        while(true){
            String personNames = Values.generatePerson();
            String ache = Values.generateAche();
            System.out.println("To hospital comes: " + personNames + " with " + ache + " ache.");
            String message = personNames + ":" + ache;
            channel.basicPublish("", Values.DOCTORS_RECIEVER_QUEUE_NAME, null, message.getBytes());
            Thread.sleep(1000);
        }
    }
}
