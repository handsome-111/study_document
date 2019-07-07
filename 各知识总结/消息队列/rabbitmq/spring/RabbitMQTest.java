package stu.wl.twitter.rabbitmq.spring;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RabbitMQTest {
	public static void main(String[] args)  {
		ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("/configuration/applicationContext.xml");
		RabbitTemplate template = (RabbitTemplate) app.getBean(RabbitTemplate.class);
		String message = "you son of bitch";
		System.out.println("xxx");
		int i = 0;
		while(i<1000){
			i++;
			template.convertAndSend(message);
		}
	}
}
