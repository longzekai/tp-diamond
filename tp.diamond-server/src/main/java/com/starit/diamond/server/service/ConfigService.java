/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.starit.diamond.server.service;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.starit.diamond.common.Constants;
import com.starit.diamond.domain.ConfigInfo;
import com.starit.diamond.domain.Page;
import com.starit.diamond.md5.MD5;
import com.starit.diamond.server.exception.ConfigServiceException;
import com.starit.diamond.server.utils.DiamondUtils;
import com.starit.diamond.server.utils.SystemConfig;

@Service
public class ConfigService {

	private static final Log log = LogFactory.getLog(ConfigService.class);

	@Autowired
	private DiskService diskService;

	@Autowired
	private PersistService persistService;

	@Autowired
	private NotifyService notifyService;

	/**
	 * content的MD5的缓存,key为group/dataId，value为md5值
	 */
	private final ConcurrentHashMap<String, String> contentMD5Cache = new ConcurrentHashMap<String, String>();

	public void updateMD5Cache(ConfigInfo configInfo) {
		this.contentMD5Cache.put(
				generateMD5CacheKey(configInfo.getDataId(),
						configInfo.getGroup()),
				MD5.getInstance().getMD5String(configInfo.getContent()));
	}

	public String getContentMD5(String dataId, String group) {
		String key = generateMD5CacheKey(dataId, group);
		String md5 = this.contentMD5Cache.get(key);
		if (md5 == null) {
			synchronized (this) {
				// 二重检查
				md5 = this.contentMD5Cache.get(key);
				if (md5 == null) {
					return null;
				} else
					return md5;
			}
		} else
			return md5;
	}

	public String getConfigInfoPath(String dataId, String address, String group) {
		return generatePath(dataId, group);
	}

	String generateMD5CacheKey(String dataId, String group) {
		String key = group + "/" + dataId;
		return key;
	}

	String generatePath(String dataId, final String group) {
		if (!StringUtils.hasLength(dataId)
				|| StringUtils.containsWhitespace(dataId))
			throw new IllegalArgumentException("无效的dataId");

		if (!StringUtils.hasLength(group)
				|| StringUtils.containsWhitespace(group))
			throw new IllegalArgumentException("无效的group");
		String fnDataId = SystemConfig.encodeDataIdForFNIfUnderWin(dataId);
		StringBuilder sb = new StringBuilder("/");
		sb.append(Constants.BASE_DIR).append("/");
		sb.append(group).append("/");
		sb.append(fnDataId);
		return sb.toString();
	}

	/**
	 * 根据dataId和group查找配置信息
	 * 
	 * @param dataId
	 * @param group
	 * @return
	 */
	public ConfigInfo findConfigInfo(String dataId, String group) {
		if (!StringUtils.hasLength(dataId)
				|| StringUtils.containsWhitespace(dataId))
			throw new IllegalArgumentException("无效的dataId");

		if (!StringUtils.hasLength(group)
				|| StringUtils.containsWhitespace(group))
			throw new IllegalArgumentException("无效的group");
		return persistService.findConfigInfo(dataId, group);
	}

	/**
	 * 根据ID删除GroupInfo
	 * 
	 * @param id
	 */
	public void removeConfigInfo(long id) {
		checkOperation("removeConfigInfo");
		try {
			ConfigInfo configInfo = this.persistService.findConfigInfoByID(id);
			this.diskService.removeConfigInfo(configInfo);
			this.contentMD5Cache.remove(generateMD5CacheKey(
					configInfo.getDataId(), configInfo.getGroup()));

			this.persistService.removeConfigInfoByID(id);
			// 通知其他节点
			this.notifyService.notifyConfigInfoChange(configInfo.getDataId(),
					configInfo.getGroup());

		} catch (Exception e) {
			log.error("删除配置信息错误", e);
			throw new ConfigServiceException(e);
		}
	}

	private void checkOperation(String operation) {
		if (SystemConfig.isOfflineMode()) {
			String msg = "OFFLINE模式，不支持的操作:" + operation + "";
			throw new UnsupportedOperationException(msg);
		}
	}

