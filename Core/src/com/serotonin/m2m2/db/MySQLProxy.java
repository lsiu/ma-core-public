/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.db;

import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.serotonin.db.DaoUtils;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;

public class MySQLProxy extends BasePooledProxy {
    @Override
    protected String getUrl(String propertyPrefix) {
        String url = super.getUrl(propertyPrefix);
        if (url.indexOf('?') > 0)
            url += "&";
        else
            url += "?";
        url += "useUnicode=yes&characterEncoding=" + Common.UTF8;
        return url;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MYSQL;
    }

    @Override
    protected String getDriverClassName() {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    public double applyBounds(double value) {
        if (Double.isNaN(value))
            return 0;
        if (value == Double.POSITIVE_INFINITY)
            return Double.MAX_VALUE;
        if (value == Double.NEGATIVE_INFINITY)
            return -Double.MAX_VALUE;

        return value;
    }

    @Override
    public void executeCompress(ExtendedJdbcTemplate ejt) {
        // no op
    }

    //    @Override
    //    public String getPaginationSql(int offset, int count) {
    //        return " limit " + offset + "," + count;
    //    }

    @Override
    public boolean tableExists(ExtendedJdbcTemplate ejt, String tableName) {
        try {
            ejt.execute("select count(*) from " + tableName);
        }
        catch (DataAccessException e) {
            if (e.getCause() instanceof SQLException) {
                SQLException se = (SQLException) e.getCause();
                // This state means a missing table.
                return !"42S02".equals(se.getSQLState());
            }
            throw e;
        }
        return true;
    }

    @Override
    public <T> List<T> doLimitQuery(DaoUtils dao, String sql, Object[] args, RowMapper<T> rowMapper, int limit) {
        return dao.query(getLimitQuerySql(sql, limit), args, rowMapper);
    }

    private String getLimitQuerySql(String sql, int limit) {
        if (limit > 0)
            sql += " limit " + limit;
        return sql;
    }

    @Override
    public String getTableListQuery() {
        return "show tables";
    }
}
