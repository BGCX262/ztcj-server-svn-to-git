package com.wm927.dao;

import java.util.List;
import java.util.Map;
/**
 * DAO层公共SQL 接口
 * @author chen
 *
 */
public interface MiddlewareDao {
	
	/** 
	* 执行sql语句 
	* @param sql sql语句 
	* @return 受影响的行数 
	*/ 
	public int update(String sql) ;
	/** 
	   * 执行sql语句 
	   * <code> 
	   * executeUpdate("update user set username = 'kitty' where username = ?", "hello 

kitty"); 
	   * </code> 
	   * @param sql sql语句 
	   * @param param 参数 
	   * @return 受影响的行数 
	   */ 
	 public int update(String sql, Object param) ;
	      
	 /** 
	   * 执行sql语句 
	  * @param sql sql语句 
	  * @param params 参数数组 
	  * @return 受影响的行数 
	  */ 
	 public int update(String sql, Object... params);
	      
	/** 
	 * 执行批量sql语句 
	 * @param sql sql语句 
	 * @param params 二维参数数组 
	 * @return 受影响的行数的数组 
	 */ 
     public int[] batchUpdate(String sql, Object[][] params);  

	/** 
	  * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中 
	  * @param sql sql语句 
	  * @return 查询结果 
	  */ 
	 public List<Map<String, Object>> find(String sql) ;
	      
	/** 
	  * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中 
	  * @param sql sql语句 
	  * @param param 参数 
	  * @return 查询结果 
	  */ 
	public List<Map<String, Object>> find(String sql, Object param);
	      
	/** 
	 * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中 
	 * @param sql sql语句 
	 * @param params 参数数组 
	 * @return 查询结果 
	 */ 
	public List<Map<String, Object>> find(String sql, Object... params);
	      
	/** 
	  * 执行查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中 
	  * @param entityClass 类名 
	  * @param sql sql语句 
	  * @return 查询结果 
	  */ 
	 public <T> List<T> find(Class<T> entityClass, String sql) ;
	 
	 /** 
	  * 执行查询，将每行的结果保存到List中 
	  * @param sql sql语句 
	  * @return 查询结果 
	  */ 
	 public <T>List<T> findList(String sql) ; 
	 /** 
	  * 执行查询，将每行的结果保存到List中 
	  * @param sql sql语句 
	  * @param param 参数 
	  * @return 查询结果 
	  */ 
	 public <T>List<T> findList(String sql,Object params) ; 
	 /** 
	  * 执行查询，将每行的结果保存到List中 
	  * @param sql sql语句 
	  * @param param 参数 
	  * @return 查询结果 
	  */ 
	 public <T>List<T> findList(String sql,Object...params) ; 
	/** 
	  * 执行查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中 
	  * @param entityClass 类名 
	  * @param sql sql语句 
	  * @param param 参数 
	  * @return 查询结果 
	  */ 
	 public <T> List<T> find(Class<T> entityClass, String sql, Object param);
	      
	/** 
	  * 执行查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中 
	  * @param entityClass 类名 
	  * @param sql sql语句 
	  * @param params 参数数组 
	  * @return 查询结果 
	  */ 
	 public <T> List<T> find(Class<T> entityClass, String sql, Object... params) ;
	      
	/** 
	  * 查询出结果集中的第一条记录，并封装成对象 
	  * @param entityClass 类名 
	  * @param sql sql语句 
	  * @return 对象 
	  */ 
	 public <T> T findFirst(Class<T> entityClass, String sql) ;
	      
	/** 
	  * 查询出结果集中的第一条记录，并封装成对象 
	  * @param entityClass 类名 
	  * @param sql sql语句 
	  * @param param 参数 
	  * @return 对象 
	  */ 
	public <T> T findFirst(Class<T> entityClass, String sql, Object param) ;
	      
	/** 
	  * 查询出结果集中的第一条记录，并封装成对象 
	  * @param entityClass 类名 
	  * @param sql sql语句 
	  * @param params 参数数组 
	  * @return 对象 
	  */ 
	  public <T> T findFirst(Class<T> entityClass, String sql, Object... params);
	      
	/** 
	  * 查询出结果集中的第一条记录，并封装成Map对象 
	  * @param sql sql语句 
	  * @return 封装为Map的对象 
	  */ 
	  public Map<String, Object> findFirst(String sql) ;
	      
	/** 
	  * 查询出结果集中的第一条记录，并封装成Map对象 
	  * @param sql sql语句 
	  * @param param 参数 
	  * @return 封装为Map的对象 
	  */ 
	  public Map<String, Object> findFirst(String sql, Object param) ;
	      
	/** 
	  * 查询出结果集中的第一条记录，并封装成Map对象 
	  * @param sql sql语句 
	  * @param params 参数数组 
	  * @return 封装为Map的对象 
	  */ 
	  public Map<String, Object> findFirst(String sql, Object... params) ;
	      
	/** 
	  * 查询某一条记录，并将指定列的数据转换为Object 
	  * @param sql sql语句 
	  * @param columnName 列名 
	  * @return 结果对象 
	  */ 
	  public String findBy(String sql, String columnName);
	      
	/** 
	  * 查询某一条记录，并将指定列的数据转换为Object 
	  * @param sql sql语句 
	  * @param columnName 列名 
	  * @param param 参数 
	  * @return 结果对象 
	  */ 
	 public String findBy(String sql, String columnName, Object param) ;
	      
	/** 
	  * 查询某一条记录，并将指定列的数据转换为Object 
	  * @param sql sql语句 
	  * @param columnName 列名 
	  * @param params 参数数组 
	  * @return 结果对象 
	  */ 
	 public String findBy(String sql, String columnName, Object... params); 
	      
	/** 
	  * 查询某一条记录，并将指定列的数据转换为Object 
	  * @param sql sql语句 
	  * @param columnIndex 列索引 
	  * @return 结果对象 
	  */ 
	 public String findBy(String sql, int columnIndex) ;
	  
	      
	/** 
	  * 查询某一条记录，并将指定列的数据转换为Object 
	  * @param sql sql语句 
	  * @param columnIndex 列索引 
	  * @param param 参数 
	  * @return 结果对象 
	  */ 
	  public String findBy(String sql, int columnIndex, Object param) ;
	      
	/** 
	  * 查询某一条记录，并将指定列的数据转换为Object 
	  * @param sql sql语句 
	  * @param columnIndex 列索引 
	  * @param params 参数数组 
	  * @return 结果对象 
	  */ 
	  public String findBy(String sql, int columnIndex, Object... params);
	  
	  /**
	   * 查询记录条数
	   * @param sql
	   * @return Object记录条数
	   */
	  public Long findCount(String sql );
	  
	  /**
	   * 查询记录条数
	   * @param sql
	   * @param params
	   * @return Object记录条数
	   */
	  public Long findCount(String sql,Object...params );
	  
	  /**
	   *  传入一个map集合sql，key为sql语句，value为sql的参数值,
	   *  但是参数是以数组形式封装传过来(SQL可以是完整的拼接好的，也可以是未拼接带有？等参数的)
	   * @return Map对象，以此存放返回值 (返回map key 还是sql value换为返回结果)
	   */
	  public Map<String, Object> insertOrUpdate(Map<String,Object> sql_map) ;
}
