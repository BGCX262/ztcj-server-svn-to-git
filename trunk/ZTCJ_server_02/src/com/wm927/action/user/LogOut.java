package com.wm927.action.user;

import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 登陆/注销日志
 * @author 郭瑜嘉
 *
 */
public class LogOut extends MiddlewareActionService{
	private static final Logger logger = Logger.getLogger(LogOut.class);
	private String uid;//用户ID
	private String callback;
	/**
	 * 用户注销
	 */
	public void execute() {
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户名不能为空",callback);
			return;
		}
		String updateOnline = "UPDATE wm_user_info SET ISONLINE = 1 WHERE UID=?";
		int count = middlewareService.update(updateOnline,uid);
		if(count == 0){
			logger.info(uid +" 用户注销失败 ---->");
		}
		responseInfo("1","用户注销成功",callback);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}
	
	
}
