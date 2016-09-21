package com.activiti.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.pojo.Leave;
import org.activiti.pojo.LeaveMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mysql.jdbc.Statement;

@Repository
public class LeaveDao{
	
	private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

    @Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public long save(final Leave leave) {
		KeyHolder key=new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement p = con.prepareStatement("insert into T_LEAVE (PROCESS_INSTANCE_ID,USER_ID,START_TIME,END_TIME,LEAVE_TYPE,APPLY_TIME,REALITY_START_TIME,REALITY_END_TIME,REASON) values (?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
				p.setString(1, leave.getProcessInstanceId());
				p.setString(2, leave.getUserId());
				p.setObject(3, leave.getStartTime());
				p.setObject(4, leave.getEndTime());
				p.setString(5, leave.getLeaveType());
				p.setObject(6, leave.getApplyTime());
				p.setObject(7, leave.getRealityStartTime());
				p.setObject(8, leave.getRealityEndTime());
				p.setString(9, leave.getReason());
				return p;
			}
		},key);
		return key.getKey().longValue();
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("delete from T_LEAVE where id=?",new Object[]{id});
    }
    
    public int deleteByProcessInstanceId(String processInstanceId){
    	return jdbcTemplate.update("delete from T_LEAVE where process_instance_id=?",new Object[]{processInstanceId});
    }

    public Leave getById(Long id) {
    	return jdbcTemplate.queryForObject("SELECT ID,PROCESS_INSTANCE_ID,USER_ID,START_TIME,END_TIME,LEAVE_TYPE,APPLY_TIME,REALITY_START_TIME,REALITY_END_TIME,REASON FROM T_LEAVE where id=?", new Object[]{id},new LeaveMapper());
    }
    
    public Leave getByProcessInstanceId(String processInstanceId) {
    	return jdbcTemplate.queryForObject("SELECT ID,PROCESS_INSTANCE_ID,USER_ID,START_TIME,END_TIME,LEAVE_TYPE,APPLY_TIME,REALITY_START_TIME,REALITY_END_TIME,REASON FROM T_LEAVE where PROCESS_INSTANCE_ID=?", new Object[]{processInstanceId},new LeaveMapper());
    }
    
    public void updateLeave(Leave leave){
    	jdbcTemplate.update(generateUpdateByIdSql(leave,false),generateUpdateParam(leave,false));
    }
    
    /**
	 * 生成更新的sql语句
	 * @param info  请假信息
	 * @param isUpdateNullValueProperty  是否更新空值对象
	 * @return
	 */
	private String generateUpdateByIdSql(Leave leave,boolean isUpdateNullValueProperty){
		StringBuffer sb = new StringBuffer();
		sb.append("update T_LEAVE set ");
		//属性不为空，或者更新空值属性为true更新空值属性
		if((leave.getProcessInstanceId()==null&&isUpdateNullValueProperty)||null!=leave.getProcessInstanceId()){
			sb.append("PROCESS_INSTANCE_ID=?,");
		}
		if((leave.getUserId()==null&&isUpdateNullValueProperty)||null!=leave.getUserId()){
			sb.append("USER_ID=?,");
		}
		if((leave.getStartTime()==null&&isUpdateNullValueProperty)||null!=leave.getStartTime()){
			sb.append("START_TIME=?,");
		}
		if((leave.getEndTime()==null&&isUpdateNullValueProperty)||null!=leave.getEndTime()){
			sb.append("END_TIME=?,");
		}
		if((leave.getLeaveType()==null&&isUpdateNullValueProperty)||null!=leave.getLeaveType()){
			sb.append("LEAVE_TYPE=?,");
		}
		if((leave.getApplyTime()==null&&isUpdateNullValueProperty)||null!=leave.getApplyTime()){
			sb.append("APPLY_TIME=?,");
		}
		if((leave.getRealityStartTime()==null&&isUpdateNullValueProperty)||null!=leave.getRealityStartTime()){
			sb.append("REALITY_START_TIME=?,");
		}
		if((leave.getRealityEndTime()==null&&isUpdateNullValueProperty)||null!=leave.getRealityEndTime()){
			sb.append("REALITY_END_TIME=?,");
		}
		if((leave.getReason()==null&&isUpdateNullValueProperty)||null!=leave.getReason()){
			sb.append("REASON=?,");
		}
		//删除最后一个,逗号
		sb.delete(sb.length()-1, sb.length());
		sb.append(" where ID=?");
		return sb.toString();
	}
	
	/**
	 * 根据课件信息生成更新sql的参数
	 * @param info   请假信息
	 * @param isUpdateNullValueProperty   是否生成空值参数
	 * @return
	 */
	private Object[] generateUpdateParam(Leave leave,boolean isUpdateNullValueProperty){
		List<Object> paramList = new ArrayList<Object>();
		//属性不为空，或者更新空值属性为true时添加参数
		if(leave.getProcessInstanceId()==null&&isUpdateNullValueProperty){
			paramList.add(null);
		}else if(null!=leave.getProcessInstanceId()){
			paramList.add(leave.getProcessInstanceId());
		}
		if(leave.getUserId()==null&&isUpdateNullValueProperty){
			paramList.add(null);
		}else if(null!=leave.getUserId()){
			paramList.add(leave.getUserId());
		}
		if(leave.getStartTime()==null&&isUpdateNullValueProperty){
			paramList.add(null);
		}else if(null!=leave.getStartTime()){
			paramList.add(leave.getStartTime());
		}
		if(leave.getEndTime()==null&&isUpdateNullValueProperty){
			paramList.add(null);
		}else if(null!=leave.getEndTime()){
			paramList.add(leave.getEndTime());
		}
		if(leave.getLeaveType()==null&&isUpdateNullValueProperty){
			paramList.add(null);
		}else if(null!=leave.getLeaveType()){
			paramList.add(leave.getLeaveType());
		}
		if(leave.getApplyTime()==null&&isUpdateNullValueProperty){
			paramList.add(null);
		}else if(null!=leave.getApplyTime()){
			paramList.add(leave.getApplyTime());
		}
		if(leave.getRealityStartTime()==null&&isUpdateNullValueProperty){
			paramList.add(null);
		}else if(null!=leave.getRealityStartTime()){
			paramList.add(leave.getRealityStartTime());
		}
		if(leave.getRealityEndTime()==null&&isUpdateNullValueProperty){
			paramList.add(null);
		}else if(null!=leave.getRealityEndTime()){
			paramList.add(leave.getRealityEndTime());
		}
		if(leave.getReason()==null&&isUpdateNullValueProperty){
			paramList.add(null);
		}else if(null!=leave.getReason()){
			paramList.add(leave.getReason());
		}
		paramList.add(leave.getId());
		return paramList.toArray(new Object[paramList.size()]);
	}
}

