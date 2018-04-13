import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Hospital {
    public static void main(String[] argv) throws InterruptedException, IOException, TimeoutException {
        System.out.println("Hospital");

        Channel channel = initialise();

        initialiseHospitalDoctorsQueue(channel);

        initialiseAdminLogQueue(channel);

        String adminQueueLog = generateAdminInfoQueue(channel);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        };
        registerConsumption(channel, consumer, adminQueueLog);
        generatePatients(channel);
    }

    private static void registerConsumption(Channel channel, Consumer consumer, String adminQueueLog) throws IOException {
        channel.basicConsume(adminQueueLog, false, consumer);
    }

    private static void generatePatients(Channel channel) throws IOException, InterruptedException {
        while(true){
            String personNames = Values.generatePerson();
            String ache = Values.generateAche();

            String message = "hospital:" + personNames + ":" + ache;
            sentToDoctors(channel, message);
            sentLog(channel, message);

            Thread.sleep(10000);
        }
    }

    private static void sentLog(Channel channel, String message) throws IOException {
        String logMessage = "LOG:" + message;
        channel.basicPublish("", Values.ADMIN_LOG_QUEUE_NAME, null, logMessage.getBytes("UTF-8"));
    }

    private static void sentToDoctors(Channel channel, String message) throws IOException {
        channel.basicPublish("", Values.DOCTORS_HOSPITAL_QUEUE_NAME, null, message.getBytes());
        System.out.println("Sent: " + message);
    }

    private static void initialiseHospitalDoctorsQueue(Channel channel) throws IOException {
        channel.queueDeclare(Values.DOCTORS_HOSPITAL_QUEUE_NAME, false, false, false, null);
    }
    private static Channel initialise() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }

    private static void initialiseAdminLogQueue(Channel channel) throws IOException {
        channel.queueDeclare(Values.ADMIN_LOG_QUEUE_NAME, false, false, false, null);
    }

    private static String generateAdminInfoQueue(Channel channel) throws IOException {
        channel.exchangeDeclare(Values.ADMIN_INFO_EXCHANGE, BuiltinExchangeType.FANOUT);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, Values.ADMIN_INFO_EXCHANGE, "");
        System.out.println("created queue" + queueName);
        return queueName;
    }
}
