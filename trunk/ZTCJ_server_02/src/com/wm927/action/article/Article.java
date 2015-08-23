package com.wm927.action.article;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 资讯
 * @author chen
 *
 */
public class Article extends MiddlewareActionService{
	private static final Logger logger = Logger.getLogger(Article.class);
	private String uid;
	
	public void execute(){
		if(!checkNull()){
			return ;
		}
		//关注国家表取出关注的国家
		String relationCountrySql = "SELECT COUNTRYNAME,COUNTRYCODE FROM wm_user_attentionCountry WHERE UID = ?";
		//String relationCounttryNameSql = "select COUNTRYNAME from wm_user_attentionCountry where UID=?";
		List<Map<String,Object>> list_country = middlewareService.find(relationCountrySql, uid);
		if(list_country == null || list_country.isEmpty()){
			responseInfo("3","关注国家为空!!");
			return;
		}
		//关注货币表取出关注的货币
		String relationCurrencySql = "SELECT NAME FROM wm_user_attentionCurrency WHERE UID = ?";
		List<Map<String,Object>> list_currency = middlewareService.find(relationCurrencySql, uid);
		if(list_currency == null || list_currency.isEmpty()){
			responseInfo("3","关注货币为空!!");
			return;
		}
		//List<String> list_countryName = middlewareService.findList(relationCounttryNameSql,uid);
		
		//根据关注的国家代码获取该国家已经公布的前10条财经数据(按最新时间排序)
		StringBuffer countrySql = new StringBuffer();
		countrySql.append("SELECT * FROM wm_data_economic WHERE ");
		
		for(Map<String,Object> list:list_country){
			countrySql.append(" ENGLISHAREANAME='"+list.get("COUNTRYCODE")+"' or");
		}
		countrySql.replace(countrySql.length()-2, countrySql.length(), " ORDER BY CREATETIME DESC LIMIT 10");
		List<Map<String,Object>> countryList_value = middlewareService.find(countrySql.toString());
		//根据关注的货币和国家的中文名字获取最新的10天资讯(按最新时间排序)
		StringBuffer currencySql = new StringBuffer();
		currencySql.append("SELECT * FROM zt_article_info_datasoure_cn WHERE ");
		for(Map<String,Object> list:list_currency){
			currencySql.append(" ARTICLETITLE LIKE '%"+list.get("NAME")+"%' OR" );
		}
		for(Map<String,Object> list:list_country){
			currencySql.append(" ARTICLETITLE LIKE '%"+list.get("COUNTRYNAME")+"%' OR" );
		}
		currencySql.replace(currencySql.length()-2, currencySql.length(), " ORDER BY CREATETIME DESC LIMIT 10");
		List<Map<String,Object>> currencyList_value = middlewareService.find(currencySql.toString());
		List<List<Map<String,Object>>> list_value = new ArrayList<List<Map<String,Object>>>();
		list_value.add(countryList_value);
		list_value.add(currencyList_value);
		
		responseInfo("0","数据返回成功",list_value);
	}
	
	/**
	 * 验证用户ID
	 * @return
	 */
	private boolean checkNull(){
		final String reg ="\\d*";
		if(DataUtils.checkString(uid)){
			responseInfo("1","该用户ID不能为空");
			return false;
		}
		if(!uid.matches(reg)){
			responseInfo("1","用户ID输入不正确！！");
			return false;
		}
		
		String userCount = "SELECT ID from wm_user_index WHERE ID=?";
		String userObj = middlewareService.findBy(userCount,"ID",uid);
		if(DataUtils.checkString(userObj)){
			responseInfo("1","用户不存在！！");
			return false;
		}
		return true;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
}
