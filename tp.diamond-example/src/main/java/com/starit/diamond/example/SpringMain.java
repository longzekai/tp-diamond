package com.starit.diamond.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.starit.diamond.example.bean.ExampleBean;
import com.starit.diamond.example.util.SpringHelper;

public class SpringMain {
	public static void main(String[] args) {
		ApplicationContext cotext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		ExampleBean exampleBean = (ExampleBean) SpringHelper.getBean("exampleBean");
		System.out.println(exampleBean.getProperties("1"));;
	}
}
