package com.wm927.action.user;

import java.util.List;
import java.util.Map;
import com.wm927.commons.CallCodeUtils;
import com.wm927.commons.Contants;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.Decoder;
import com.wm927.commons.MoneyCodeUtils;
import com.wm927.commons.PageUtils;
import com.wm927.commons.SensitiveWordFilter;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * APP分析师信息
 * @author chen
 *
 */
public class AppAnalyst extends MiddlewareActionService{
	private String appname;//APP名字
	private String hotphone;//APP热线电话
	private String twocode;//APP二维码
	private String appaddr;//APP下载地址
	private String uid;
	private String callback ;//跨域调用号
	private String headurl;//连接头像
	private String softurl;//软件头像
	private String share;//一句话分享
	private String state;//APP绑定状态(0代表绑定，1代表解绑定)
	private String ouid;//机构ID
	private String page;
	private String number;
	private String lastid;//动态id
	private String blogid;
	private String size;
	private String date;
	private String type = "1";
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * 手机APP分析师申请认证
	 */
	public void execute(){
		//map.put("二维码", twocode);map.put("app下载地址不能为空", appaddr);
		if(!checkNull(new Object[]{"用户ID不能为空","用户上传的头像不能为空","用户软件图标不能为空", "一句分享话不能为空",
				"app名字不能为空", "热线电话不能为空"},
				new Object[]{uid,headurl,softurl,share,appname,hotphone}))
			return ; 
		share = SensitiveWordFilter.doFilter(share);
		appname = SensitiveWordFilter.doFilter(appname);
		String sql = "INSERT INTO wm_userApp_info (UID,APPNAME,APPLINKPHOTO,SHARESENTENCE,HOTLINE,APPICON,TWODIMENSIONALCODE,APPDOWN,ISAPP,LASTUPDATETIME)VALUES(?,?,?,?,?,?,?,?,?,?)";
		
		String checkApp = "SELECT COUNT(*) FROM wm_userApp_info WHERE UID = ?";
		int updateCount ;
		if(middlewareService.findCount(checkApp,uid) == 0){
			updateCount = middlewareService.update(sql,uid,appname,headurl,share,hotphone,softurl,twocode,appaddr,1,DateUtils.getCurrentTime());
		}else{
			sql = "UPDATE wm_userApp_info SET APPNAME=?,APPLINKPHOTO=?,APPICON=?,SHARESENTENCE=?,HOTLINE=?,LASTUPDATETIME=? WHERE UID = ?";
			updateCount = middlewareService.update(sql,appname,headurl,softurl,share,hotphone,DateUtils.getCurrentTime(),uid);
		}
		if(updateCount == 0){
			responseInfo("-3","APP个人信息编辑失败");
			return;
		}
		responseInfo("1","APP个人信息编辑成功");
	}
	/**
	 * 删除APP信息
	 */
	public void deleteAppRegist(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		String sql = "DELETE FROM wm_userApp_info  WHERE UID = ?";
		middlewareService.update(sql,uid);
		responseInfo("1","数据已清空");
	}
	/**
	 * 手机APP分析师相关信息
	 */
	public void relationAnalyst(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid},callback))
			return ; 

		String sql = "SELECT ISAPP,HOTLINE,DATE_FORMAT(LASTUPDATETIME ,'%Y-%m-%d %H:%i:%s') AS 'LASTUPDATETIME'  ,APPLINKPHOTO,APPICON,SHARESENTENCE,TWODIMENSIONALCODE,APPDOWN,APPNAME FROM wm_userApp_info WHERE UID = ? ";
		List<Map<String,Object>> list_value = middlewareService.find(sql,uid);
		for(Map<String,Object> map: list_value){
			map.putAll(findUserInfo(map.get(uid)));
		}
		responseInfo("1","返回数据成功",list_value,callback);
	}
	
	
	
	/**
	 * 分析师绑定机构
	 */
	public void appAnalystRegist(){
		if(!checkNull(new Object[]{"用户ID不能为空","机构ID不能为空","申请状态不能为空"},new Object[]{uid,ouid,state},callback))
			return ; 
		if(!checkOrgRole(ouid))
			return;
		if(!checkAppAnalystRole(uid))
			return;
		//验证分析师绑定机构状态
		String checkAnalystState = "SELECT AUTHENTICATESTATE FROM wm_user_roleAnalyst WHERE UID = ?";
		String oidState = middlewareService.findBy(checkAnalystState, "AUTHENTICATESTATE",uid);
		String updateAnalystState = "UPDATE wm_user_roleAnalyst SET AUTHENTICATETIME = ? ,AUTHENTICATESTATE = ? WHERE UID = ?";
		//发送通知到机构
		String msg = "已提交申请绑定";
		String userInfo = "SELECT NICKNAME FROM wm_user_info WHERE UID = ?";
		String name = middlewareService.findBy(userInfo, "NICKNAME",uid);
		String cur_date = DateUtils.getCurrentTime();
		String advice = "INSERT INTO wm_user_advice (UID,CONTENT,ADDTIME,SENDID,SENDIP,TYPE)VALUES(?,?,?,?,?,?)";
		int adviceCount = 0;
		if("1".equals(state)){
			//解除关系
			adviceCount = middlewareService.update(advice,ouid,name+"已经与您解除机构关系，请注意查看",cur_date,uid,getIpAddr(),3);
			updateAnalystState = "UPDATE wm_user_roleAnalyst SET OID = ? WHERE UID = ?";
			middlewareService.update(updateAnalystState,Contants.DEFAULT_OID,uid);
			//获取机构的全名
			String orgsql = "SELECT ORGANIZATIONNAME FROM wm_user_roleOrganization WHERE UID = ?";
			String orgname = middlewareService.findBy(orgsql, "ORGANIZATIONNAME",ouid);
			middlewareService.update(advice,uid,"您已经与"+orgname+"解除了机构关系",cur_date,ouid,getIpAddr(),3);
			msg = "已提交解除绑定";
		}else{
			//申请关系
			if("2".equals(oidState)){
				responseInfo("-3","您已经提交了申请绑定机构,不能再申请绑定机构!");
				return;
			}
			middlewareService.update(updateAnalystState,cur_date,2,uid);
			adviceCount = middlewareService.update(advice,ouid,name+"申请成为您机构的分析师，请注意查看",cur_date,uid,getIpAddr(),2);
		}
		
		if(adviceCount == 0){
			responseInfo("-3","添加通知失败");
			return;
		}
		//用户的通知变动数+1
		String adviceChangeSql = "UPDATE wm_user_communityInfo SET NOTICECHANGENUMBER = NOTICECHANGENUMBER+1 WHERE UID = ?";
		middlewareService.update(adviceChangeSql,ouid);
		responseInfo("1",msg);
	}

	/**
	 * 手机APP单个分析师的所有喊单和实战圈子下数据(类似动态加载)
	 * 若lastid为NULL，代表第一次加载数据,返回最新5条数据，否则lastid代表加载更多的动态
	 */
	public void phoneCallList(){
		if(!checkNull(new Object[]{"用户ID不能为空","被索引的用户id为空"},new Object[]{uid,ouid},callback))
			return ; 
		if(!checkUser(uid))
			return;
		if(!checkAppAnalystRole(ouid, callback))
			return;
		String dynamicInfo = "SELECT dy.ID AS DID,dy.HDID,dy.CID,dy.TYPE AS HDTYPE,  DATE_FORMAT(dy.ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME'  FROM wm_user_dynamic dy WHERE dy.UID = "+ouid+" AND (dy.CID IS NOT NULL OR dy.HDID IS NOT NULL ) ";
		//获取实战圈子数据如果关注则获取全部数据,否则获取对所有人查看的实战圈子数据
		String circleInfo = "SELECT UID,CONTENT,COMMENTNUMBER,BROWSENUMBER,PURVIEWID FROM wm_blog_circle WHERE  ID = ?";
		String attentionState = "0";//是否查看更多的内容，0查看，1不能查看
		if(!uid.equals(ouid)){
			String attentionCount =  attentionState(uid,ouid);
			if("0".equals(attentionCount)  || "1".equals(attentionCount)){
				attentionState = "1";
			}
		}
		if(!DataUtils.checkString(date)){
			//针对PC端需求，需要按日期查询数据
			String beginDate = date +" 00:00:00";
			String endDate = date +" 23:59:59";
			dynamicInfo += " AND ADDTIME BETWEEN '"+beginDate +"' AND '"+ endDate +"'";
		}
		
		if(!DataUtils.checkString(lastid)){
			//动态加载更多
			if("0".equals(type)){//新数据
				dynamicInfo += " AND dy.ID > "+lastid;
			}else{//老数据
				dynamicInfo += " AND dy.ID < "+lastid;
			}
		}
		dynamicInfo +=" ORDER BY dy.ID DESC LIMIT "+PageUtils.getPageCount(size);
		//获取喊单信息
		String callSql = "SELECT CODE,TYPE,SUNTYPE,TRADE,PRICE,PROFIT,EXITPRICE FROM wm_bill_info WHERE ID =  ?" ;
		//存放总收益与胜率的map
		Map<String,Object> community_map =	findUserCallInfo(ouid);
		
		List<Map<String,Object>> list_value = middlewareService.find(dynamicInfo);
		//存放实战圈子信息
		Map<String,Object> blog_map = null;
		//存放喊单信息
		Map<String,Object> call_map = null;
		for(Map<String,Object> map:list_value){
			if(!DataUtils.checkString(map.get("cid"))){
				map.put("state", "0");
				blog_map = middlewareService.findFirst(circleInfo,map.get("cid"));
				blog_map.put("attentionstate", attentionState);
				map.putAll(blog_map);
			}else{
				map.put("state", "1");
				call_map = middlewareService.findFirst(callSql,map.get("hdid"));
				Object code = call_map.get("code");
				call_map.put("code", MoneyCodeUtils.CODE_MAP.get(code));
				
				//0代表内盘1代表外汇2代表贵金属
				if("0".equals(call_map.get("type"))){
					call_map.put("ttype", "0");
				}else{
					if("2".equals(call_map.get("suntype"))){
						call_map.put("ttype", "2");
					}else{
						call_map.put("ttype", "1");
					}
				}
				
				if("1".equals(map.get("hdtype"))){
					//平仓
					call_map.put("price", returnCallMessage(call_map.get("exitprice"),code));
				}else{
					//喊单
					call_map.put("price", returnCallMessage(call_map.get("price"),code));
				}
				
				map.putAll(community_map);map.putAll(call_map);
			}
		}
		responseInfo("1","返回数据成功",list_value,callback);
	}
	
	
	/**
	 * 拼接喊单数据
	 * 
	 * @return
	 */
