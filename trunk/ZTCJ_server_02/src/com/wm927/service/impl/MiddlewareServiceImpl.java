package com.wm927.service.impl;

import java.util.List;
import java.util.Map;

import com.wm927.dao.MiddlewareDao;
import com.wm927.service.MiddlewareService;

/**
 * 公共服务层包 实现
 * @author chen
 *
 */
public class MiddlewareServiceImpl implements MiddlewareService{
	private MiddlewareDao middlewareDao ;

	public void setMiddlewareDao(MiddlewareDao middlewareDao) {
		this.middlewareDao = middlewareDao;
	}
	public int update(String sql) {
		return middlewareDao.update(sql);
	}

	public int update(String sql, Object param) {
		return middlewareDao.update(sql,param);
	}

	public int update(String sql, Object... params) {
		return middlewareDao.update(sql, params);
	}

	public int[] batchUpdate(String sql, Object[][] params) {
		return middlewareDao.batchUpdate(sql, params);
	}

	public List<Map<String, Object>> find(String sql) {
		return middlewareDao.find(sql);
	}

	public List<Map<String, Object>> find(String sql, Object param) {
		return middlewareDao.find(sql, param);
	}
	


	public List<Map<String, Object>> find(String sql, Object... params) {
		return middlewareDao.find(sql, params);
	}

	public <T> List<T> find(Class<T> entityClass, String sql) {
		return middlewareDao.find(entityClass, sql);
	}

	public <T> List<T> find(Class<T> entityClass, String sql, Object param) {
		return middlewareDao.find(entityClass, sql, param);
	}

	public <T> List<T> find(Class<T> entityClass, String sql, Object... params) {
		return middlewareDao.find(entityClass, sql, params);
	}

	public <T> T findFirst(Class<T> entityClass, String sql) {
		return middlewareDao.findFirst(entityClass, sql);
	}

	public <T> T findFirst(Class<T> entityClass, String sql, Object param) {
		return middlewareDao.findFirst(entityClass, sql, param);
	}

	public <T> T findFirst(Class<T> entityClass, String sql, Object... params) {
		return middlewareDao.findFirst(entityClass, sql, params);
	}

	public Map<String, Object> findFirst(String sql) {
		return middlewareDao.findFirst(sql);
	}

	public Map<String, Object> findFirst(String sql, Object param) {
		return middlewareDao.findFirst(sql, param);
	}

	public Map<String, Object> findFirst(String sql, Object... params) {
		return middlewareDao.findFirst(sql, params);
	}

	public String findBy(String sql, String columnName) {
		return middlewareDao.findBy(sql, columnName);
	}

	public String findBy(String sql, String columnName, Object param) {
		return middlewareDao.findBy(sql, columnName, param);
	}

	public String findBy(String sql, String columnName, Object... params) {
		return middlewareDao.findBy(sql, columnName, params);
	}

	public String findBy(String sql, int columnIndex) {
		return middlewareDao.findBy(sql, columnIndex);
	}

	public String findBy(String sql, int columnIndex, Object param) {
		return middlewareDao.findBy(sql, columnIndex, param);
	}

	public String findBy(String sql, int columnIndex, Object... params) {
		return middlewareDao.findBy(sql, columnIndex, params);
	}
	
	public Long findCount(String sql) {
		return middlewareDao.findCount(sql);
	}
	public Long findCount(String sql,Object...params) {
		return middlewareDao.findCount(sql,params);
	}
	
	public <T> List<T> findList(String sql) {
		return middlewareDao.findList(sql);
	}

	public <T> List<T> findList(String sql, Object params) {
		return middlewareDao.findList(sql, params);
	}

	public <T> List<T> findList(String sql, Object... params) {
		return middlewareDao.findList(sql, params);
	}
	
	/** 
	 * 传入一个map集合sql，key为sql语句，value为sql的参数值,
	 * 但是参数是以数组形式封装传过来(SQL可以是完整的拼接好的，也可以是未拼接带有？等参数的)
	 */
	public Map<String, Object> insertOrUpdate(Map<String,Object> sql_map) {
		if(sql_map==null)
			return null;
		return middlewareDao.insertOrUpdate(sql_map);
	}
	
}
