import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Hospital {
    public static void main(String[] argv) throws InterruptedException, IOException, TimeoutException {
        System.out.println("Hospital");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(Values.DOCTORS_HOSPITAL_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(Values.ADMIN_LOG_QUEUE_NAME, false, false, false, null);

        channel.exchangeDeclare(Values.ADMIN_INFO_EXCHANGE, BuiltinExchangeType.FANOUT);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, Values.ADMIN_INFO_EXCHANGE, "");
        System.out.println("created queue: " + queueName);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                //if(message.split(":")[0].equals("LOG"))
                System.out.println("Received: " + message);

            }
        };
        channel.basicConsume(queueName, true, consumer);

        while(true){
            String personNames = Values.generatePerson();
            String ache = Values.generateAche();

            String message = "hospital:" + personNames + ":" + ache;
            System.out.println(message);

            channel.basicPublish("", Values.DOCTORS_HOSPITAL_QUEUE_NAME, null, message.getBytes());
            String logMessage = "LOG:" + message;
            channel.basicPublish("", Values.ADMIN_LOG_QUEUE_NAME, null, logMessage.getBytes("UTF-8"));
            Thread.sleep(10000);
        }
    }
}
