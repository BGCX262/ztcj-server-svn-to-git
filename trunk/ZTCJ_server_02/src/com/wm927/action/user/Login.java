package com.wm927.action.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.ResponseCodeUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 用户登录与返回信息接口
 * @author chen
 * 修改于2013-10-22
 */
public class Login extends MiddlewareActionService{
	//用户名
	private String username;
	//用户密码
	private String password;
	private String uid;
	//0代表用户名登录 1代表手机登录 2代表邮箱登录,3代表cookie登录
	private String loginstate;
	private String callback;
	private static final Logger logger = Logger.getLogger(Login.class);
	
	/**
	 * 登录接口 
	 * 第一步：判断字段是否为空
	 * 第二步：判断登录状态是用户名登录还是邮箱登录还是手机登录
	 * 第三步：何种状态则进行何种登录
	 * 第四步：更新登录状态次数
	 * 第五步：获取登录返回的信息
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户名不能为空","密码不能为空","用户名状态不能为空"},new Object[]{username,password,loginstate}))
			return ; 
		int type = 0;
		try{
			type = Integer.parseInt(loginstate);
		}catch(Exception e){
			logger.info("login type error" + e.getMessage());
			type = 0;
		}
		String phoneLogin = "SELECT ID,PASSWORD FROM wm_user_index uindex  WHERE uindex.TELENO = ?";
		String emailLogin = "SELECT ID,PASSWORD FROM wm_user_index uindex  WHERE uindex.EMAIL = ?";
		String usernameLogin = "SELECT ID,PASSWORD FROM wm_user_index WHERE USERNAME = ?";
		switch(type){
		case 0: login(usernameLogin);break;
		case 1: login(phoneLogin);break;
		case 2: login(emailLogin);break;
		case 3: cookieLogin();break;
		default : login(usernameLogin);break;
		}
	}
	
	
	/**
	 * 登录
	 * @param loginname
	 */
	private void login(String sql){
		Map<String,Object> map = middlewareService.findFirst(sql,username);
		if(map==null||map.isEmpty()){
			responseInfo("-3","输入的账号不存在");
			return ;
		}
		if(!checkPassword(String.valueOf(map.get("password")))){
			responseInfo("-3","输入的密码错误");
			return ;
		}
		String ipAddress = getIpAddr();
		String updateSql = "UPDATE wm_user_index SET LOGINCOUNT=LOGINCOUNT+1 ,LASTLOGINTIME = ?,LASTLOGINIP = ?,LASTUPDATETIME = ?,LASTUPDATEIP = ? where ID = ? ";
		String updateOnline = "UPDATE wm_user_info SET ISONLINE = 1 WHERE UID=?";
		middlewareService.update(updateOnline,map.get("id"));
		int count = middlewareService.update(updateSql,DateUtils.getCurrentTime(),ipAddress,DateUtils.getCurrentTime(),ipAddress,map.get("id"));
		if(count==0){
			logger.info(username+"账号登录成功，但是更新登录状态失败");
		}
		if(ResponseCodeUtils.ANDROID_PORT.equals(terminal) || ResponseCodeUtils.IPHONE_PORT.equals(terminal)){
			//安卓端，登陆后就返回信息
			this.uid = String.valueOf(map.get("id"));
			getUserInfo();
			return;
		}
		//登录成功，返回当前登录人的ID
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("uid", String.valueOf(map.get("id")));
		list_value.add(map_value);
		responseInfo("1","成功",list_value);
	}
	
	
	public void getUserInfo(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户名不能为空",callback);
			return;
		}
		String infoSql = "SELECT "+
				"	uinfo.UID,uinfo.ROLETAG,uindex.STEP,uindex.ID,uindex.TELENO,uindex.LOGINCOUNT,uindex.EMAIL,"+
				"	uindex.USERNAME,uindex.ISLOCK,uinfo.NICKNAME,uinfo.PHOTO,uinfo.REALNAME,uinfo.GENDER,"+
				"	DATE_FORMAT(uinfo.BIRTHDAY,'%Y-%m-%d %H:%i:%s'"+
				"	) AS 'BIRTHDAY',uinfo.PLACE,uinfo.BELONGS,uinfo.INVESTMENT,uinfo.RECIEVEMSG,"+
				"	uinfo.INTEREST,uinfo.WORKYEAR, ucom.ATTENTIONSUM,ucom.FANSSUM,ucom.BLOGSUM,"+
				"	ucom.FANSCHANGENUBER,urank.total_profit AS SUMRETURN,urank.month_profit AS MONTHLYRETURN,ucom.COMMENTCHANGENUMBER,"+
				"	ucom.PRAISECHANGENUMBER,ucom.NOTICECHANGENUMBER,urank.ODDS,uinfo.SUMMY,uinfo.JOB,"+
				"   uinfo.TARDEMETHOD,urole.ADDTIME,ustate.EMAILVALIDSTATE"+
				"	FROM wm_user_index uindex"+
				"	LEFT JOIN wm_user_info uinfo ON uindex.ID = uinfo.UID"+
				"	LEFT JOIN wm_user_communityInfo ucom ON uindex.ID = ucom.UID"+
				"	LEFT JOIN wm_user_roleAnalyst urole ON uindex.ID = urole.UID"+
				"	LEFT JOIN wm_user_stateInfo ustate ON uindex.ID = ustate.UID"+
				"	LEFT JOIN wm_user_rank urank ON uindex.ID = urank.UID AND urank.TYPE=0"+
				"	WHERE uindex.ID = ?";
	
