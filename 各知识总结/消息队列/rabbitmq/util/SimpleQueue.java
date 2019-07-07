package stu.wl.twitter.rabbitmq.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class SimpleQueue {
	public static final String QUEUE_NAME="test_simple_queue";
	public static void main(String[] args) throws Exception {
		//获取一个连接
		Connection connection = ConnectionUtil.getConnection();
		//实现一条通道
		Channel channel = connection.createChannel();
		//创建队列声明
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		
		String msg = "hello simple";
		
		channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());

		System.out.println("------````send MSG:"+msg);
		
		channel.close();
		connection.close();
	}
}
