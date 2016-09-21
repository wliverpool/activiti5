package com.activiti.util;

import java.util.Date;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtil {

	/**
	 * ��ȡ����
	 * 
	 * @param timestamp
	 * @return Date
	 */
	public Date getDate(Timestamp timestamp) {
		return toDate(timestamp, null);
	}

	/**
	 * ��ȡ����
	 * 
	 * @param timestamp
	 * @param format
	 * @return Date
	 */
	public Date getDate(Timestamp timestamp, String format) {
		return toDate(timestamp, format);
	}

	/**
	 * Timestamp����ʽת����Date
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
