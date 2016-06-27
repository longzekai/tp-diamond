/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.starit.diamond.utils;
/**
 * 用来处理协议相关的操作
 * @author zhidao
 * @version 1.0 2011/05/03
 *
 */
public class Protocol {
	/**
	 * 解析类于2.0.4(major.minor.bug-fix这样的版本为数字)
	 * @param version
	 * @return
	 */
	public static int getVersionNumber(String version) {
		if(version==null)return -1;
		
		String[] vs = version.split("\\.");
    	int sum = 0;
    	for(int i=0;i<vs.length;i++){
    		try{    		
    		sum =sum*10+Integer.parseInt(vs[i]);
    		}catch(Exception e){
    		}
    	}
    	return sum;
	}
}
