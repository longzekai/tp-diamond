<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="userAddId" id="pageId" />

<h2>修改密码</h2>
<form class="form-horizontal" method="post" action='<c:url value="/admin/changePassword" />' autocomplete="off" >
   	<div class="control-group">
 		<label class="control-label">用户名：</label>
     	<input type="text" class="input-xlarge" name="userName" readonly value="${param.userName }">
   	</div>
   	<div class="control-group">
 		<label class="control-label">密码：</label>
     	<input type="text" class="input-xlarge" name="password">
   	</div>
   	<div class="form-actions">
		<button class="btn btn-primary" type="submit">保存</button>
		<button class="btn" onclick="history.go(-1);">返回</button>
	</div>
</form>