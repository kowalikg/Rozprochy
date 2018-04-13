import com.rabbitmq.client.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeoutException;

public class Technician {

    public static void main(String[] argv) throws IOException, TimeoutException {
        int id = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        String[] bodyParts = Values.generateRandomParts();

        System.out.println("Technician nr " + id + ", accepts " + bodyParts[0] + " and " + bodyParts[1] + ".");
        Channel channel = initialise();

        initialiseDoctorsTechniciansExchange(channel, bodyParts);
        initialiseAdminLogQueue(channel);
        String adminQueueInfo = generateAdminInfoQueue(channel);

        channel.basicQos(1);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");

                String[] request = message.split(":");

                if (request[0].equals("order")){
                    System.out.println("Received from doctor: " + message);
                    sentToDoctor(channel, envelope, request, id);
                    sentLog(channel, request, id);
                }
                else if (request[0].equals("admin")) System.out.println("Received from " + message);

            }
        };

        System.out.println("Waiting for messages...");
        registerConsumption(channel, adminQueueInfo, bodyParts, consumer);

    }

    private static void registerConsumption(Channel channel, String adminQueueInfo, String[] bodyParts, Consumer consumer) throws IOException {
        channel.basicConsume(bodyParts[0], false, consumer);
        channel.basicConsume(bodyParts[1], false, consumer);
        channel.basicConsume(adminQueueInfo, false, consumer);
    }

    private static void sentLog(Channel channel, String[] request, int id) throws IOException {
        String logMessage = "LOG:" + "done:" + id + ":" + request[2] + ":" + request[3];
        channel.basicPublish("", Values.ADMIN_LOG_QUEUE_NAME, null, logMessage.getBytes("UTF-8"));
    }

    private static void sentToDoctor(Channel channel, Envelope envelope, String[] request, int id) throws IOException {
        String response = "done:" + id + ":" + request[2] + ":" + request[3];
        channel.basicPublish(Values.EXCHANGE_DOCTORS_TECHNICIAN, request[1], null, response.getBytes("UTF-8"));
        channel.basicAck(envelope.getDeliveryTag(), false);
        System.out.println("Sent: " + response);
    }

    private static String generateAdminInfoQueue(Channel channel) throws IOException {
        channel.exchangeDeclare(Values.ADMIN_INFO_EXCHANGE, BuiltinExchangeType.FANOUT);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, Values.ADMIN_INFO_EXCHANGE, "");
        return queueName;
    }

    private static void initialiseAdminLogQueue(Channel channel) throws IOException {
        channel.queueDeclare(Values.ADMIN_LOG_QUEUE_NAME, false, false, false, null);
    }

    private static void initialiseDoctorsTechniciansExchange(Channel channel, String[] bodyParts) throws IOException {
        channel.exchangeDeclare(Values.EXCHANGE_DOCTORS_TECHNICIAN, BuiltinExchangeType.DIRECT);

        channel.queueDeclare(bodyParts[0], false, false, false, null);
        channel.queueDeclare(bodyParts[1], false, false, false, null);

        channel.queueBind(bodyParts[0], Values.EXCHANGE_DOCTORS_TECHNICIAN, bodyParts[0]);
        channel.queueBind(bodyParts[1], Values.EXCHANGE_DOCTORS_TECHNICIAN, bodyParts[1]);

    }

    private static Channel initialise() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }
}
