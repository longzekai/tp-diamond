/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.starit.diamond.server.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.starit.diamond.common.Constants;
import com.starit.diamond.domain.ConfigInfo;
import com.starit.diamond.domain.Page;
import com.starit.diamond.server.service.ConfigService;
import com.starit.diamond.server.service.UserService;
import com.starit.diamond.server.utils.DiamondUtils;

/**
 * 管理控制器
 * 
 * @author boyan
 * @date 2010-5-6
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

	private static final Log log = LogFactory.getLog(AdminController.class);
	private static final Log updateLog = LogFactory.getLog("updateLog");
	private static final Log deleteLog = LogFactory.getLog("deleteLog");

	int FORBIDDEN_403 = 403;

	@Autowired
	private UserService userService;

	@Autowired
	private ConfigService configService;

	/**
	 * 增加新的配置信息
	 * 
	 * @param request
	 * @param dataId
	 * @param group
	 * @param content
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value="/postConfig", method = RequestMethod.POST)
	public String postConfig(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("dataId") String dataId,
			@RequestParam("group") String group,
			@RequestParam("description") String description,
			@RequestParam("content") String content, ModelMap modelMap) {
		response.setCharacterEncoding("GBK");

		boolean checkSuccess = true;
		String errorMessage = "参数错误";
		if (!StringUtils.hasLength(dataId)
				|| DiamondUtils.hasInvalidChar(dataId.trim())) {
			checkSuccess = false;
			errorMessage = "无效的DataId";
		}
		if (!StringUtils.hasLength(group)
				|| DiamondUtils.hasInvalidChar(group.trim())) {
			checkSuccess = false;
			errorMessage = "无效的分组";
		}
		if (!StringUtils.hasLength(content)) {
			checkSuccess = false;
			errorMessage = "无效的内容";
		}
		if (!checkSuccess) {
			modelMap.addAttribute("message", errorMessage);
			try {
				response.sendError(FORBIDDEN_403, errorMessage);
			} catch (IOException ioe) {
				log.error(ioe.getMessage(), ioe.getCause());
			}
			return "/admin/confignew";
		}
		String userName = (String) request.getSession().getAttribute("user");
		this.configService.addConfigInfo(dataId, group, userName, content, description);

		request.getSession().setAttribute("message", "提交成功!");
		return "redirect:" + listConfig(request, response, null, null, 1, 10, modelMap);
	}

	@RequestMapping(value="/delete", method = RequestMethod.GET)
	public String deleteConfig(HttpServletRequest request,
			HttpServletResponse response, @RequestParam("id") long id) {
		// 根据id查询出该条数据
		ConfigInfo configInfo = this.configService.getPersistService().findConfigInfoByID(id);
		if (configInfo == null) {
			deleteLog.warn("删除失败, 要删除的数据不存在, id=" + id);
			request.getSession().setAttribute("message", "删除失败, 要删除的数据不存在, id=" + id);
			return "redirect:/admin/config";
		}
		String dataId = configInfo.getDataId();
		String group = configInfo.getGroup();
		String content = configInfo.getContent();
		String sourceIP = this.getRemoteIP(request);
		// 删除数据
		this.configService.removeConfigInfo(id);
		// 记录删除日志, AOP方式的记录不会记录dataId等信息, 所以在这里再次记录
		deleteLog.warn("数据删除成功\ndataId=" + dataId + "\ngroup=" + group + "\ncontent=\n" + content + "\nsrc ip=" + sourceIP);
		request.getSession().setAttribute("message", "删除成功!");

		return "redirect:/admin/config";
	}

	/**
	 * 更改配置信息
	 * 
	 * @param request
	 * @param dataId
	 * @param group
	 * @param content
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value="/updateConfig", method = RequestMethod.POST)
	public String updateConfig(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("dataId") String dataId,
			@RequestParam("group") String group,
			@RequestParam("description") String description,
			@RequestParam("content") String content, ModelMap modelMap) {
		response.setCharacterEncoding(Constants.ENCODE);

		String remoteIp = getRemoteIP(request);

		String userName = (String) request.getSession().getAttribute("user");
		ConfigInfo configInfo = new ConfigInfo(dataId, group, userName, content, description);
		boolean checkSuccess = true;
		String errorMessage = "参数错误";
		if (!StringUtils.hasLength(dataId)
				|| DiamondUtils.hasInvalidChar(dataId.trim())) {
			checkSuccess = false;
			errorMessage = "无效的DataId";
		}
		if (!StringUtils.hasLength(group)
				|| DiamondUtils.hasInvalidChar(group.trim())) {
			checkSuccess = false;
			errorMessage = "无效的分组";
		}
		if (!StringUtils.hasLength(content)) {
			checkSuccess = false;
			errorMessage = "无效的内容";
		}
		if (!checkSuccess) {
			modelMap.addAttribute("message", errorMessage);
			modelMap.addAttribute("configInfo", configInfo);
			return "/admin/detailConfig";
		}

		// 查数据,目的是为了在日志中记录被更新的数据的内容
		ConfigInfo oldConfigInfo = this.configService.findConfigInfo(dataId,
				group);
		if (oldConfigInfo == null) {
			updateLog.warn("更新数据出错,要更新的数据不存在, dataId=" + dataId + ",group="
					+ group);
			modelMap.addAttribute("message", "更新数据出错, 要更新的数据不存在, dataId="
					+ dataId + ",group=" + group);
			return listConfig(request, response, dataId, group, 1, 20, modelMap);
		}
		String oldContent = oldConfigInfo.getContent();

		this.configService.updateConfigInfo(dataId, group, content, userName, description);

		// 记录更新日志
		updateLog.warn("更新数据成功\ndataId=" + dataId + "\ngroup=" + group
				+ "\noldContent=\n" + oldContent + "\nnewContent=\n" + content
				+ "\nsrc ip=" + remoteIp);
		request.getSession().setAttribute("message", "提交成功!");
		return "redirect:" + listConfig(request, response, null, null, 1, 10, modelMap);
	}

	/**
	 * 模糊查询配置信息
	 * 
	 * @param request
	 * @param dataId
	 * @param group
	 * @param pageNo
	 * @param pageSize
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value="/config", method = RequestMethod.GET)
	public String listConfig(HttpServletRequest request,
			HttpServletResponse response, String dataId, String group, 
			@RequestParam(defaultValue="1", required=false) int pageNo, 
			@RequestParam(defaultValue="10", required=false) int pageSize, ModelMap modelMap) {
		String userName = (String) request.getSession().getAttribute("user");
		Page<ConfigInfo> page = this.configService.findConfigInfoLike(pageNo,
				pageSize, group, dataId, userName);
		
		modelMap.addAttribute("page", page);
		modelMap.addAttribute("dataId", dataId);
		modelMap.addAttribute("group", group);
		modelMap.addAttribute("method", "listConfigLike");
		return "/admin/config";
	}

	/**
	 * 查看配置信息详情
	 * 
	 * @param request
	 * @param dataId
	 * @param group
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value="/detailConfig", method = RequestMethod.GET)
	public String getConfigInfo(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("dataId") String dataId,
			@RequestParam("group") String group, ModelMap modelMap) {
		dataId = dataId.trim();
		group = group.trim();
		ConfigInfo configInfo = this.configService
				.findConfigInfo(dataId, group);
		modelMap.addAttribute("configInfo", configInfo);
		return "/admin/configedit";
	}

	/**
	 * 展示所有用户
	 * 
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value="/listUser", method = RequestMethod.GET)
	public String listUser(HttpServletRequest request,
			HttpServletResponse response, ModelMap modelMap) {
		Map<String, String> userMap = this.userService.getAllUsers();
		modelMap.addAttribute("userMap", userMap);
		return "/admin/listUser";
	}

	/**
	 * 添加用户
	 * 
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value="/addUser", method = RequestMethod.POST)
	public String addUser(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("userName") String userName,
			@RequestParam("password") String password, ModelMap modelMap) {
		if (!StringUtils.hasLength(userName)
				|| DiamondUtils.hasInvalidChar(userName.trim())) {
			request.getSession().setAttribute("message", "无效的用户名");
			return "redirect:" + listUser(request, response, modelMap);
		}
		if (!StringUtils.hasLength(password)
				|| DiamondUtils.hasInvalidChar(password.trim())) {
			request.getSession().setAttribute("message", "无效的密码");
			return "redirect:/admin/usernew";
		}
		if (this.userService.addUser(userName, password))
			request.getSession().setAttribute("message", "添加成功!");
		else
			request.getSession().setAttribute("message", "添加失败!");
		return "redirect:" + listUser(request, response, modelMap);
	}

	/**
	 * 删除用户
	 * 
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value="/deleteUser", method = RequestMethod.GET)
	public String deleteUser(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("userName") String userName, ModelMap modelMap) {
		if (!StringUtils.hasLength(userName)
				|| DiamondUtils.hasInvalidChar(userName.trim())) {
			request.getSession().setAttribute("message", "无效的用户名");
			return "redirect:" + listUser(request, response, modelMap);
		}
		if (this.userService.removeUser(userName)) {
			request.getSession().setAttribute("message", "删除成功!");
		} else {
			request.getSession().setAttribute("message", "删除失败!");
		}
		return "redirect:" + listUser(request, response, modelMap);
	}

	/**
	 * 更改密码
	 * 
	 * @param userName
	 * @param password
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value="/changePassword", method = RequestMethod.POST)
	public String changePassword(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("userName") String userName,
			@RequestParam("password") String password, ModelMap modelMap) {

		userName = userName.trim();
		password = password.trim();

		if (!StringUtils.hasLength(userName)
				|| DiamondUtils.hasInvalidChar(userName.trim())) {
			request.getSession().setAttribute("message", "无效的用户名");
			return "redirect:" + listUser(request, response, modelMap);
		}
		if (!StringUtils.hasLength(password)
				|| DiamondUtils.hasInvalidChar(password.trim())) {
			request.getSession().setAttribute("message", "无效的新密码");
			return "redirect:" + listUser(request, response, modelMap);
		}
		if (this.userService.updatePassword(userName, password)) {
			request.getSession().setAttribute("message", "更改成功,下次登录请用新密码！");
		} else {
			request.getSession().setAttribute("message", "更改失败!");
		}
		return "redirect:" + listUser(request, response, modelMap);
	}
	
	/**
	 * 重新加载用户信息
	 * 
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value="/reloadUser", method = RequestMethod.GET)
	public String reloadUser(HttpServletRequest request,
			HttpServletResponse response, ModelMap modelMap) {
		this.userService.loadUsers();
		request.getSession().setAttribute("message", "加载成功!");
		return "redirect:" + listUser(request, response, modelMap);
	}

	private String getRemoteIP(HttpServletRequest request) {
		String remoteIP = request.getRemoteAddr();
		if (remoteIP.equals("127.0.0.1")) {
			remoteIP = request.getHeader("X-Real-IP");
		}
		return remoteIP;
	}

}
