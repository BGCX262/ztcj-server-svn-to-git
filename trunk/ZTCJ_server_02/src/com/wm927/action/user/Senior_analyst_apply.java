package com.wm927.action.user;

import java.util.Map;

import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;
/**
 * 高级分析师申请
 * @author 郭瑜嘉
 * 2014-3-10
 */
public class Senior_analyst_apply extends MiddlewareActionService{
	private String content;
	private String uid;
	
	public void apply(){
		if(DataUtils.checkString(content)){
			responseInfo("-1","请填写申请理由");
			return;
		}
		if(DataUtils.checkString(uid)){
			responseInfo("-1","uid不能为空");
			return;
		}
		
		String sql = "UPDATE wm_user_roleAnalyst SET SUPER_AUTHENTICATE_APPLY_TIME = ?," +
		"SUPER_AUTHENTICATE_STATUS = 1,SUPER_AUTHENTICATE_APPLY_CONTENT = ? WHERE UID = ?";
		int result = middlewareService.update(sql, DateUtils.getCurrentTime(),content,uid);
		if(result > 0){
			responseInfo("1","您提交的高级分析师申请正在审核，我们会在3个工作日内处理完毕，并会以消息的方式通知给您，请耐心等待。");
		}else{
			responseInfo("-1","申请失败，服务器繁忙，请重试");
		}
	}

	public void getStatus(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","uid不能为空");
			return;
		}
		String sql = "SELECT SUPER_AUTHENTICATE_STATUS,SUPER_AUTHENTICATE_FAILED_CONTENT FROM wm_user_roleAnalyst WHERE uid = ?";
		Map<String,Object> result = middlewareService.findFirst(sql,uid);
		responseInfo("1","高级分析状态查询成功0=未申请1=申请中2=通过3=没过",result);
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
