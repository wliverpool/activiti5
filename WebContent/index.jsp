<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>首页</title>
</head>
<body>
<a href="${pageContext.request.contextPath}/deploy.jsp">部署流程</a><br/>
<a href="${pageContext.request.contextPath}/processList">已部署流程</a><br/>
<a href="${pageContext.request.contextPath}/myTasks">待办任务</a><br/>
<a href="${pageContext.request.contextPath}/toLeaveApply">发起请假(普通表单)</a><br/>
<a href="${pageContext.request.contextPath}/toTaskLeaveList">请假流程任务列表</a><br/>
<a href="${pageContext.request.contextPath}/joinedExecutionList">执行中的已参与流程</a><br/>
<a href="${pageContext.request.contextPath}/finishedList">已结束流程</a><br/>
<a href="${pageContext.request.contextPath}/taskRunning">使用mybatis方式查询</a><br/>
<a href="${pageContext.request.contextPath}/processinstanceManagerList">流程实例管理</a><br/>
<a href="${pageContext.request.contextPath}/jobList">作业管理</a><br/>
<a href="${pageContext.request.contextPath}/getEngineInfo">引擎配置</a><br/>
<a href="${pageContext.request.contextPath}/databaseInfo">引擎数据库</a><br/>
<a href="${pageContext.request.contextPath}/identityUserList">用户和组</a>
</body>
</html>