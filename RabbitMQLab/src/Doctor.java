import com.rabbitmq.client.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Doctor {

    public static void main(String[] argv) throws IOException, TimeoutException {
        int id = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);

        System.out.println("I am doctor nr " + id);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        new Thread(new DoctorSenderThread(channel, id)).start();
        channel.queueDeclare(Values.DOCTORS_RECIEVER_QUEUE_NAME, false, false, false, null);
        channel.exchangeDeclare(Values.EXCHANGE_DOCTORS_TECHNICIAN, BuiltinExchangeType.DIRECT);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                String parts[] = message.split(":");
                message = "order:" + id + ":" + message;
                channel.basicPublish(Values.EXCHANGE_DOCTORS_TECHNICIAN, parts[1], null, message.getBytes("UTF-8"));
                System.out.println("Sent: " + message);
            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        channel.basicConsume(Values.DOCTORS_RECIEVER_QUEUE_NAME, true, consumer);
    }
}
