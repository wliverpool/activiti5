<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/common/global.jsp"%>
	<%@ include file="/common/meta.jsp" %>
	<%@ include file="/common/include-base-styles.jsp" %>
	<link rel="stylesheet" href="${ctx}/js/common/plugins/timepicker.css">
	<title>请假申请</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<script type="text/javascript" src="${ctx }/js/common/jquery.js"></script>
	<script type="text/javascript" src="${ctx }/js/common/bootstrap.min.js"></script>
	<script type="text/javascript" src="${ctx }/js/common/bootstrap-datepicker.js"></script>
	<script type="text/javascript" src="${ctx }/js/common/plugins/bootstrap-timepicker.js"></script>
	<script type="text/javascript" src="${ctx }/js/common/plugins/datetimepicker/bootstrap-datetimepicker.min.js"></script>
	<script type="text/javascript">
	$(function() {
		$('.datepicker').datetimepicker();
		
	});

	function beforeSend() {
		$('input[name=startTime]').val($('#startDate').val());
		$('input[name=endTime]').val($('#endDate').val());
		//alert($('input[name=startTime]').val());
	}
	</script>
</head>
<body>
	<c:if test="${not empty message}">
		<div id="message" class="alert alert-success">${message}</div>
		<!-- 自动隐藏提示信息 -->
		<script type="text/javascript">
		setTimeout(function() {
			$('#message').hide('slow');
		}, 5000);
		</script>
	</c:if>
	<form action="${ctx }/leaveStart" class="form-horizontal" method="post" onsubmit="beforeSend()">
		<input type="hidden" name="startTime" />
		<input type="hidden" name="endTime" />
		<fieldset>
			<legend><small>请假申请</small></legend>
			<div id="messageBox" class="alert alert-error input-large controls" style="display:none">输入有误，请先更正。</div>
			<div class="control-group">
				<label for="loginName" class="control-label">请假类型:</label>
				<div class="controls">
					<select id="leaveType" name="leaveType" class="required">
						<option>公休</option>
						<option>病假</option>
						<option>调休</option>
						<option>事假</option>
						<option>婚假</option>
					</select>
				</div>
			</div>
			<div class="control-group">
				<label for="name" class="control-label">开始时间:</label>
				<div id="datetimepicker" class="datepicker input-append date">
                    <input data-format="yyyy-MM-dd hh:mm:ss" id="startDate" name="effectiveDate" class="input-medium"
                           type="text"/>
                        <span class="add-on">
                            <i data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
                        </span>
                </div>
			</div>
			<div class="control-group">
				<label for="plainPassword" class="control-label">结束时间:</label>
				<div id="datetimepicker" class="datepicker input-append date">
                    <input data-format="yyyy-MM-dd hh:mm:ss" id="endDate" name="effectiveDate" class="input-medium"
                           type="text"/>
                        <span class="add-on">
                            <i data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
                        </span>
                </div>
			</div>
			<div class="control-group">
				<label for="groupList" class="control-label">请假原因:</label>
				<div class="controls">
					<textarea name="reason"></textarea>
				</div>
			</div>
			<div class="form-actions">
				<button type="submit" class="btn"><i class="icon-play"></i>启动流程</button>
			</div>
		</fieldset>
	</form>
</body>
</html>