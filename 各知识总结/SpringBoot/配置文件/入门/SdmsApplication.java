package club.sdms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController		//控制层
public class SdmsApplication {
	//只需要在网址中输入：localhost:8080/hello  即可 输出 		你好，世界
	@RequestMapping("/hello")
	public String hello(){
		return "你好，世界";
	}
	public static void main(String[] args) {
		System.out.println("11");
		SpringApplication.run(SdmsApplication.class, args);
	}
}
