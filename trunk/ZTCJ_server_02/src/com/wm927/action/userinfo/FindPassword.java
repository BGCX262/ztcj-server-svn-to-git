package com.wm927.action.userinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.action.validate.SpringtoMail;
import com.wm927.commons.CodeUtils;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 找回密码
 * @author chen
 * 修改于2013-10-22
 */
public class FindPassword extends MiddlewareActionService{
	private static final Logger logger = Logger.getLogger(FindPassword.class);
	//电话号码
	private String phone;
	//验证码
	private String code;
	//邮箱
	private String email;
	//日期
	private String date;
	//用户id
	private String uid;
	//新密码
	private String npassword;
	
	/**
	 * 找回密码---发送验证码到手机
	 * 第一步：判断手机号码是否已经绑定
	 * 第二步：生成随机6位不同数字验证码
	 * 第三步：发送验证码到手机短信
	 */
	public void sendCode(){
		if(DataUtils.checkString(phone)){
			responseInfo("-1","电话号码不能为空！");
			return ;
		}
		String checkphone = "SELECT ID FROM wm_user_index  WHERE TELENO =  ?";
		String objid = middlewareService.findBy(checkphone, "ID",phone);
		if(DataUtils.checkString(objid)){
			responseInfo("-1","电话号码没有 绑定！");
			return ;
		}
		//生成随机验证码
		String msgCode = CodeUtils.createCode();
		String insertSql = "INSERT INTO wm_user_telno (UID,CODE,TELNO,VALIDTIME,VALIDIP)VALUES(?,?,?,?,?)";
		int count = middlewareService.update(insertSql,objid,msgCode,phone,DateUtils.getCurrentTime(),getIpAddr());
		if(count == 0){
			responseInfo("-1","验证码插入失败");
			return ;
		}
		
		//向短信发送表插入一条需要发送的短信信息
		String msgSql = "INSERT INTO wm_msg_send(TELNO,MSGCONTENT)VALUES(?,?)";
		String msgContent = "智通财经网已经收到您的找回密码请求，验证码是:"+msgCode+" ,请勿泄露！";
		middlewareService.update(msgSql, phone,msgContent);
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("status", "Y");list_value.add(map);
		responseInfo("1","短信已发送，请注意查收",list_value);
	}
	
	/**
	 * 忘记密码--手机验证是否正确
	 * 第一步:判断号码和验证码是否为空
	 * 第二步：根据号码和验证码去验证数据库是否存在记录
	 */
	public void forgetPasswordForPhone(){
		if(DataUtils.checkString(phone)){
			responseInfo("-1","电话号码不能为空！");
			return ;
		}
		if(DataUtils.checkString(code)){
			responseInfo("-1","验证码不能为空！");
			return;
		}
		String sql = "SELECT ID FROM wm_user_index WHERE TELENO  = ?";
		String objid = middlewareService.findBy(sql, "ID",phone);
		String checkCode = "SELECT COUNT(*) FROM wm_user_telno WHERE UID = ? AND TELNO = ? AND CODE = ?";
		Long count  = middlewareService.findCount(checkCode,objid,phone,code);
		if(count == 0){
			responseInfo("-1","验证码不一致！");
			return ;
		}
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", objid);map.put("status", "Y");list_value.add(map);
		responseInfo("1","手机验证成功",list_value);
	}
	
	/**
	 * 忘记密码--通过 邮箱找回
	 */
	public void forgetPasswordForEmail(){
		if(DataUtils.checkString(email)){
			responseInfo("-1","邮箱不能为空！");
			return;
		}
		String checkemail = "SELECT ID FROM wm_user_index  WHERE EMAIL = ?";
		String objid = middlewareService.findBy(checkemail,"ID",email);
		if(DataUtils.checkString(objid)){
			responseInfo("-3","邮箱没有 绑定！");
			return ;
		}
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		//生成随机验证码
		String msgCode = CodeUtils.createCode();
		//更新用户的状态表中的邮箱验证码为当前生成的验证码
		String updateCode = "INSERT INTO wm_user_email  (UID , CODE, EMAIL , VALIDTIME , VALIDIP) VALUES(?,?,?,?,?)";
		int updateCount = middlewareService.update(updateCode,objid,msgCode,email,DateUtils.getCurrentTime(),getIpAddr());
		logger.info("update forgetPasswordForEmail"+this.getClass().getSimpleName()+" "+updateCount);
		
		//向该用户发送一条邮箱验证的连接
		long longtime = System.currentTimeMillis()/1000;
		boolean sendok = springtoMail.send(objid,msgCode,longtime,email,"1");
		String msg = "邮箱已发送";
		if(sendok){
			map_value.put("status", "Y");
		}else{
			map_value.put("status", "N");
			msg = "邮箱发送失败，检查邮箱是否填写正确";
		}
		list_value.add(map_value);
		responseInfo("1",msg,list_value);
	}
	
	/**
	 * 忘记密码---验证邮箱连接是否有效
	 */
	public void checkEmail(){
		Map<String,Object> checkMap = new HashMap<String,Object>();
		checkMap.put("时间不能为空", date);checkMap.put("用户ID不能为空", uid);
		checkMap.put("验证码为空", code);
		if(!checkDate()){
			responseInfo("-1","连接已经失效！");
			return ;
		}
		if(!checkUser(uid)){
			return;
		}
		String checkCode = "SELECT COUNT(*) FROM wm_user_email WHERE CODE = ? AND UID = ?";
		Long emailCodeCheck = middlewareService.findCount(checkCode,code,uid);
		if(emailCodeCheck==0){
			responseInfo("-3","验证码错误！");
			return ;
		}
		responseInfo("1","有效邮箱");
	}
	
	/**
	 * 忘记密码--重置密码
	 */
	public void resetPassword(){
		if(DataUtils.checkString(npassword)){
			responseInfo("-1","新密码不能为空！");
			return ;
		}
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空！");
			return ;
		}
		if(!checkUser(uid)){
			return;
		}
		//修改mysql密码
		String updatePassword = "UPDATE wm_user_index SET PASSWORD = ? WHERE ID = ?";
		int ucount = middlewareService.update(updatePassword,npassword,uid);
		
		
		logger.info(" update resetPassword"+ this.getClass().getSimpleName()+" UID ==> "+uid);
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map  = new HashMap<String,Object>();
		if(ucount == 0){
			map.put("status", "N");
		}else{
			map.put("status", "Y");
		}
		list_value.add(map);
		responseInfo("1","重置成功",list_value);
	}
	

	/**
	 * 验证时间格式是否正确,并与当前时间判断是否在48小时之内
	 * @return
	 */
	private boolean checkDate(){
		long nowtime = System.currentTimeMillis()/1000;
		long parsetime = 48*3600;
		Long parsedate = null;
		try{
			parsedate = Long.parseLong(date);
		}catch(Exception e){
			responseInfo("3","日期格式不正确");
			return false;
		}
		return nowtime - parsedate < parsetime;
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
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getNpassword() {
		return npassword;
	}
	public void setNpassword(String npassword) {
		this.npassword = npassword;
	}
	public SpringtoMail springtoMail;

	public void setSpringtoMail(SpringtoMail springtoMail) {
		this.springtoMail = springtoMail;
	}
}
