package com.starit.diamond.example.bean;

import com.starit.diamond.properties.PropertyConfigurer;

public class ExampleBean {
	public String getProperties(String key) {
		return PropertyConfigurer.getString(key);
	}
}
