package com.dianping.nimbus.client.service;

import com.dianping.nimbus.client.bo.LoginTokenBo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {
	void isAuthenticated(String tokenid, AsyncCallback<Boolean> callback);
	
	void authenticate(String username, String password, AsyncCallback<LoginTokenBo> callback);
	
	void logout(String tokenid, AsyncCallback<Boolean> callback);
	
	public static final class Util {
		private static LoginServiceAsync instance;

		public static final LoginServiceAsync getInstance() {
			if (instance == null) {
				instance = (LoginServiceAsync) GWT.create(LoginService.class);
			}
			return instance;
		}

		private Util() {
		}
	}
}
