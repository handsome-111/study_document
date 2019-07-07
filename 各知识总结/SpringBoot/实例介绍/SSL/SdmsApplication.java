package club.sdms;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.sdms.auto.HelloService;

@SpringBootApplication	//该例表示数据源自动配置
@RestController		
public class SdmsApplication {
	@Autowired
	public HelloService helloService;
	
	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		tomcat.addAdditionalTomcatConnectors(createHTTPConnector());
		return tomcat;
	}
	private Connector createHTTPConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		//同时启用http（8080）、https（8443）两个端口
		connector.setScheme("http");
		connector.setSecure(false);
		connector.setPort(8080);
		connector.setRedirectPort(8443);
		return connector;
	}
	
	@RequestMapping("/hello")
	public String hello() throws Exception {
		return helloService.sayHello();
	}
	public static void main(String[] args) {
		//关闭banner
		new SpringApplicationBuilder(SdmsApplication.class).run(args);
		System.out.println(11);
	}
}
