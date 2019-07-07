package club.sdms.auto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import club.sdms.auto.HelloService;
import club.sdms.auto.HelloServiceProperties;

@Configuration
@EnableConfigurationProperties(HelloServiceProperties.class)	//将带有@ConfigurationProperties注解的类注入为Spring容器的Bean
@ConditionalOnClass(HelloService.class)
@ConditionalOnProperty(prefix="hello",value="enabled",matchIfMissing=true)		
//enabled 设置为可以在application.rpoperties里设置该属性，matchIfMissing=true表示
public class HelloServiceAutoConfiguration {
	@Autowired
	private HelloServiceProperties helloServiceProperties;
	@Bean
	@ConditionalOnMissingBean(HelloService.class)	//如果不存在HelloService.class这个Bean，则从以下方法注入bean
	public HelloService helloService(){
		HelloService hs = new HelloService();
		hs.setMsg(helloServiceProperties.getMsg());
		return hs;
	}
}
