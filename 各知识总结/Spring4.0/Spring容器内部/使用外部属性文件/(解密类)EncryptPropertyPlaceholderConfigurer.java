package com.smart.placeholder;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class EncryptPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer{
	private String[] encryptPropNames ={"userName","password"};

	//对特定属性值进行转换，返回属性的值
	protected String convertProperty(String propertyName, String propertyValue) {	
		//如果属性为加密属性
		if(isEncryptProp(propertyName)){
			String decryptValue = DESUtils.getDecryptString(propertyValue);
			System.out.println("加密的属性名为"+propertyName+"="+decryptValue);
			return decryptValue;
		}else{
			return propertyValue;
		}
	}
	//判断属性是否需要解密，也就是说判断该属性是否加密
	private boolean isEncryptProp(String propertyName){
		for(String encryptPropName:encryptPropNames){
			if(encryptPropName.equals(propertyName)){
				return true;
			}
		}
		return false;
	}


}
