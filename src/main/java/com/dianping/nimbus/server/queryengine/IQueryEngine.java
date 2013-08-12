package com.dianping.nimbus.server.queryengine;

import com.dianping.nimbus.client.bo.HiveQueryInputBo;
import com.dianping.nimbus.client.bo.HiveQueryOutputBo;

public interface IQueryEngine {
	
	@Deprecated
	public HiveQueryOutput getQueryResult(HiveQueryInput input);
	
	public HiveQueryOutputBo getQueryResult(HiveQueryInputBo input);
	
	public String getQueryStatus(String queryId);
	
	public Boolean stopQuery(String queryId);
	
}
