package com.starit.diamond.properties;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyConfigurer {

	private static Logger LOGGER = LoggerFactory.getLogger(PropertyConfigurer.class);

	public static Properties props = null;

	public static void load(StringReader reader) {
		if (null == props)
			props = new Properties();
		try {
			props.load(reader);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}

	public static void load(Properties defaultProps) {
		if (null == props) {
			props = new Properties();
			convertProperties(defaultProps);
		} else {
			convertProperties(defaultProps);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void convertProperties(Properties defaultProps) {
		Enumeration propertyNames = defaultProps.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = defaultProps.getProperty(propertyName);
			if (StringUtils.isNotEmpty(propertyName))
				props.setProperty(propertyName, propertyValue);
		}
	}

	public static Object getProperty(String key) {
		if (null == props) {
			return null;
		}
		return props.get(key);
	}

	public static String getValue(String key) {
		Object object = getProperty(key);
		if (null != object) {
			return (String) object;
		}

		LOGGER.warn("配置项为" + key + "的配置未在Diamond中添加或设置的内容为空");
		return null;
	}

	public static String getString(String key) {
		Object object = getProperty(key);
		if (null != object) {
			return (String) object;
		}

		LOGGER.warn("配置项为" + key + "的配置未在Diamond中添加或设置的内容为空");
		return null;
	}

	public static Long getLong(String key) {
		Object object = getProperty(key);
		if (null != object) {
			return Long.valueOf(Long.parseLong(object.toString()));
		}
		LOGGER.warn("配置项为" + key + "的配置未在Diamond中添加或设置的内容为空");
		return null;
	}

	public static Integer getInteger(String key) {
		Object object = getProperty(key);
		if (null != object) {
			return Integer.valueOf(Integer.parseInt(object.toString()));
		}

		LOGGER.warn("配置项为" + key + "的配置未在Diamond中添加或设置的内容为空");
		return null;
	}
}
