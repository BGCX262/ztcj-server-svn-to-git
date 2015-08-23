package com.wm927.action.userinfo;

import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 短信验证状态
 * @author chen
 * 修改于2013-10-22
 */
public class MessageState extends MiddlewareActionService{
	//用户id
	private String uid;
	//0是取消，1是开通
	private String state;
	/**
	 * 第一步：判断用户id是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：更新短信开通状态
	 */
	public void execute(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		int state1 = 0;
		String stateContent = "开通短信状态成功";
		String msgContent = "尊敬的用户，您已开通由智通财经为您提供的”30天财经数据免费短信提醒服务“，如需取消，请登录智通财经网www.wm927.com的个人中心进行取消。";
		try{
			state1 = Integer.parseInt(state);
		}catch(Exception e){
			responseInfo("-1","状态不能为空");
			return;
		}
		if(state1==0){
			 stateContent = "取消短信状态成功";
			 msgContent = "尊敬的用户，您的”30天财经数据免费短信提醒服务“已取消，如需开通，请登录智通财经网www.wm927.com的个人中心进行开通。";
		}
		if(!checkUser(uid)){
			return;
		}
		String sql = "UPDATE wm_user_info SET RECIEVEMSG =?  where UID =? ";
		int count = middlewareService.update(sql,state1,uid);
		if(count==0){
			responseInfo("-3","更新短信状态失败");
			return;
		}
		String findTelno = "SELECT TELENO FROM wm_user_index WHERE ID = ?";
		String telno = middlewareService.findBy(findTelno, "TELENO",uid);
		String insertMsg = "INSERT INTO wm_msg_send(TELNO,CONTENT)VALUES(?,?)";
		middlewareService.update(insertMsg,telno,msgContent);
		responseInfo("1",stateContent);
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

}
