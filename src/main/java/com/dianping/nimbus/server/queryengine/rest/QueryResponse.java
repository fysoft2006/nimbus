package com.dianping.nimbus.server.queryengine.rest;

import java.util.List;

public class QueryResponse {
	private String id;
	private String[] columnNames;
	private List<String[]> data;
	private long execTime;
	private String errorMsg;
	private String resultFilePath;
	private Boolean success;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String[] getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	public List<String[]> getData() {
		return data;
	}
	public void setData(List<String[]> data) {
		this.data = data;
	}
	public long getExecTime() {
		return execTime;
	}
	public void setExecTime(long execTime) {
		this.execTime = execTime;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public String getResultFilePath() {
		return resultFilePath;
	}
	public void setResultFilePath(String resultFilePath) {
		this.resultFilePath = resultFilePath;
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}

}
