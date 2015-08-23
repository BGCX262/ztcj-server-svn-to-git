package com.wm927.action.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;


public class ActivityMailBoxAction extends MiddlewareActionService {
	Logger log = Logger.getLogger(ActivityMailBoxAction.class.getName());
	
	private SpringtoMail springtoMail;
	private String uid;
	private String code;
	private String date;

	public void setSpringtoMail(SpringtoMail springtoMail) {
		this.springtoMail = springtoMail;
	}
	
	public void execute(){
		Map<String,Object> value_map = new HashMap<String, Object>();
		List<Map<String,Object>> value_list = new ArrayList<Map<String,Object>>();
			if(DataUtils.checkString(code)){
				responseInfo("-1","验证码为空");
				return;
			}
			if(DataUtils.checkString(uid)){
				responseInfo("-1","用户ID为空");
				return;
			}
			if(DataUtils.checkString(date)){
				responseInfo("-1","时间不能为空");
			}
			if(!checkUser(uid))
				return;
			
			long emailDate = Long.parseLong(date);
			long time_size = 48 * 60 * 60 ;
			long currentDate = System.currentTimeMillis()/1000;
			String emailInfoSql = "SELECT EMAIL,CODE FROM wm_user_email WHERE UID = ?  ORDER BY ID DESC LIMIT 1";
			Map<String,Object> emailInfo = middlewareService.findFirst(emailInfoSql ,uid);
			if(emailInfo == null || emailInfo.isEmpty()){
				responseInfo("-1","未填写邮箱，请检查");
				return ;
			}
			//验证邮箱是否过期
			if (( currentDate - emailDate > time_size)) {
				responseInfo("-3","激活超时");
				boolean sendok = springtoMail.send(uid,code,currentDate,emailInfo.get("email")+"","0");
				if(!sendok)
					responseInfo("-3","输入的邮箱有误");
				return;
			}
			
			/**
			 * 验证验证码是否正确
			 */
			if(code.equals(emailInfo.get("code"))){
				String registTime = DateUtils.getCurrentTime();
				String updateEmailInfo = "UPDATE wm_user_email SET VALIDTIME =?,VALIDSTATE = ?,VALIDIP = ?  WHERE UID = ?";
				middlewareService.update(updateEmailInfo, registTime,1,getIpAddr(),uid);
				String updatesql =  "UPDATE wm_user_index SET EMAIL = ?, LASTUPDATETIME = ?,STEP = 1,LASTUPDATEIP = ? WHERE ID = ? ";
				middlewareService.update(updatesql,emailInfo.get("email"), registTime,getIpAddr(),uid);
				value_map.put("status", "Y");
				value_list.add(value_map);
				responseInfo("1", "邮件激活成功");
			}else{
				value_map.put("status", "N");
				value_list.add(value_map);
				responseInfo("-1", "验证码不对");
			}
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
}
