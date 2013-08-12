package com.dianping.nimbus.server.store.persistence;

import java.util.List;

import com.dianping.nimbus.server.store.domain.QueryFavorite;

public interface QueryFavoriteMapper {
	List<QueryFavorite> selectQueryFavoriteByUsername(String username);

	void insertQueryFavorite(QueryFavorite qf);
}
