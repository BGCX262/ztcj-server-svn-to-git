package com.wm927.action.data;

import org.apache.log4j.Logger;

import com.mchange.v2.codegen.bean.ClassInfo;
import com.wm927.commons.DataUtils;
import com.wm927.commons.Decoder;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 喊单是否公开
 * @author chen
 *
 */
public class CallInfo extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(ClassInfo.class);
	private String type ;//0公开，1关闭
	private String uid;
	private String anacontent;
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","修改喊单权限类型不能为空"},new Object[]{uid,type}))
			return;
		int ttype = DataUtils.praseNumber(type, 0);
		String sql = "UPDATE wm_user_info SET OPENCALL= ? WHERE UID = ?";
		int count = middlewareService.update(sql,ttype,uid);
		if(count == 0){
			logger.info("修改喊单权限失败 ---->"+uid);
		}
		responseInfo("1","成功");
	}
	/**
	 * 修改分析师公告
	 */
	public void updateAnalystAnnoun(){
		if(!checkNull(new Object[]{"用户ID不能为空","公告的内容不能为空"},new Object[]{uid,anacontent}))
			return;
		String sql = "UPDATE wm_user_info SET ANNOUNCEMENT= ? WHERE UID = ?";
		int count = middlewareService.update(sql,anacontent,uid);
		if(count == 0){
			logger.info("修改喊单权限失败 ---->"+uid);
		}
		responseInfo("1","成功");
	}
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAnacontent() {
		return anacontent;
	}
	public void setAnacontent(String anacontent) {
		if(!DataUtils.checkString(anacontent))
			anacontent = Decoder.decode(anacontent);
		this.anacontent = anacontent;
	}
	
}
