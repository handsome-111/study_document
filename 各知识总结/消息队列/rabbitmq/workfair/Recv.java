package stu.wl.twitter.rabbitmq.workfair;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Recv {
	public static final String QUEUE_NAME="test_simple_queue";

	public static void main(String args[])throws Exception{
		//获取连接
		Connection conn = ConnectionUtil.getConnection();
		//在连接里创建一个通道
		final Channel channel = conn.createChannel();
		//声明通道
		channel.queueDeclare(QUEUE_NAME,false,false,false,null);
		
		//每次从队列获取的数量
		channel.basicQos(1);
		
		Consumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String message = new String(body,"UTF-8");
				System.out.println(message);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//ACK：发送确认   DeliveryTag:交货标签
				channel.basicAck(envelope.getDeliveryTag(), false);
			}
		};
		//设置某个客户监听某个队列
		//false：关闭自动应答
		channel.basicConsume(QUEUE_NAME, false, consumer);
	}
}
