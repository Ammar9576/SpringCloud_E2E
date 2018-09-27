package com.nbc.custom_reports.repository.olympics;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConvergenceRepositoryImpl implements ConvergenceRepository{
	@Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
	
	public Long lookupTADUser() {
        return jdbcTemplate.queryForObject("select user_id from onair.app_user where ident = '206494818'", new HashMap<String, Object>(), Long.class);
    }
}
