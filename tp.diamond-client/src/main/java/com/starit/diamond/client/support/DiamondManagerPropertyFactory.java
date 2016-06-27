/**
 * Copyright (c) 2012,USTC E-BUSINESS TECHNOLOGY CO.LTD All Rights Reserved.
 */

package com.starit.diamond.client.support;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import com.starit.diamond.manager.DiamondManager;
import com.starit.diamond.manager.ManagerListener;
import com.starit.diamond.manager.impl.DefaultDiamondManager;

/**
 * @author bsli@starit.com.cn
 * @date 2012-5-28 上午10:14:51
 */
public class DiamondManagerPropertyFactory implements FactoryBean<Properties> {
	private static final Log LOG = LogFactory.getLog(DiamondManagerPropertyFactory.class);
	private String dataId;
	private String groupId;

	@Override
	public Properties getObject() throws Exception {
		final Properties properties = new Properties();
		final DiamondManager manager = new DefaultDiamondManager(groupId, dataId, new ManagerListener() {
			public Executor getExecutor() {
				return null;
			}

			public void receiveConfigInfo(String configInfo) {
				try {
					properties.load(new StringReader(configInfo));
				} catch (IOException e) {
					LOG.error("配置文件有错误：" + e.getMessage());
				}
			}

		});
		
		String content = manager.getAvailableConfigureInfomation(1000);
		LOG.info("从 diamond server 加载配置信息成功");
		
		properties.load(new StringReader(content));
		ConfigUtil.setProperties(properties);
		
		return properties;
	}

	@Override
	public Class<Properties> getObjectType() {
		return Properties.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
}
