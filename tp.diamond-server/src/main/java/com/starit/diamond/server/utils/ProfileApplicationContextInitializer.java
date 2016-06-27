/**
 * Copyright (c) 2012,USTC E-BUSINESS TECHNOLOGY CO.LTD All Rights Reserved.
 */

package com.starit.diamond.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author bsli@starit.com.cn
 * @date 2012-6-5 下午7:49:45
 */
public class ProfileApplicationContextInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Logger _logger = LoggerFactory
			.getLogger(ProfileApplicationContextInitializer.class);

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		String profile = ProfileUtil.getValue();

		if ("${profiles.active}".equals(profile.trim())) {
			applicationContext.getEnvironment()
					.setActiveProfiles("development");

			_logger.info("spring profile {}", "development");
		} else {
			applicationContext.getEnvironment().setActiveProfiles(profile);

			_logger.info("spring profile {}", profile);
		}
	}

}