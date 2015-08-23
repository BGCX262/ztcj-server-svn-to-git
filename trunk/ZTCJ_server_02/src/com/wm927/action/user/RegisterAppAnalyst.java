package com.wm927.action.user;

import com.wm927.service.impl.MiddlewareActionService;

public class RegisterAppAnalyst extends MiddlewareActionService{
	private String uid;
	/**
	 * 注册成为APP分析师
	 * .....
	 * N，在实战圈子表插入一条记录叫在线答疑
	 */
	public void execute(){
		String sql = "INSERT INTO wm_blog_circle (UID,CONTENT,CLASSID,PURVIEWID,ADDIP)VALUES(?,?,?,?,?)";
		middlewareService.update(sql,uid,"在线答疑",1,0,getIpAddr());
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
}

