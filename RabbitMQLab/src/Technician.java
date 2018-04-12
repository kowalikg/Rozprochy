import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Technician {

    public static void main(String[] argv) throws IOException, TimeoutException {
        String[] bodyParts = Values.generateRandomParts();
        for (String s:bodyParts){
            System.out.println(s);
        }
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();


        // exchange
        channel.exchangeDeclare(Values.EXCHANGE_DOCTORS_TECHNICIAN, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(Values.EXCHANGE_DOCTORS_TECHNICIAN_SENDER, BuiltinExchangeType.DIRECT);

        channel.queueDeclare(bodyParts[0], false, false, false, null);
        channel.queueDeclare(bodyParts[1], false, false, false, null);
        channel.queueBind(bodyParts[0], Values.EXCHANGE_DOCTORS_TECHNICIAN, bodyParts[0]);
        channel.queueBind(bodyParts[1], Values.EXCHANGE_DOCTORS_TECHNICIAN, bodyParts[1]);

        channel.basicQos(1);


        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                String[] request = message.split(":");
                String response = "Done:" + request[1] + ":" + request[2] + ":" + request[3];
                System.out.println(response);
                channel.basicPublish(Values.EXCHANGE_DOCTORS_TECHNICIAN_SENDER, request[1], null, response.getBytes("UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(), false);

            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        channel.basicConsume(bodyParts[0], false, consumer);
        channel.basicConsume(bodyParts[1], false, consumer);

    }
}
