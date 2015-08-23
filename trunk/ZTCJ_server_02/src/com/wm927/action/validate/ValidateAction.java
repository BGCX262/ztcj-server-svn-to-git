package com.wm927.action.validate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.CodeUtils;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

public class ValidateAction  extends MiddlewareActionService {
	Logger log = Logger.getLogger(ValidateAction.class.getName());
	
	private SpringtoMail springtoMail;
	private String uid;
	private String email;
	private String type ;
	
	public void setSpringtoMail(SpringtoMail springtoMail) {
		this.springtoMail = springtoMail;
	}

	public void execute(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID为空");
			return;
		}
		
		if(DataUtils.checkString(email)){
			responseInfo("-1","邮箱为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String code = CodeUtils.createCode();
		long time = System.currentTimeMillis()/1000;
		//邮箱激活这里，因为官网跟社区的地址不同，所以需要判断
		String ttype = "0";
		if(!DataUtils.checkString(type)){
			ttype = "2";
		}
		boolean send = springtoMail.send(uid,code,time,email,ttype);
		
		String vlidatesql = "INSERT INTO wm_user_email(EMAIL,UID,CODE,VALIDTIME,VALIDIP) VALUES (?,?,?,?,?) "; 
		middlewareService.update(vlidatesql, email,uid,code,DateUtils.getCurrentTime(),getIpAddr());
		Map<String,Object> value_map = new HashMap<String, Object>();
		List<Map<String,Object>> value_list = new ArrayList<Map<String,Object>>();
		value_list.add(value_map);
		if(send == true){
			responseInfo("1", "邮件发送成功");
		}else{
			responseInfo("-1", "邮件发送失败");
		}
}

	public boolean check_Time(String data_Time) throws Exception {
		boolean isTrue = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long time_size = 48*60 * 60 * 1000;
		Date nowDate = new Date();// 当前时间
		Date DataDate = sdf.parse(data_Time);
		// 判定是否在规定时间内
		if ((nowDate.getTime() - DataDate.getTime() < time_size)) {
			isTrue = true;
		}
		return isTrue;
	   }

public String getUid() {
	return uid;
}

public void setUid(String uid) {
	this.uid = uid;
}

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getType() {
	return type;
}

public void setType(String type) {
	this.type = type;
}

}
