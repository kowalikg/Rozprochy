import com.rabbitmq.client.*;

import java.io.IOException;

public class DoctorSenderThread implements Runnable{
    private Channel channel;
    private int id;

    public DoctorSenderThread(Channel channel, int id) {
        this.channel = channel;
        this.id = id;
    }

    @Override
    public void run() {
        String queueName = null;
        String key = String.valueOf(id);
        System.out.println(key);
        try {
            channel.exchangeDeclare(Values.EXCHANGE_DOCTORS_TECHNICIAN_SENDER, BuiltinExchangeType.DIRECT);
            queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, Values.EXCHANGE_DOCTORS_TECHNICIAN_SENDER, key);
            System.out.println("created queue: " + queueName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);

            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        try {
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
