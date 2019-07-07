package stu.wl.twitter.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Send {
	public static final String EXCHANGE_NAME="test_exchange_direct";
	public static void main(String[] args) throws Exception {
		Connection conn = ConnectionUtil.getConnection();
		Channel channel = conn.createChannel();
		
		//声明交换机
		channel.exchangeDeclare(EXCHANGE_NAME, "direct");
		
		String message = "you are aaaa son of bitch";
		
		String routingKey = "error";
		channel.basicPublish(EXCHANGE_NAME,routingKey,null,message.getBytes());
		
		System.out.println("发送信息:"+message);
		
		channel.close();
		conn.close();
	}
}
