package com.dianping.nimbus.server.queryengine.cmdline.streamhandler;

import java.io.InputStream;

import com.dianping.nimbus.client.bo.HiveQueryOutputBo;

public interface IStreamHandler extends Runnable{
	
	public void setInputStream(InputStream is);
	
	public void setShowLimit(int limit);
	
	public void setSaveRecordsLimit(int limit);
	
	public void setExecuteProcess(Process proc);
	
	public Boolean getProcessKillStatus();
	
	public void setProcessKillStatus(Boolean killStatus);
	
	public HiveQueryOutputBo getResult();
}
