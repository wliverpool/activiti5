<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>登录系统</title>
<%@ include file="/common/global.jsp"%>
<%@ include file="/common/meta.jsp"%>
<%@ include file="/common/include-base-styles.jsp" %>
</head>

<body style="margin-top: 3em;">
	<center>
		<c:if test="${not empty param.error}">
			<h2 id="error" class="alert alert-error">用户名或密码错误！！！</h2>
		</c:if>
		<c:if test="${not empty param.timeout}">
			<h2 id="error" class="alert alert-error">未登录或超时！！！</h2>
		</c:if>
		<div style="width: 500px">
			<h2>activiti in action</h2>
			<form action="${ctx }/logon" method="get">
				<table>
					<tr>
						<td width="80">用户名：</td>
						<td><input id="username" name="username" style="width: 100px" /></td>
					</tr>
					<tr>
						<td>密码：</td>
						<td><input id="password" name="password" type="password"
							style="width: 100px" /></td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td>
							<button type="submit" class="btn btn-primary">登录系统</button>
						</td>
					</tr>
				</table>
			</form>
		</div>
	</center>
</body>
</html>