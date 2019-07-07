package com.smart.placeholder;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class EncryptPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer{
	private String[] encryptPropNames ={"userName","password"};

	//���ض�����ֵ����ת�����������Ե�ֵ
	protected String convertProperty(String propertyName, String propertyValue) {	
		//�������Ϊ��������
		if(isEncryptProp(propertyName)){
			String decryptValue = DESUtils.getDecryptString(propertyValue);
			System.out.println("���ܵ�������Ϊ"+propertyName+"="+decryptValue);
			return decryptValue;
		}else{
			return propertyValue;
		}
	}
	//�ж������Ƿ���Ҫ���ܣ�Ҳ����˵�жϸ������Ƿ����
	private boolean isEncryptProp(String propertyName){
		for(String encryptPropName:encryptPropNames){
			if(encryptPropName.equals(propertyName)){
				return true;
			}
		}
		return false;
	}


}
