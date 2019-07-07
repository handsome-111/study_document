package stu.wl.twitter.rabbitmq.util;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class Receive {
	public static final String QUEUE_NAME="test_simple_queue";
	public static void main(String[] args) throws Exception {
		//获取连接 
		Connection conn = ConnectionUtil.getConnection();
		//创建通道
		Channel channel = conn.createChannel();
		//创建消费者，并连接指定通道
		Consumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String message  = new String(body,"UTF-8");
				System.out.println("message:"+message);
			}
		};
		
		//监听队列，一旦有消息，就会触发handleDelivery
		channel.basicConsume(QUEUE_NAME	, true,consumer);
	}
}
