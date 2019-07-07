package stu.wl.twitter.rabbitmq.topic;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Recv2 {
	public static final String QUEUE_NAME = "test_exchange_topic2";
	public static void main(String[] args) throws Exception {
		Connection conn = ConnectionUtil.getConnection();
		final Channel channel = conn.createChannel();
		
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		
		//将队列绑定到交换机并指定key
		channel.queueBind(QUEUE_NAME, Send.EXCHANGE_NAME, "goods.#");

		channel.basicQos(1);

		Consumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String message = new String(body,"utf-8");
				System.out.println("message[2]:"+message);
				channel.basicAck(envelope.getDeliveryTag(), false);
			}
		};
		channel.basicConsume(QUEUE_NAME, false,consumer);
	}
}
