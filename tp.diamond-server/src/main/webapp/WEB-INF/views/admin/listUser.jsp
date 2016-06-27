<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="userAddId" id="pageId" />

<c:if test="${sessionScope.message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 195px; padding: 2px 15px 2px 10px;">
		${sessionScope.message}
	</div>
</c:if>

<table class="table table-striped table-bordered">
  	<thead>
    	<tr>
      		<th>用户名</th>
      		<th>密码</th>
      		<th>操作</th>
    	</tr>
  	</thead>
  	<tbody>
    	<c:forEach items="${userMap}" var="user">
            <tr>
               <td>
                  <c:out value="${user.key}"/>
               </td>
              <td>
                  <c:out value="${user.value}" />
               </td>
              <c:url var="changePasswordUrl" value="/admin/changePasswordPage" >
                  <c:param name="userName" value="${user.key}" />
              </c:url>
              <c:url var="deleteUserUrl" value="/admin/deleteUser" >
                  <c:param name="userName" value="${user.key}" />
                  <c:param name="password" value="${user.value}" />
              </c:url>
              <td>
                 <a href="${changePasswordUrl}" onclick="return changePassword('${user.key}',this);">修改密码</a>&nbsp;&nbsp;&nbsp;
                 <c:if test="${user.key != 'admin' }">
                	 <a href="${deleteUserUrl}" class="deleteUser">删除</a>&nbsp;&nbsp;&nbsp;
                 </c:if>
              </td>
            </tr>
          </c:forEach>
	</tbody>
</table>
<ul class="pager">
	<button class="btn btn-primary" onclick="window.location.href = './../admin/usernew'">添加用户</button>
	<button class="btn btn-primary" onclick="window.location.href = './../admin/reloadUser'">重新加载用户信息</button>
</ul>

<script type="text/javascript">

$(document).ready(function () {
	$("a.deleteUser").click(function(e) {
	    e.preventDefault();
	    bootbox.confirm("确定删除用户，删除之后不可恢复！", function(confirmed) {
	    	if(confirmed)
	    		window.location.href = e.target.href;
	    });
	    
	    return false;
	});
});
</script>