package com.wm927.service.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.util.StringUtils;
import com.wm927.commons.DataUtils;
import com.wm927.commons.PageUtils;
import com.wm927.commons.RequestUtils;
import com.wm927.commons.ServerDomainUtils;
import com.wm927.service.MiddlewareService;

public class MiddlewareActionServiceInterface {
	/**
	 * 使用MySql数据源
	 */
	protected MiddlewareService middlewareService;
	
	public void setMiddlewareService(MiddlewareService middlewareService) {
		this.middlewareService = middlewareService;
	}
	
	protected String responseToJsonObject(Object value ){
		ObjectMapper mapper = new ObjectMapper();
		String jsonObject =  "";
		try {
			jsonObject = mapper.writeValueAsString(value);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * 将数据以XML格式的形式发送到前端网页
	 * @param info
	 */
	protected void responseToXml(Object info) {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/xml;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out =null;
		try {
			out = response.getWriter();
			out.write( info.toString( ) );
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(out!=null){
					out.close();
				}
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 将数据以HTML形式发送到前端网页
	 * @param info
	 */
	protected void responseToHtml(Object info) {
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out =null;
		try {
			out = response.getWriter();
			out.print(info.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(out!=null){
					out.close();
				}
			} catch (Exception e) {
			}
		}
	}

	protected String getParameter( String key ){
		HttpServletRequest request = ServletActionContext.getRequest( );
		return request.getParameter( key );
	}
	
	protected String getParameterOrDefault(String key,String defaultValue){
		String result = ServletActionContext.getRequest().getParameter(key);
		if(!StringUtils.isEmpty(result)){
			return result;
		}else{
			return defaultValue;
		}
	}
	
	protected HttpServletRequest getRequest( ){
		return ServletActionContext.getRequest( );
	}
	/**
	 * 获取IP地址
	 * @return
	 */
	protected String getIpAddr( ){
		HttpServletRequest request = getRequest( );
		return RequestUtils.getIpAddr(request);
	}
	/**
	 * 获取分页的data数据
	 * @param page
	 * @param number
	 * @param sql
	 * @return
	 */
	protected List<Map<String,Object>> findPageInfo(String page,String number,String sql){
		//拼接分页sql
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append(sql);
		sbuilder.append(" LIMIT ");
		sbuilder.append((PageUtils.getPage(page)-1)*PageUtils.getPageCount(number));
		sbuilder.append(" , ");
		sbuilder.append( PageUtils.getPageCount(number));
				
		return middlewareService.find(sbuilder.toString());
	}
	
	/**
	 * 获取分页的data页数
	 * @param page
	 * @param number
	 * @param sql
	 * @return
	 */
	protected Map<String,Object> findPageSize( String page,String number,String sql){
		String countSql = getCountSql(sql);
		Long totalCount = middlewareService.findCount(countSql);
		Map<String,Object> pageInfoMap = new HashMap<String,Object>();
		pageInfoMap.put("pageIndex", PageUtils.getPage(page));pageInfoMap.put("pageSize", PageUtils.getPageCount(number));
		pageInfoMap.put("totalRecords", totalCount);pageInfoMap.put("totalPages", PageUtils.getTotalPage(totalCount,number));
		return pageInfoMap;
	}
	/**
	 * 获取分页SQL
	 * @param sql
	 * @return
	 */
	private String getCountSql(String sql){
		
		StringBuilder sb = new StringBuilder();
		if(checkMin_Max_Sum(sql)){
			sb.append("SELECT COUNT(*) FROM ( ");
			sb.append(sql);
			sb.append(") as aa");
			return sb.toString();
		}
		sb.append("SELECT COUNT(*)");
		int index = sql.lastIndexOf("FROM");
		if(index!=-1){
			sb.append(" "+sql.substring(index));
		}else{
			sb.append(" "+sql.substring(sql.indexOf("FROM")));
		}
		String endIndexStr = sb.toString().toUpperCase();
		int endIndex = endIndexStr.lastIndexOf("GROUP");
		if(endIndex!=-1){
			return sb.substring(0, endIndex);
		}
		return sb.toString();
	}
	
	
	protected boolean checkMin_Max_Sum(String sql){
		if(StringUtils.isEmpty(sql))
			return false;
		String newsql = sql.toLowerCase();
		//有些sql含有求最大值，最小值，求和运算，所以必须先将其分组再计算总数
		if(newsql.indexOf("MIN")!=-1||newsql.indexOf("MAX")!=-1||newsql.indexOf("SUM")!=-1){
			return true;
		}
		return false ;
	}
	
	
	
	/**
	 * 返回给前台json数据，默认成功或者失败，没有数据返回
	 * @param code
	 * @param msg
	 */
	protected void responseInfo(String code,String msg,String ...callback ){
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("data", new ArrayList<String>());
		responseAllInfo(code,msg,map_value,callback);
	}
	/**
	 * 返回给前台json数据，有内容的json数据
	 * @param code
	 * @param msg
	 * @param obj
	 */
	protected void responseInfo(String code,String msg,Object obj,String ...callback){
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("data", obj);
		responseAllInfo(code,msg,map_value,callback);
	}
	
	/**
	 * 返回给前台json数据(返回分页数据)
	 * @param code 标识码
	 * @param msg 返回信息
	 * @param sql sql
	 * @param pageinfo
	 */
	protected void responseInfo(String code,String msg,Object obj,Object pageinfo,String ... callback){
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("data", obj);
		map_value.put("pageinfo", pageinfo);
		responseAllInfo(code,msg,map_value,callback);
	}
	private void responseAllInfo(String code,String msg,Map<String,Object> map_value,String ...callback){
		map_value.put("statusCode", code);
		map_value.put("statusMsg", msg);
		map_value.putAll(ServerDomainUtils.map_value);
		responseInfo(map_value,callback);
	}
	/**
	 * 数据返回
	 * @param map_value
	 * @param callback
	 */
	private void responseInfo(Map<String,Object> map_value ,String ... callback){
		if(callback.length == 0){
			//没有回调域json数据返回
			responseToHtml(responseToJsonObject(map_value));
		}else{
			String back = Arrays.asList(callback).get(0);
			if( DataUtils.checkString(back) ){
				//传入回调域，若普通json返回则回调域内容为null
				responseToHtml(responseToJsonObject(map_value));
			}else{
				//传入回调域，有回调域内容
				responseToHtml(back+"("+responseToJsonObject(map_value)+")");
			}
		}
	}
	
	/**
	 * 返回给前台json数据(返回分页数据)
	 * @param page 分页
	 * @param count 每页显示的数目
	 * @param msg 返回信息
	 * @param sql sql
	 * 
	 */
	protected void responseInfo(String page,String size,String code,String msg,String sql){
		
		List<Map<String,Object>> list_value = findPageInfo(page,size,sql);
		Map<String,Object> pageInfoMap = findPageSize(page,size,sql);
		responseInfo(code,msg,list_value,pageInfoMap);
	}
	
}
