package com.activiti.util;

import java.util.Date;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtil {

	/**
	 * 获取日期
	 * 
	 * @param timestamp
	 * @return Date
	 */
	public Date getDate(Timestamp timestamp) {
		return toDate(timestamp, null);
	}

	/**
	 * 获取日期
	 * 
	 * @param timestamp
	 * @param format
	 * @return Date
	 */
	public Date getDate(Timestamp timestamp, String format) {
		return toDate(timestamp, format);
	}

	/**
	 * Timestamp按格式转换成Date
	 * 
	 * @param timestamp
	 * @param format
	 * @return Date
	 */
	public Date toDate(Timestamp timestamp, String format) {
		Date date = null;
		if (null == format || "".equals(format))
			format = "";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			date = sdf.parse(sdf.format(timestamp));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

}
