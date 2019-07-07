package stu.wl.twitter.rabbitmq.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ConnectionUtil {
	/**
	 * 获取MQ连接
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection()throws Exception{
		//定义一个连接工厂
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("132.232.1.147");
		//AMQP协议端口
		factory.setPort(5672);
		//vhost
		factory.setVirtualHost("/twitter");
		//设置用户名和密码
		factory.setUsername("admin");
		factory.setPassword("whj13379959770");
		
		return factory.newConnection();
	}
}
