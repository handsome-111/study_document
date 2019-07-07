package stu.wl.twitter.rabbitmq.tx;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class TxRecv {
	public static final String QUEUE_NAME = "test_queue_tx";
	public static void main(String[] args) throws Exception {
		//获取连接
		Connection conn = ConnectionUtil.getConnection();
		//开启通道
		final Channel channel = conn.createChannel();
		//定义队列
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		channel.basicQos(1);
		Consumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String message = new String(body,"utf-8");
				System.out.println("获取的消息："+message);
				channel.basicAck(envelope.getDeliveryTag(), false);
			}
		};
		channel.basicConsume(QUEUE_NAME, false, consumer);
	}
}



