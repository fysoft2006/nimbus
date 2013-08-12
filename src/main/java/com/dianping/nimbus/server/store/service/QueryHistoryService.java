package com.dianping.nimbus.server.store.service;

import java.util.List;

import javax.jdo.annotations.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.nimbus.server.store.domain.QueryHistory;
import com.dianping.nimbus.server.store.persistence.QueryHistoryMapper;

@Service
public class QueryHistoryService {
	private static Log logger = LogFactory.getLog(QueryHistoryService.class);
	
	@Autowired
	private QueryHistoryMapper queryHistoryMapper;
	
	@Transactional
	public List<QueryHistory> selectQueryHistoryByUsername(String username){
		List<QueryHistory> queryHistoryList = queryHistoryMapper.selectQueryHistoryByUsername(username);
		
		if (logger.isDebugEnabled()){
			logger.debug("username:" + username + " total query history list size is:" + queryHistoryList.size());
			
			for (QueryHistory queryHistory : queryHistoryList) {
				logger.debug(queryHistory);
			}
		}
		return queryHistoryList;
	};
    
	@Transactional
    public void insertQueryHistory(QueryHistory qh){
		if (logger.isDebugEnabled()){
			logger.debug(qh);
		}
		
		queryHistoryMapper.insertQueryHistory(qh);
	};
	
	@Transactional
	public List<String> selectLastNQuery(){
		List<String> hqls = queryHistoryMapper.selectLastNQuery();
		return hqls;
	}
}
