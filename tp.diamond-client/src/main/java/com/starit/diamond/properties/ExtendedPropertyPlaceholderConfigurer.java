package com.starit.diamond.properties;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.starit.diamond.manager.DiamondManager;
import com.starit.diamond.manager.ManagerListener;
import com.starit.diamond.manager.impl.DefaultDiamondManager;

public class ExtendedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private static Logger logger = LoggerFactory.getLogger(ExtendedPropertyPlaceholderConfigurer.class);

	private Properties props;

	private boolean loadDiamond = true;

	public void setLoadDiamond(boolean loadDiamond) {
		this.loadDiamond = loadDiamond;
	}

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
		if (!loadDiamond) {
			super.processProperties(beanFactory, props);
			this.props = props;
			PropertyConfigurer.load(props);
			return;
		}

		String group = props.getProperty("deploy.group");
		String dataId = props.getProperty("deploy.dataId");

		logger.warn("配置项：group=" + group);
		logger.warn("配置项：dataId=" + dataId);

		if (StringUtils.isNotEmpty(group) && StringUtils.isNotEmpty(dataId)) {
			DiamondManager manager = new DefaultDiamondManager(group, dataId, new ManagerListener() {
				public Executor getExecutor() {
					return null;
				}

				public void receiveConfigInfo(String configInfo) {
					// 客户端处理数据的逻辑
					logger.warn("已改动的配置：\n" + configInfo);
					StringReader reader = new StringReader(configInfo);
					try {
						PropertyConfigurer.props.load(reader);
					} catch (IOException e) {
						logger.error("配置合并异常" + e);
					}
				}
			});

			try {
				String configInfo = manager.getAvailableConfigureInfomation(30000);
				logger.warn("配置项内容: \n" + configInfo);
				if (StringUtils.isNotEmpty(configInfo)) {
					StringReader reader = new StringReader(configInfo);
					props.load(reader);
					PropertyConfigurer.load(props);
				} else {
					logger.error("在配置管理中心找不到配置信息");
				}
			} catch (IOException e) {
				logger.error("配置合并异常" + e);
			}
		} else {
			PropertyConfigurer.load(props);
		}
		super.processProperties(beanFactory, props);
		this.props = props;
	}

	public Object getProperty(String key) {
		return props.get(key);
	}
}