	/**
	 * 添加配置信息, 并将时间戳、源IP和源用户添加到数据库表中
	 * 
	 * @param dataId
	 * @param group
	 * @param content
	 * @param srcIp
	 * @param srcUser
	 */
	public void addConfigInfo(String dataId, String group, String userName, String content, String description) {
		checkOperation("addConfigInfo");
		checkParameter(dataId, group, content);
		ConfigInfo configInfo = new ConfigInfo(dataId, group, userName, content, description);

		// 获取当前时间
		Timestamp currentTime = DiamondUtils.getCurrentTime();
		// 写的顺序: 数据库、内存、磁盘
		try {
			persistService.addConfigInfo(currentTime, configInfo);
			// 切记更新缓存
			this.contentMD5Cache.put(generateMD5CacheKey(dataId, group),
					configInfo.getMd5());
			diskService.saveToDisk(configInfo);
			// 通知其他节点
			this.notifyService.notifyConfigInfoChange(configInfo.getDataId(),
					configInfo.getGroup());
		} catch (Exception e) {
			log.error("保存ConfigInfo失败", e);
			throw new ConfigServiceException(e);
		}
	}

	/**
	 * 更新配置信息
	 * 
	 * @param dataId
	 * @param group
	 * @param content
	 */
	public void updateConfigInfo(String dataId, String group, String content, String userName, String description) {
		checkOperation("updateConfigInfo");
		checkParameter(dataId, group, content);
		ConfigInfo configInfo = new ConfigInfo(dataId, group, userName, content, description);

		Timestamp currentTime = DiamondUtils.getCurrentTime();
		// 先更新数据库，再更新磁盘
		try {
			persistService.updateConfigInfo(currentTime, configInfo);
			// 切记更新缓存
			this.contentMD5Cache.put(generateMD5CacheKey(dataId, group),
					configInfo.getMd5());
			diskService.saveToDisk(configInfo);
			// 通知其他节点
			this.notifyService.notifyConfigInfoChange(configInfo.getDataId(),
					configInfo.getGroup());
		} catch (Exception e) {
			log.error("保存ConfigInfo失败", e);
			throw new ConfigServiceException(e);
		}
	}

	/**
	 * 删除ConfigInfo
	 * 
	 * @param dataId
	 * @param group
	 */
	public void removeConfigInfo(String dataId, String group) {
		checkParameter(dataId, group);
		try {
			this.contentMD5Cache.remove(generateMD5CacheKey(dataId, group));
			diskService.removeConfigInfo(dataId, group);
			persistService.removeConfigInfo(dataId, group);
			this.notifyService.notifyConfigInfoChange(dataId, group);
		} catch (Exception e) {
			log.error("保存ConfigInfo失败", e);
			throw new ConfigServiceException(e);
		}
	}

	/**
	 * 将配置信息从数据库加载到磁盘
	 * 
	 * @param id
	 */
	public void loadConfigInfoToDisk(String dataId, String group) {
		try {
			ConfigInfo configInfo = this.persistService.findConfigInfo(dataId,
					group);
			if (configInfo != null) {
				this.contentMD5Cache.put(generateMD5CacheKey(dataId, group),
						configInfo.getMd5());
				this.diskService.saveToDisk(configInfo);
			} else {
				// 删除文件
				this.contentMD5Cache.remove(generateMD5CacheKey(dataId, group));
				this.diskService.removeConfigInfo(dataId, group);
			}
		} catch (Exception e) {
			log.error("保存ConfigInfo到磁盘失败", e);
			throw new ConfigServiceException(e);
		}
	}

	private void checkParameter(String dataId, String group, String content) {
		if (!StringUtils.hasLength(dataId)
				|| StringUtils.containsWhitespace(dataId))
			throw new ConfigServiceException("无效的dataId");

		if (!StringUtils.hasLength(group)
				|| StringUtils.containsWhitespace(group))
			throw new ConfigServiceException("无效的group");

		if (!StringUtils.hasLength(content))
			throw new ConfigServiceException("无效的content");
	}

	/**
	 * 分页模糊查找配置信息
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param group
	 * @param dataId
	 * @return
	 */
	public Page<ConfigInfo> findConfigInfoLike(final int pageNo,
			final int pageSize, final String group, final String dataId, String userName) {
		return this.persistService.findConfigInfo(pageNo, pageSize, dataId,
				group, userName);
	}

	private void checkParameter(String dataId, String group) {
		if (!StringUtils.hasLength(dataId)
				|| DiamondUtils.hasInvalidChar(dataId.trim()))
			throw new ConfigServiceException("无效的dataId");

		if (!StringUtils.hasLength(group)
				|| DiamondUtils.hasInvalidChar(group.trim()))
			throw new ConfigServiceException("无效的group");
	}

	public DiskService getDiskService() {
		return diskService;
	}

	public void setDiskService(DiskService diskService) {
		this.diskService = diskService;
	}

	public PersistService getPersistService() {
		return persistService;
	}

	public void setPersistService(PersistService persistService) {
		this.persistService = persistService;
	}

	public NotifyService getNotifyService() {
		return notifyService;
	}

	public void setNotifyService(NotifyService notifyService) {
		this.notifyService = notifyService;
	}

}
