package stu.wl.twitter.rabbitmq.spring;

import org.springframework.stereotype.Component;

@Component("userConsumer")
public class UserConsumer {
	public void listen(String message){
		System.out.println("用户消费者:"+message);
	}
}
