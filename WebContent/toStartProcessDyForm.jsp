<%@page import="org.apache.commons.lang3.ObjectUtils"%>
<%@page import="org.activiti.engine.form.FormProperty"%>
<%@page import="org.activiti.engine.form.FormType"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="/common/global.jsp"%>
<%@ include file="/common/meta.jsp" %>
<%@ include file="/common/include-base-styles.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>启动动态表单流程</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<script type="text/javascript" src="${ctx }/js/common/jquery.js"></script>
	<script type="text/javascript" src="${ctx }/js/common/bootstrap.min.js"></script>
	<script type="text/javascript" src="${ctx }/js/common/bootstrap-datepicker.js"></script>
</head>
<body>
<h3>启动流程—
	<c:if test="${hasStartFormKey}">
		[${processDefinition.name}]，版本号：${processDefinition.version}
	</c:if>
	<c:if test="${!hasStartFormKey}">
		[${startFormData.processDefinition.name}]，版本号：${startFormData.processDefinition.version}
	</c:if>
</h3>
<hr/>
<form action="${ctx }/startProcessDyForm/${pdid}" method="post">
	<c:if test="${hasStartFormKey}"><!-- 外置表单直接显示内容 -->
		${startFormData}
	</c:if>
	<c:if test="${!hasStartFormKey}"><!-- 动态表单根据读取到的表单列表显示表单中的元素 -->
	<c:forEach items="${startFormData.formProperties }" var="fp">
		<c:set var="fpo" value="${fp }"/>
		<% 
			FormType type = ((FormProperty)pageContext.getAttribute("fpo")).getType();
			//需要读取扩展属性
			String[] keys = {"datePattern"};
			for(String key:keys){
				pageContext.setAttribute(key, ObjectUtils.toString(type.getInformation(key)));
			}
		%>
		<!-- 显示流程定义中定义的启动流程的动态表单 -->
		<div class="control-group">
		<!-- 文本或者数字输入项 -->
		<c:if test="${fp.type.name=='string'||fp.type.name=='long'|| fp.type.name == 'double' }">
			<label for="${fp.id }">${fp.name}:</label>
			<input type="text" id="${fp.id }" name="${fp.id }" data-type="${fp.type.name}" ${required}/>
		</c:if>
		<!-- 日期输入项 -->
		<c:if test="${fp.type.name=='date' }">
			<label for="${fp.id }">${fp.name }:</label>
			<input type="text" id="${fp.id }" name="${fp.id }" class="datepicker"  data-type="${fp.type.name}" data-date-format="${fn:toLowerCase(datePattern)}"/>
		</c:if>
		
		<%-- 大文本 --%>
		<c:if test="${fp.type.name == 'bigtext'}">
			<label class="control-label" for="${fp.id}">${fp.name}:</label>
			<div class="controls">
				<textarea id="${fp.id}" name="${fp.id}" data-type="${fp.type.name}" ${required}></textarea>
			</div>
		</c:if>
		
		<%-- Javascript类型 --%>
		<c:if test="${fp.type.name == 'javascript'}">
			<script type="text/javascript">${fp.value};</script>
		</c:if>
		
		<%-- 选择人员 --%>
		<c:if test="${fp.type.name == 'users'}">
			<label class="control-label" for="${fp.id}">${fp.name}:</label>
			<div class="controls">
				<input type="text" id="${fp.id}" name="${fp.id}" data-type="${fp.type.name}" class="users" readonly />
			</div>
		</c:if>
		
		</div>
	</c:forEach>
	</c:if>
	<div class="control-group">
		<div class="controls">
			<a href="javascript:history.back();" class="btn"><i class="icon-backward"></i>返回列表</a>
			<button type="submit" class="btn"><i class="icon-play"></i>启动流程</button>
		</div>
	</div>
</form>

<div id="userModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="userModalLabel" aria-hidden="true" style="width: 260px;">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
    <h3 id="userModalLabel">选择人员</h3>
  </div>
  <div class="modal-body">
  	<select multiple="multiple" style="height: 200px;"></select>
  </div>
  <div class="modal-footer">
    <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
    <button class="btn btn-primary ok">确定</button>
  </div>
</div>
<script type="text/javascript">
$(function() {
	$('.datepicker').datepicker();
	
	// 选择人对话框
	$('#userModal').on('show', function() {
		$('#userModal select').html('');
		$.getJSON(ctx + '/auditUserList', function(datas) {
			$.each(datas, function(k, v) {
				var $opg = $('<optgroup/>', {
					label: k
				}).appendTo('#userModal select');
				$.each(v, function() {
					$('<option/>', {
						'value': this.id,
						'text': this.firstName + ' ' + this.lastName + '（' + this.id + '）'
					}).appendTo($opg);
				});
			});
		});
	});

	// 单击打开选择人对话框
	$('.users').live('click', function() {
		$('body').data('usersEle', this);
		$('#userModal').modal('show');
	});

	$('#userModal .ok').click(function() {
		var ele = $('body').data('usersEle');
		var users = new Array();
		$('#userModal select option:selected').each(function() {
			users.push($(this).val());
		});
		$(ele).val(users);
		$('#userModal').modal('hide');
	});
});
</script>
</body>
</html>