	String countrySql = "SELECT COUNTRYNAME FROM wm_user_attentionCountry WHERE UID = ?";
	String currencySql = "SELECT `NAME` FROM wm_user_attentionCurrency WHERE UID = ?";
	List<Map<String,Object>> list_value_country = middlewareService.find(countrySql,uid);
	List<Map<String,Object>> list_value_currency = middlewareService.find(currencySql,uid);
	StringBuilder countrybuilder = new StringBuilder();
	StringBuilder currencybuilder = new StringBuilder();
	if(list_value_country!=null){
		for(Map<String,Object> map:list_value_country){
			countrybuilder.append(","+map.get("countryname"));
		}
	}
	
	if(list_value_currency!=null){
		for(Map<String,Object> map:list_value_currency){
			currencybuilder.append(","+map.get("name"));
		}
	}
	
	List<Map<String,Object>> list_value = middlewareService.find(infoSql,uid);
	//拆分地区
	String place = "";
	String newphone = "";
	for(Map<String,Object> map:list_value){
		place = String.valueOf(map.get("place"));
		newphone = String.valueOf(map.get("teleno"));
		newphone = StringUtils.isEmpty(newphone)?"":newphone.substring(0,newphone.length()-4)+"****";
		map.put("teleno", newphone);
	}
	String places[] = place.split(",");
	String araename = "";
	String areanames = "";
	String checkplace = "SELECT AREANAME FROM wm_setting_area where ID = ?";
	for(String p:places){
		araename = middlewareService.findBy(checkplace,"AREANAME", p);
		areanames +=araename+" ";
	}
	for(Map<String,Object> map:list_value){
		map.put("name", currencybuilder.length()==0?"":currencybuilder.substring(1));
		map.put("countryname", countrybuilder.length()==0?"":countrybuilder.substring(1));
		map.put("placename", areanames);
	}

	responseInfo("1","获取个人信息成功",list_value,callback);
	
}
	/**
	 * 验证账号密码是否正确
	 * @param pwd
	 * @return
	 */
	private boolean checkPassword(String pwd){
		if(DataUtils.checkString(pwd)||DataUtils.checkString(password)){
			return false;
		}
		return pwd.equals(password);
	}
	
	/**
	 * cookie登录
	 * @return
	 */
	public void cookieLogin(){
		String uid = getParameter("uid");
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String sql = "INSERT INTO wm_log_login (UID,TYPE,IP,TIME)VALUES(?,?,?,?)";
		int count = middlewareService.update(sql,uid,1,getIpAddr(),DateUtils.getCurrentTime());
		if(count == 0){
			logger.info(uid+" --->> "+"更新登录日志失败");
		}
		String updateLoginState = "UPDATE wm_user_index SET LASTUPDATETIME = ?,LASTUPDATEIP = ? WHERE ID=?";
		int stateCount = middlewareService.update(updateLoginState,DateUtils.getCurrentTime(),getIpAddr(),uid);
		String updateOnline = "UPDATE wm_user_info SET ISONLINE = 1 WHERE UID=?";
		middlewareService.update(updateOnline,uid);
		if(stateCount == 0){
			logger.info(uid+" --->> "+"更新登录在线状态失败");
		}
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("uid", uid);list_value.add(map);
		responseInfo("1","成功",list_value);
	}
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getLoginstate() {
		return loginstate;
	}
	public void setLoginstate(String loginstate) {
		this.loginstate = loginstate;
	}


	public String getCallback() {
		return callback;
	}


	public void setCallback(String callback) {
		this.callback = callback;
	}


	public String getUid() {
		return uid;
	}


	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
