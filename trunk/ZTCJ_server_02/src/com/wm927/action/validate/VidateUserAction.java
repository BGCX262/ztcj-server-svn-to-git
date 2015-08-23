package com.wm927.action.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

//type参数(0:代表用户名,1:代表邮箱(需要匹配),2:电话号码(需要匹配)
//keyword参数:对于0为username,1为email,2为teleno
public class VidateUserAction  extends MiddlewareActionService{
	private String type;
	private String keyword;
	public void execute(){
			String word = "";
			if("0".equals(type)){
				word = "USERNAME" ;
				if(DataUtils.checkString(keyword)){
					responseInfo("-1","用户名为空");
					return;
				}
			}
			if("1".equals(type)){
				word = "EMAIL" ;
				if(DataUtils.checkString(keyword)){
					responseInfo("-1","邮箱为空");
					return;
				}
			}
			
			if("2".equals(type)){
				word = "TELENO" ;
				if(DataUtils.checkString(keyword)){
					responseInfo("-1","电话号码为空");
					return;
				}
			}
			check(word,keyword);
	}
	
	public void check(String word,String keyword){
		List<Map<String, Object>> value_list = new ArrayList<Map<String,Object>>();
		Map<String, Object> value_map = new HashMap<String, Object>();
		String sql  = "SELECT COUNT(*)  FROM wm_user_index WHERE "+word+" = ? ";
		Long count  = middlewareService.findCount(sql,keyword);
		if( count == 0){
			//不存在，可以注册
			value_map.put("status", "Y");
			value_list.add(value_map);
			responseInfo("1", "可以添加", value_list);
			return;
		}
		value_map.put("status", "N");
		value_list.add(value_map);
		if("0".equals(type)){
			responseInfo("-1", "用户名已存在",value_list );
			return;
		}else 
		 if("1".equals(type)){
			responseInfo("-1", "该邮箱已注册",value_list );
			return;
		}else{
			responseInfo("-1", "该号码已存在",value_list );
			return;
		}
		
	}
	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
}
