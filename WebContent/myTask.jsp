<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="/common/global.jsp"%>
<%@ include file="/common/meta.jsp" %>
<%@ include file="/common/include-base-styles.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>待办任务</title>
</head>
<body>
<c:if test="${not empty message }">
	${message }
</c:if>
<table>
	<thead>
		<tr>
			<th>任务id</th>
			<th>人物名称</th>
			<th>流程实例id</th>
			<th>任务创建时间</th>
			<th>任务办理人</th>
			<th>操作</th>
		</tr>
	</thead>
	<c:forEach items="${tasks}" var="task">
		<tr>
			<td>${task.id}</td>
			<td>${task.name}</td>
			<td>${task.processInstanceId}</td>
			<td>${task.createTime}</td>
			<td>${task.assignee}</td>
			<td>
			<a class="btn" href="${ctx }/doTask/${task.id}"><i class="icon-eye-open"></i>查看</a>
			<%-- 未签收 -->
			<c:if test="${empty task.assignee }">
				
				<a href="${ctx }/claimTask/${task.id }">签收</a>
			</c:if>
			<!-- 签收 -->
			<c:if test="${not empty task.assignee }">
				<a href="${ctx }/doTask/${task.id }">办理</a>
			</c:if--%>
			</td>
		</tr>
	</c:forEach>
</table>
</body>
</html>