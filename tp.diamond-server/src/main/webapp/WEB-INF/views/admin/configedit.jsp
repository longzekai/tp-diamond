<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="configId" id="pageId" />

<c:if test="${sessionScope.message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 195px; padding: 2px 15px 2px 10px;">
		${sessionScope.message}
	</div>
</c:if>

<h2>更新配置信息</h2>
<form class="form-horizontal" method="post" action='<c:url value="/admin/updateConfig" />' autocomplete="off" >
   	<div class="control-group">
 		<label class="control-label">group：</label>
     	<input type="text" class="input-xlarge" name="group" readonly value="${configInfo.group }">
   	</div>
   	<div class="control-group">
 		<label class="control-label">dataId：</label>
     	<input type="text" class="input-xlarge" name="dataId" readonly value="${configInfo.dataId }">
   	</div>
   	<div class="control-group">
 		<label class="control-label">描述：</label>
     	<input type="text" class="input-xlarge" name="description" value="${configInfo.description }">
   	</div>
   	<div class="control-group">
 		<label class="control-label">配置信息：</label>
     	<textarea rows="20" id="textarea" class="input-xlarge" name="content" style="width: 80%">${configInfo.content }</textarea>
   	</div>
   	<div class="form-actions">
		<button class="btn btn-primary" type="submit">保存</button>
		<button class="btn" onclick="history.go(-1);">返回</button>
	</div>
</form>