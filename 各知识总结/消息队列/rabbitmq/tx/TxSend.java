package stu.wl.twitter.rabbitmq.tx;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import stu.wl.twitter.rabbitmq.util.ConnectionUtil;

public class TxSend {
	public static final String QUEUE_NAME = "test_queue_tx";
	public static void main(String[] args) throws Exception {
		Connection conn = ConnectionUtil.getConnection();
		Channel channel = conn.createChannel();
		
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		String message = "hello tx message";
		
		//事务处理
		try{
			//开启事务
			channel.txSelect();
			//发送消息
			channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
			//提交事务
			channel.txCommit();
		}catch(Exception e){
			channel.txRollback();
			System.out.println("send message txRollback");
		}finally{
			channel.close();
			conn.close();
		}
	}
}
