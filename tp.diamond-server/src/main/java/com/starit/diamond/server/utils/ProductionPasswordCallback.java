/**
 * Copyright (c) 2012,USTC E-BUSINESS TECHNOLOGY CO.LTD All Rights Reserved.
 */

package com.starit.diamond.server.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.security.auth.callback.PasswordCallback;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.starit.diamond.common.EnvConstant;

/**
 * @author bsli@starit.com.cn
 * @date 2012-6-5 下午9:12:07
 */
public class ProductionPasswordCallback extends PasswordCallback implements InitializingBean {
	
	private static final Logger _logger = LoggerFactory.getLogger(ProductionPasswordCallback.class);

	private static final long serialVersionUID = 1L;
	
	private String productionFilePath = System.getProperties().getProperty("user.home") + "/.druid";
	
	private String password = "";
	
	public ProductionPasswordCallback() {
		super("production", false);
	}

	public ProductionPasswordCallback(String prompt, boolean echoOn) {
		super(prompt, echoOn);
	}

	@Override
	public char[] getPassword() {
		return password.toCharArray();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			Properties properties = PropertiesLoaderUtils.loadAllProperties("META-INF/res/profile.properties");
			String env = properties.getProperty("spring.profiles.active");
			properties.clear();
			properties = PropertiesLoaderUtils.loadAllProperties("META-INF/res/jdbc-" + env + ".properties");
			if(EnvConstant.PRODUCTION.equals(env)) {
				String username = properties.getProperty("jdbc.username");
				properties.clear();
				properties.load(new FileInputStream(productionFilePath));
				
				password = properties.getProperty(username);
			} else {
				password = properties.getProperty("jdbc.password");
			}
			
			_logger.info("运行环境为： {}, 数据库密码为: {}", env, StringUtils.leftPad("", password.length(), "*"));
		} catch (IOException e) {
			_logger.error(e.getMessage(), e);
		}
	}

	public String getProductionFilePath() {
		return productionFilePath;
	}

	public void setProductionFilePath(String productionFilePath) {
		this.productionFilePath = productionFilePath;
	}
}
