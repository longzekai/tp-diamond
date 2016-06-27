/**
 * Copyright (c) 2012,USTC E-BUSINESS TECHNOLOGY CO.LTD All Rights Reserved.
 */

package com.starit.diamond.server.utils;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * @author bsli@starit.com.cn
 * @date 2012-5-26 上午10:58:54
 */
public class ProfileUtil {
	private static String profile = "";
	private static String dbType = ""; 
	
	static {
		try {
			ClassPathResource resource = new ClassPathResource("META-INF/res/profile.properties");
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			profile = properties.getProperty("spring.profiles.active");
			
			resource = new ClassPathResource("META-INF/res/jdbc-"+profile+".properties");
			properties = PropertiesLoaderUtils.loadProperties(resource);
			String jdbcurl = properties.getProperty("jdbc.url");
			

	        if (jdbcurl.startsWith("jdbc:mysql:")) {
	        	dbType = "mysql";
	        } else if (jdbcurl.startsWith("jdbc:oracle:")) {
	        	dbType = "oracle";
	        }
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getValue() {
		return profile;
	}
	
	public static String getDbType() {
		return dbType;
	}
}
