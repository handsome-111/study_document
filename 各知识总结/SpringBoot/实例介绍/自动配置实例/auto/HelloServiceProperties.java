package club.sdms.auto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="hello")
//相当于在application.properties 里的： hello.msg
//作用是将properties转换为对象
public class HelloServiceProperties {
	private static final String MSG = "world";
	private String msg = MSG;
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
