package com.wm927.action.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.wm927.commons.CodeUtils;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;


public class EmailSendAgain  extends MiddlewareActionService {
	Logger log = Logger.getLogger(EmailSendAgain.class.getName());
	
	private SpringtoMail springtoMail;
	private String uid;
	
	public void setSpringtoMail(SpringtoMail springtoMail) {
		this.springtoMail = springtoMail;
	}
	
	public void execute(){
		Map<String,Object> value_map = new HashMap<String, Object>();
		List<Map<String,Object>> value_list = new ArrayList<Map<String,Object>>();
			if(DataUtils.checkString(uid)){
				responseInfo("-1","用户ID为空");
				return;
			}
			if(!checkUser(uid))
				return;

			String email_sql  = "SELECT EMAIL FROM wm_user_index where ID = ?";
			String email = middlewareService.findBy(email_sql,"EMAIL", uid);
			if(StringUtils.isEmpty(email)){
				//因为有些人第一次注册绑定邮箱，是没有在index表中加入验证信息的邮箱
				email_sql = "SELECT EMAIL FROM wm_user_email where UID = ? ORDER BY ID DESC LIMIT 1";
				email = middlewareService.findBy(email_sql,"EMAIL", uid);
				if(StringUtils.isEmpty(email)){
					responseInfo("1","该用户的邮箱不存在");
					return;
				}
			}
			String code = CodeUtils.createCode();
			long time = System.currentTimeMillis()/1000;
			String insertEmailSql = "INSERT INTO wm_user_email (UID,EMAIL,CODE,VALIDTIME,VALIDIP) VALUES(?,?,?,?,?)";
			boolean sendok = springtoMail.send(uid,code,time,email,"0");
			String msg = "邮件重新发送成功";
			if(sendok){
				value_map.put("status", "Y");
				//邮箱发送成功，更新验证码
				middlewareService.update(insertEmailSql,uid,email,code,DateUtils.getCurrentTime(),getIpAddr());
			}else{
				value_map.put("status", "N");
				msg = "邮件发送失败，检查邮箱填写是否正确";
			}
			
			value_list.add(value_map);
			responseInfo("1", msg, value_list);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
}

