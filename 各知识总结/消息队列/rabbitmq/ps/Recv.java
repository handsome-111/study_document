package stu.wl.twitter.rabbitmq.ps;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Recv {
	public static final String QUEUE_NAME = "test_queue_fanout_email";
	public static void main(String[] args) throws Exception {
		//获取连接
		Connection conn = ConnectionUtil.getConnection();
		//创建通道
		final Channel channel = conn.createChannel();
		//声明队列
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		//通过通道将队列绑定到指定的交换机上
		channel.queueBind(QUEUE_NAME, Send.EXCHANGENAME, "");
		channel.basicQos(1);
		Consumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String message = new String(body,"utf-8");
				System.out.println("message:"+message);
				channel.basicAck(envelope.getDeliveryTag(), false);
			}
		};
		channel.basicConsume(QUEUE_NAME, false,consumer);
	}
}
