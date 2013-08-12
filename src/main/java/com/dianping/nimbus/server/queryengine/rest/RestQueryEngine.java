package com.dianping.nimbus.server.queryengine.rest;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.nimbus.client.bo.HiveQueryInputBo;
import com.dianping.nimbus.client.bo.HiveQueryOutputBo;
import com.dianping.nimbus.server.LoginServiceImpl;
import com.dianping.nimbus.server.queryengine.HiveQueryInput;
import com.dianping.nimbus.server.queryengine.HiveQueryOutput;
import com.dianping.nimbus.server.queryengine.IQueryEngine;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestQueryEngine implements IQueryEngine {
	private static final Log LOG = LogFactory.getLog(RestQueryEngine.class);

	private static final String STATUS_REST_PREFIX = "/polestar/query/status/";
	private static final String CANCEL_REST_PREFIX = "/polestar/query/cancel/";
	private static final String POST_REST_PREFIX = "/polestar/query/post";

	private Gson gson = new Gson();
	private Client client = Client.create();
	private String host;

	@Override
	public HiveQueryOutput getQueryResult(HiveQueryInput input) {
		return null;
	}

	@Override
	public HiveQueryOutputBo getQueryResult(HiveQueryInputBo input) {
		QueryRequest qreq = new QueryRequest();
		qreq.setId(input.getQueryid());
		qreq.setDatabase(input.getDatabase());
		qreq.setMode(input.getEngineMode());
		qreq.setSql(input.getHql());
		qreq.setStoreResult(input.isStoreResult());
		qreq.setUsername(input.getUsername());
		qreq.setPassword(LoginServiceImpl.getUsernameToPasswd().getIfPresent(
				input.getUsername()));

		String queryRequestJson = gson.toJson(qreq);
		WebResource webResource = client.resource(host + POST_REST_PREFIX);
		ClientResponse response = webResource.type(MediaType.APPLICATION_JSON)
				.post(ClientResponse.class, queryRequestJson);
		QueryResponse qres = gson.fromJson(response.getEntity(String.class),
				QueryResponse.class);

		HiveQueryOutputBo queryOutputBo = new HiveQueryOutputBo();
		queryOutputBo.setSuccess(qres.getSuccess());
		queryOutputBo.setResultFileAbsolutePath(qres.getResultFilePath());
		queryOutputBo.setExecTime(qres.getExecTime());
		queryOutputBo.setFieldSchema(qres.getColumnNames());
		queryOutputBo.setErrorMsg(qres.getErrorMsg());
		queryOutputBo.setData(qres.getData());

		return queryOutputBo;
	}

	@Override
	public String getQueryStatus(String queryId) {
		WebResource statusResouce = client.resource(host + STATUS_REST_PREFIX
				+ queryId);
		ClientResponse response = statusResouce
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		QueryStatus qs = gson.fromJson(response.getEntity(String.class),
				QueryStatus.class);
		if (qs.getSuccess() || !StringUtils.isEmpty(qs.getMessage())) {
			return qs.getMessage();
		}
		return "status info not available";
	}

	@Override
	public Boolean stopQuery(String queryId) {
		WebResource stopQueryResouce = client.resource(host
				+ CANCEL_REST_PREFIX + queryId);
		ClientResponse response = stopQueryResouce.type(
				MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return gson.fromJson(response.getEntity(String.class), Boolean.class);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public static void main(String[] args) {
		RestQueryEngine r = new RestQueryEngine();
		r.setHost("http://10.1.77.84:8080");
		HiveQueryInputBo b = new HiveQueryInputBo();
		b.setHql("show tables");
		b.setEngineMode("hive");
		b.setDatabase("default");
		b.setUsername("yukang.chen");
		b.setStoreResult(false);
		b.setQueryid("11111");
		LoginServiceImpl.getUsernameToPasswd()
				.put("yukang.chen", "yukang.chen");
		HiveQueryOutputBo bb = r.getQueryResult(b);
		for (String[] ss : bb.getData()) {
			System.out.println(StringUtils.join(ss));
		}
	}
}
