import com.rabbitmq.client.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeoutException;

public class Doctor {

    public static void main(String[] argv) throws IOException, TimeoutException {
        int id = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);

        System.out.println("Doctor nr " + id);

        Channel channel = initialise();

        initialiseAdminLogQueue(channel);
        String adminQueueInfo = generateAdminInfoQueue(channel);

        initialiseHospitalDoctorsQueue(channel);
        String doctorsTechnicianQueueName = generateDoctorsTechniciansExchange(channel, id);
        System.out.println(doctorsTechnicianQueueName);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");

                String parts[] = message.split(":");

                if (parts[0].equals("hospital")) {
                    System.out.println("Received from hospital: " + message);
                    sentToTechnician(parts, id, channel);
                    sentLog(channel, parts, id);
                }
                else if (parts[0].equals("done")){
                    System.out.println("Received from technician: " + message);
                }
                else if (parts[0].equals("admin")) System.out.println("Received from " + message);
            }
        };

        System.out.println("Waiting for messages...");
        registerConsumption(channel, adminQueueInfo, doctorsTechnicianQueueName, consumer);
    }

    private static void registerConsumption(Channel channel, String adminQueueInfo, String doctorsTechnicianQueueName, Consumer consumer) throws IOException {
        channel.basicConsume(Values.DOCTORS_HOSPITAL_QUEUE_NAME, false, consumer);
        channel.basicConsume(adminQueueInfo, false, consumer);
        channel.basicConsume(doctorsTechnicianQueueName, false, consumer);
    }

    private static void sentToTechnician(String[] parts, int id, Channel channel) throws IOException {
        String message = "order:" + id + ":" + parts[1] + ":" + parts[2];
        channel.basicPublish(Values.EXCHANGE_DOCTORS_TECHNICIAN, parts[2], null, message.getBytes("UTF-8"));
        System.out.println("Sent: " + message);
    }
    private static void sentLog(Channel channel, String[] request, int id) throws IOException {
        String logMessage = "LOG:" + "order:" + id + ":" + request[1] + ":" + request[2];
        channel.basicPublish("", Values.ADMIN_LOG_QUEUE_NAME, null, logMessage.getBytes("UTF-8"));
    }

    private static void initialiseHospitalDoctorsQueue(Channel channel) throws IOException {
        channel.queueDeclare(Values.DOCTORS_HOSPITAL_QUEUE_NAME, false, false, false, null);
    }

    private static String generateDoctorsTechniciansExchange(Channel channel, int id) throws IOException {
        channel.exchangeDeclare(Values.EXCHANGE_DOCTORS_TECHNICIAN, BuiltinExchangeType.DIRECT);
        String doctorsTechnicianQueueName = channel.queueDeclare().getQueue();
        channel.queueBind(doctorsTechnicianQueueName, Values.EXCHANGE_DOCTORS_TECHNICIAN, String.valueOf(id));
        System.out.println("created doctor queue" + doctorsTechnicianQueueName);
        return doctorsTechnicianQueueName;
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
        System.out.println("created admin queue" + queueName);
        return queueName;
    }
}
