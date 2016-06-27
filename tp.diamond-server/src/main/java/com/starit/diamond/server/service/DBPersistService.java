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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.starit.diamond.domain.ConfigInfo;
import com.starit.diamond.domain.Page;
import com.starit.diamond.server.utils.PaginationHelper;

@Service
public class DBPersistService implements PersistService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBPersistService.class);

	private static final int QUERY_TIMEOUT = 1;// seconds

	private JdbcTemplate jt;
	
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jt = new JdbcTemplate(dataSource);
	}

	public JdbcTemplate getJdbcTemplate() {
		return this.jt;
	}

	private static final class ConfigInfoRowMapper implements
			ParameterizedRowMapper<ConfigInfo> {
		public ConfigInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfo info = new ConfigInfo();
			info.setId(rs.getLong("ID"));
			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setUserName(rs.getString("username"));
			info.setContent(rs.getString("content"));
			info.setMd5(rs.getString("md5"));
			info.setDescription(rs.getString("description"));
			return info;
		}
	}

	private static final ConfigInfoRowMapper CONFIG_INFO_ROW_MAPPER = new ConfigInfoRowMapper();

	public void addConfigInfo(final Timestamp time, final ConfigInfo configInfo) {
		final Long id = this.jt.queryForObject("SELECT MAX(ID) FROM config_info", Long.class);
		
		this.jt.update(
				"insert into config_info (id, data_id,group_id, username, content,md5,gmt_create,gmt_modified, description) values(?,?,?,?,?,?,?,?,?)",
				new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps)
							throws SQLException {
						
						int index = 1;
						if(id == null)
							ps.setLong(index++, 1);
						else
							ps.setLong(index++, (Long)id + 1);
						
						ps.setString(index++, configInfo.getDataId());
						ps.setString(index++, configInfo.getGroup());
						ps.setString(index++, configInfo.getUserName());
						ps.setString(index++, configInfo.getContent());
						ps.setString(index++, configInfo.getMd5());
						ps.setTimestamp(index++, time);
						ps.setTimestamp(index++, time);
						ps.setString(index++, configInfo.getDescription());
					}

				});
	}

	public void removeConfigInfo(final String dataId, final String group) {

		this.jt.update(
				"delete from config_info where data_id=? and group_id=?",
				new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps)
							throws SQLException {
						int index = 1;
						ps.setString(index++, dataId);
						ps.setString(index++, group);
					}

				});
	}

	public void removeConfigInfoByID(final long id) {

		this.jt.update("delete from config_info where ID=? ",
				new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps)
							throws SQLException {
						ps.setLong(1, id);
					}

				});
	}

	public void updateConfigInfo(final Timestamp time,
			final ConfigInfo configInfo) {

		this.jt.update(
				"update config_info set content=?,md5=?,gmt_modified=?,description=? where data_id=? and group_id=?",
				new PreparedStatementSetter() {

					public void setValues(PreparedStatement ps)
							throws SQLException {
						int index = 1;
						ps.setString(index++, configInfo.getContent());
						ps.setString(index++, configInfo.getMd5());
						ps.setTimestamp(index++, time);
						ps.setString(index++, configInfo.getDescription());
						ps.setString(index++, configInfo.getDataId());
						ps.setString(index++, configInfo.getGroup());
					}
				});
	}

	public ConfigInfo findConfigInfo(final String dataId, final String group) {
		this.jt.setQueryTimeout(QUERY_TIMEOUT);

		try {
			return (ConfigInfo) this.jt
					.queryForObject(
							"select ID,data_id,group_id,username,content,md5,description from config_info where group_id=? and data_id=?",
							new Object[] { group, dataId },
							CONFIG_INFO_ROW_MAPPER);
		} catch (DataAccessException e) {
			if (!(e instanceof EmptyResultDataAccessException)) {
				LOGGER.error("查询ConfigInfo失败, 数据库异常", e);
				// 将异常重新抛出
				throw e;
			}
			// 是EmptyResultDataAccessException, 表明数据不存在, 返回null
			return null;
		}
	}

	public ConfigInfo findConfigInfoByID(long id) {
		this.jt.setQueryTimeout(QUERY_TIMEOUT);

		try {
			return (ConfigInfo) this.jt
					.queryForObject(
							"select ID,data_id,group_id,content,md5,username,description from config_info where ID=?",
							new Object[] { id }, CONFIG_INFO_ROW_MAPPER);
		} catch (DataAccessException e) {
			if (!(e instanceof EmptyResultDataAccessException)) {
				LOGGER.error("查询ConfigInfo失败, 数据库异常", e);
				throw e;
			}
			return null;
		}
	}
	
	public Page<ConfigInfo> findConfigInfo(final int pageNo,final int pageSize) {
		return this.findConfigInfo(pageNo, pageSize, null, null, "admin");
	}

	public Page<ConfigInfo> findConfigInfo(final int pageNo,
			final int pageSize, final String dataId, final String group, final String userName) {
		this.jt.setQueryTimeout(QUERY_TIMEOUT);

		PaginationHelper<ConfigInfo> helper = new PaginationHelper<ConfigInfo>();
		String sqlCountRows = "select count(ID) from config_info ";
		String sqlFetchRows = "select ID,data_id,group_id,content,md5,username,description from config_info ";
		if(!"admin".equals(userName)) {
			sqlCountRows += "where username = ? ";
			sqlFetchRows += "where username = ? ";
		} else {
			sqlCountRows += "where 'admin' = ? ";
			sqlFetchRows += "where 'admin' = ? ";
		}
		
		if (StringUtils.hasLength(dataId)) {
			sqlCountRows += "and data_id like ? ";
			sqlFetchRows += "and data_id like ? ";
		}
		if (StringUtils.hasLength(group)) {
			sqlCountRows += "and group_id like ? ";
			sqlFetchRows += "and group_id like ? ";
		}
		Object[] args = null;
		if (StringUtils.hasLength(dataId) && StringUtils.hasLength(group)) {
			args = new Object[3];
			args[0] = userName;
			args[1] = generateLikeArgument(dataId);
			args[2] = generateLikeArgument(group);
		} else if (StringUtils.hasLength(dataId)) {
			args = new Object[] {userName, generateLikeArgument(dataId) };
		} else if (StringUtils.hasLength(group)) {
			args = new Object[] {userName, generateLikeArgument(group) };
		} else {
			args = new Object[] {userName};
		}

		return helper.fetchPage(this.jt, sqlCountRows, sqlFetchRows, args,
				pageNo, pageSize, CONFIG_INFO_ROW_MAPPER);
	}

	private String generateLikeArgument(String s) {
		if (s.indexOf("*") >= 0)
			return s.replaceAll("\\*", "%");
		else {
			return "%" + s + "%";
		}
	}

}
