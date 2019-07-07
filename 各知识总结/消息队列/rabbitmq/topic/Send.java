package stu.wl.twitter.rabbitmq.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Send {
	public static final String EXCHANGE_NAME="test_exchange_topic";
	public static void main(String[] args) throws Exception {
		Connection conn = ConnectionUtil.getConnection();
		Channel channel = conn.createChannel();
		
		//声明交换机
		channel.exchangeDeclare(EXCHANGE_NAME, "topic");
		
		String message = "垃圾货物，删除";
		
		String routingKey = "goods.delete";
		channel.basicPublish(EXCHANGE_NAME,routingKey,null,message.getBytes());
		
		System.out.println("发送信息:"+message);
		
		channel.close();
		conn.close();
	}
}
