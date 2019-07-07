package stu.wl.twitter.rabbitmq.confirm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class Send1 {
	public static final String QUEUE_NAME = "test_queue_confirm1";
	public static void main(String[] args) throws Exception {
		Connection conn = ConnectionUtil.getConnection();
		Channel channel = conn.createChannel();
		//定义队列
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		
		//选择confirm模式
		channel.confirmSelect();
		
		for(int i = 0; i < 10; i++){
			String message = "hello confirm"+i;
			channel.basicPublish("", QUEUE_NAME	, null,message.getBytes());
		}
		//等待确定
		if(!channel.waitForConfirms()){
			System.out.println("message send faild");
		}else{
			System.out.println("message send ok");
		}
	}
}
