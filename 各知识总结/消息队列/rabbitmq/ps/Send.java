package stu.wl.twitter.rabbitmq.ps;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Send {
	//交换机的名字
	public static final String EXCHANGENAME = "test_exchange_fanout";
	public static void main(String[] args)throws Exception{
		//获取连接
		Connection conn = ConnectionUtil.getConnection();
		//创建通道
		Channel channel = conn.createChannel();
		//声明交换机
		channel.exchangeDeclare(EXCHANGENAME, "fanout");
		
		String message = "you son of bitch";
		//将消息发送到交换机上
		channel.basicPublish(EXCHANGENAME, "", null, message.getBytes());
		
		channel.close();
		conn.close();
	}
}
