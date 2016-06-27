<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
	<head>
    	<meta charset="utf-8">
    	<title>Diamond 配置管理服务器</title>
    	<meta name="viewport" content="width=device-width, initial-scale=1.0">
    	<link href='<c:url value="/resources/css/bootstrap/css/bootstrap.min.css" />' rel="stylesheet">
    	<script type="text/javascript" src='<c:url value="/resources/js/jquery.min.js" />'></script>
    	<script type="text/javascript" src='<c:url value="/resources/js/bootstrap-modal.js" />'></script>
    	<script type="text/javascript" src='<c:url value="/resources/js/bootstrap-transition.js" />'></script>
    	<script type="text/javascript" src='<c:url value="/resources/js/bootbox.min.js" />'></script>
    	<style type="text/css">
	      body {
	        padding-top: 60px;
	        padding-bottom: 40px;
	      }
	      .sidebar-nav {
	        padding: 9px 0;
	      }
	    </style>
	    <script type="text/javascript">
		  	$(document).ready(function () {
		  		var menuId = $("#pageId").val();
				$("#" + menuId).addClass("active");
		  	});
		</script>
	    <decorator:head/>
	</head>

  	<body>
    	<div class="navbar navbar-fixed-top">
      		<div class="navbar-inner">
        		<div class="container-fluid">
          			<a class="brand" href="#">Diamond 配置管理服务器</a>
          			<div class="pull-right">
            			<a class="btn" href='<c:url value="/logout" />'>注销</a>
          			</div>
        		</div>
      		</div>
    	</div>

    	<div class="container-fluid">
      		<div class="row-fluid">
        		<div class="span2">
          			<div class="well sidebar-nav">
            			<ul class="nav nav-list">
              				<li class="nav-header"><h3>导航菜单</h3></li>
              				<li id="configId"><a href="./../admin/config">配置信息管理</a></li>
              				<c:if test="${sessionScope.user == 'admin'}">
              					<li id="userAddId"><a href="./../admin/listUser">用户管理</a></li>
              				</c:if>
            			</ul>
          			</div><!--/.well -->
        		</div><!--/span-->
        		<div class="span10">
          			<div class="hero-unit" style="padding: 10px;">
          				<decorator:body></decorator:body>
          			</div>
        		</div><!--/span-->
      		</div><!--/row-->
    	</div>
    	<% request.getSession().removeAttribute("message"); %>
  	</body>
</html>
