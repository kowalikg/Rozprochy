import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Admin {
    public static void main(String[] argv) throws IOException, TimeoutException {
        Channel channel = initialise();
        initialiseAdminLogQueue(channel);
        initialiseAdminInfoExchange(channel);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                if(message.split(":")[0].equals("LOG")) System.out.println(message);
            }
        };

        System.out.println("Waiting for messages...");
        registerConsumption(channel, consumer);

        while(true){
            sendMessage(channel);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static void sendMessage(Channel channel) throws IOException {
        String message = "admin:I am watching you:" + System.currentTimeMillis();
        channel.basicPublish(Values.ADMIN_INFO_EXCHANGE, "", null, message.getBytes("UTF-8"));
        System.out.println("Sent: " + message);
    }

    private static void registerConsumption(Channel channel, Consumer consumer) throws IOException {
        channel.basicConsume(Values.ADMIN_LOG_QUEUE_NAME, false, consumer);
    }

    private static void initialiseAdminInfoExchange(Channel channel) throws IOException {
        channel.exchangeDeclare(Values.ADMIN_INFO_EXCHANGE, BuiltinExchangeType.FANOUT);
    }

    private static void initialiseAdminLogQueue(Channel channel) throws IOException {
        channel.queueDeclare(Values.ADMIN_LOG_QUEUE_NAME, false, false, false, null);
    }

    private static Channel initialise() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }
}
