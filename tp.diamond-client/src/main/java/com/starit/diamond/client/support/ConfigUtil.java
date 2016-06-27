/**
 * Copyright (c) 2012,USTC E-BUSINESS TECHNOLOGY CO.LTD All Rights Reserved.
 */

package com.starit.diamond.client.support;

import java.util.Properties;

/**
 * @author bsli@starit.com.cn
 * @date 2012-5-28 上午10:21:16
 */
abstract public class ConfigUtil {
	private static Properties properties;
	
	public static String getValue(String key) {
		return properties.getProperty(key);
	}

	public static Properties getProperties() {
		return properties;
	}

	public static void setProperties(Properties properties) {
		ConfigUtil.properties = properties;
	}
	
}
