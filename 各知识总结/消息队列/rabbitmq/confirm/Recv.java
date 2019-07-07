package stu.wl.twitter.rabbitmq.confirm;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Recv {
	public static final String QUEUE_NAME = "test_queue_confirm4";
	public static void main(String[] args) throws Exception {
		Connection conn = ConnectionUtil.getConnection();
		final Channel channel = conn.createChannel();
		channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		channel.basicQos(1);
		channel.basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String message = new String(body,"UTF-8");
				System.out.println("收到的消息:"+message);
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//channel.basicAck(envelope.getDeliveryTag(), false);
			}
		});
	}

}
