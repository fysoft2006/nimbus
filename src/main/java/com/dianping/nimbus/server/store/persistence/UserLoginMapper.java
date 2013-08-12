package com.dianping.nimbus.server.store.persistence;

import java.util.List;

import com.dianping.nimbus.server.store.domain.UserLogin;

public interface UserLoginMapper {
	
	List<UserLogin> selectUserLoginByUsername(String username);
    
    void insertUserLogin(UserLogin userLogin);
}
