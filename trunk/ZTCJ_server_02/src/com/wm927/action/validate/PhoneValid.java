package com.wm927.action.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.CodeUtils;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 手机验证接口
 * @author chen
 * 修改于2013-10-22
 */
public class PhoneValid extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(PhoneValid.class);
	//用户id
	private String uid;
	//电话号码
	private String phone;
	//验证码
	private String code;
	//邮箱
	private String email;
	
	/**
	 * 发送验证码接口
	 * 第一步：验证字段是否为空
	 * 第三步：向短信发送表插入一条需要发送验证码的短信
	 */
	public void sendValidCode(){
		if(!checkNull()){
			return ;
		}
		//生成随机验证码
		String msgCode = CodeUtils.createCode();
		//获取IP
		String ipAddress = getIpAddr();
		//向手机验证表添加记录
		String phoneSql = "INSERT INTO wm_user_telno (UID,TELNO,CODE,VALIDTIME,VALIDIP)VALUES(?,?,?,?,?)";
		int insertCheckCount = middlewareService.update(phoneSql,uid, phone,msgCode,0,ipAddress);
		
		if(insertCheckCount==0){
			logger.info("INSERT wm_user_telno false on this class --> "+this.getClass().getSimpleName());
			responseInfo("-3","插入手机验证失败");
			return;
		}
		//向短信发送表插入一条需要发送的短信信息
		String msgSql = "INSERT INTO wm_msg_send(TELNO,MSGCONTENT)VALUES(?,?)";
		String msgContent = "欢迎注册智通财经会员，您的验证码:" + msgCode ;
		int insertTelnoCount = middlewareService.update(msgSql, phone,msgContent);
		if(insertTelnoCount==0){
			logger.info("insert wm_msg_send false on this class --> "+this.getClass().getSimpleName());
			responseInfo("-3","插入发送短信失败");
			return;
		}
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("status", "Y");list_value.add(map);
		responseInfo("1","短信以发送，请注意查收",list_value);
	}
	
	/**
	 * 检查验证码接口
	 * 第一步：验证字段是否为空
	 * 第二步：判断当前验证码是否与数据库存在的验证码相同
	 * 第三步：更新用户相关信息
	 * 第四步：查询用户状态表是否存在记录(存在更新，否则插入新记录)
	 * 第五步：更新手机验证表记录为已验证
	 */
	public void checkValidCode(){
		if(DataUtils.checkString(code)){
			responseInfo("-1","验证码能为空");
			return ;
		}
		if(!checkNull()){
			return ;
		}
		//判断用户发送的验证码是否与数据库验证码是否相同
		String checkCodeSql = "SELECT COUNT(*) FROM wm_user_telno WHERE UID=? AND TELNO = ? AND CODE = ?";
		Long count = middlewareService.findCount(checkCodeSql,uid,phone,code);
		if(count == 0){
			responseInfo("-3","验证码错误");
			return ;
		}
		//更新用户相关信息
		String updateUserInfoSql = "UPDATE wm_user_index SET TELENO = ? , STEP = 1 WHERE ID = ?";
		int updateIndexCount = middlewareService.update(updateUserInfoSql,phone,uid);
		if(updateIndexCount==0){
			logger.info("update wm_user_index false on this class --> "+this.getClass().getSimpleName());
			responseInfo("-3","更新用户相关信息失败");
			return;
		}
		
		//返回验证成功
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("status", "Y");list_value.add(map);
		responseInfo("1","验证成功!!",list_value);
	}
	
	/**
	 * 判断手机验证状态
	 * 第一步：判断手机号是否为空
	 * 第二步：验证当前手机号码时候已验证
	 */
	public void checkPhoneState(){
		if(DataUtils.checkString(phone)){
			responseInfo("-1","电话号码不能为空");
			return ;
		}
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		String checkSql = "SELECT COUNT(*) FROM wm_user_index WHERE TELENO = ?";
		Long count = middlewareService.findCount(checkSql, phone);
		if(count == 0){
			map_value.put("state", "N");list_value.add(map_value);
			responseInfo("1","返回成功",list_value);
			
		}else{
			map_value.put("state", "Y");list_value.add(map_value);
			responseInfo("1","返回成功",list_value);
		}
	}
	
	/**
	 * 判断邮箱验证状态
	 * 第一步：判断邮箱是否为空
	 * 第二步：判断邮箱是否已验证
	 */
	public void checkEmailState(){
		if(DataUtils.checkString(email)){
			responseInfo("-1","EMAIL不能为空 ");
			return ;
		}
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		String checkSql = "SELECT COUNT(*) FROM wm_user_index WHERE EMAIL = ?";
		Long count = middlewareService.findCount(checkSql, email);
		if(count == 0){
			map_value.put("state", "N");list_value.add(map_value);
			responseInfo("1","返回成功",list_value);
			
		}else{
			map_value.put("state", "Y");list_value.add(map_value);
			responseInfo("1","返回成功",list_value);
		}
	}

	/**
	 * 验证参数
	 * @return
	 */
	private boolean checkNull(){
		if(DataUtils.checkString(phone)){
			responseInfo("-1","手机号不能为空");
			return false;
		}
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return false;
		}
		final String reg ="\\d*";
		if(!uid.matches(reg)){
			responseInfo("-1","用户ID输入不正确！！");
			return false;
		}
		if(!checkUser(uid)){
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
