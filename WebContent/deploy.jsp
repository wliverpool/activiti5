<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>部署</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/deploy" method="post" enctype="multipart/form-data">
	<input type="file" name="file"/>
	
	<input type="submit" value="Submit"/>
</form>
<a href="${pageContext.request.contextPath}">返回</a>
</body>
</html>