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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.starit.diamond.server.service.UserService;


/**
 * 登录登出控制器
 * 
 * @author boyan
 * @date 2010-5-6
 */
@Controller
public class SecurityController {
    @Autowired
    private UserService userService;
    
    @RequestMapping(value="/login",method = RequestMethod.POST)
    public String login(HttpServletRequest request, String username, String password) {
        if (userService.login(username, password)) {
        	request.getSession().removeAttribute("message");
            request.getSession().setAttribute("user", username);
            
            request.getSession().removeAttribute("message");
            return "redirect:/admin/index";
        } else {
        	request.getSession().setAttribute("message", "登录失败，用户名密码不匹配");
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/";
    }

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
