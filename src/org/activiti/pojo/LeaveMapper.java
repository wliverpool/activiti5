package org.activiti.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class LeaveMapper implements RowMapper<Leave> {

	@Override
	public Leave mapRow(ResultSet rs, int rowNum) throws SQLException {
		Leave leave = new Leave();
		leave.setId(rs.getLong("ID"));
		leave.setApplyTime(rs.getTimestamp("APPLY_TIME"));
		leave.setEndTime(rs.getTimestamp("END_TIME"));
		leave.setLeaveType(rs.getString("LEAVE_TYPE"));
		leave.setProcessInstanceId(rs.getString("PROCESS_INSTANCE_ID"));
		leave.setRealityEndTime(rs.getTimestamp("REALITY_END_TIME"));
		leave.setRealityStartTime(rs.getTimestamp("REALITY_START_TIME"));
		leave.setReason(rs.getString("REASON"));
		leave.setStartTime(rs.getTimestamp("START_TIME"));
		leave.setUserId(rs.getString("USER_ID"));
		return leave;
	}

}
