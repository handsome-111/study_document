package stu.wl.twitter.rabbitmq.workfair;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Send {
	public static final String QUEUE_NAME="test_simple_queue";
	public static void main(String[] args) throws Exception {
		//获取连接
		Connection conn = ConnectionUtil.getConnection();
		//实现一条通道
		Channel channel = conn.createChannel();
		//声明一个队列（将队列放到通道里）
		channel.queueDeclare(QUEUE_NAME	, false, false, false, null);
		
		/**
		 * 每个生产者 发送确认消息之前，消息列队不发送下一个消息到消费者，一次只处理一个消息，
		 * 
		 * 限制发送同一个消费者 不得超过一条消息
		 */
		//int prefetchCount = 1;
		//channel.basicQos(prefetchCount);
		
		for(int i = 0 ; i < 50; i++){
			String message = "hello:"+i+i;
			System.out.println("message:"+message);
			channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
		}
		channel.close();
		conn.close();
	}
}