private  String returnCallMessage(Object price,Object code){
	String message = "";
	if(DataUtils.checkString(price)){
		return message;
	}
	String price1 = String.valueOf(price);
	String code1 = String.valueOf(code);
	int flag = DataUtils.praseNumber(CallCodeUtils.CODE_VALUE.get(code1), 0);
	if(flag == 0){
		flag = price1.indexOf(".");
	}else{
		flag = price1.indexOf(".")+flag+1;
	}
	message = price1.substring(0,flag);
	return message;
}

	/**
	 * 返回机构图片logo，app名称，app分析师头像，app昵称
	 */
		public void findAppInfo(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户id不能为空",callback);
			return;
		}
		if(!checkAppAnalystRole(uid, callback))
			return;
		//APP信息
		String sql = "SELECT APPNAME,APPLINKPHOTO,APPICON,HOTLINE FROM wm_userApp_info WHERE UID = ?";
		//所绑定的机构ID
		String orgInfo = "SELECT OID FROM wm_user_roleAnalyst WHERE UID = ?";
		//机构LOGO
		String appLogo = "SELECT APPLOGO FROM wm_user_roleOrganization WHERE UID = ?";
		Map<String,Object> map_value_info = findUserInfo(uid);
		String orgid = middlewareService.findBy(orgInfo,"OID",uid);
		List<Map<String,Object>> list_value = middlewareService.find(sql,uid);
		Map<String,Object> map_value_org = middlewareService.findFirst(appLogo,orgid);
		for(Map<String,Object> map :list_value){
			map.putAll(map_value_info);
			map.putAll(map_value_org);
		}
		responseInfo("1","成功",list_value,callback);
	}

	
	
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		if(!DataUtils.checkString(appname))
			appname = Decoder.decode(appname);
		this.appname = appname;
	}
	public String getHotphone() {
		return hotphone;
	}
	public void setHotphone(String hotphone) {
		this.hotphone = hotphone;
	}
	public String getTwocode() {
		return twocode;
	}
	public void setTwocode(String twocode) {
		this.twocode = twocode;
	}
	public String getAppaddr() {
		return appaddr;
	}
	public void setAppaddr(String appaddr) {
		this.appaddr = appaddr;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getHeadurl() {
		return headurl;
	}
	public void setHeadurl(String headurl) {
		this.headurl = headurl;
	}
	public String getSofturl() {
		return softurl;
	}
	public void setSofturl(String softurl) {
		this.softurl = softurl;
	}
	public String getShare() {
		return share;
	}
	public void setShare(String share) {
		if(!DataUtils.checkString(share))
			share = Decoder.decode(share);
		this.share = share;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getOuid() {
		return ouid;
	}
	public void setOuid(String ouid) {
		this.ouid = ouid;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getLastid() {
		return lastid;
	}
	public void setLastid(String lastid) {
		this.lastid = lastid;
	}
	public String getBlogid() {
		return blogid;
	}
	public void setBlogid(String blogid) {
		this.blogid = blogid;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	
	
}
