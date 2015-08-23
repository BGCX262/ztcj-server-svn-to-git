package com.wm927.action.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.Decoder;
import com.wm927.commons.SensitiveWordFilter;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 认证
 * @author chen
 * 分析师--机构认证
 * 修改于2013-10-22 
 */
public class AnalystAuthenticate extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(AnalystAuthenticate.class);
	//<机构认证的属性>
	//社区官方名称
	private String orgname;
	//企业名称
	private String comname;
	//联系人
	private String relname;
	//联系人电话
	private String relphone;
	//营业执照注册号
	private String license;
	//营业执照副本路径
	private String path;
	//用户id
	private String uid;
	//官网
	private String web;
	//<分析师认证的属性>
	//真实姓名
	private String realname;
	//身份证号码
	private String cardno;
	//电话号码
	private String phone;
	//工作年限
	private String workyear;
	//交易方法,基本面(0) 技术面(1) 基本面+技术面(2)
	private String tread;
	//身份证图片
	private String cardpicture;
	//认证属性，判断是重新认证还是第一次认证(0代表第一次认证，1代表重新认证)
	private String state;
	
	/**
	 * 机构认证
	 * 第一步：判断字段是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：判断用户是否已经绑定
	 * 第四步：判断是否已经申请了认证记录
	 * 第五步：在机构认证表添加一条申请认证记录
	 */
	public void execute(){
		if(!checkNull(new Object[]{"社区官方名称不能为空","企业名称不能为空","联系人不能为空","联系人电话不能为空","营业执照注册号不能为空"
				,"路径不能为空","用户ID不能为空","认证状态不能为空"},
				new Object[]{orgname,comname,relname,relphone,license,path,uid,state}))
			return ;
		
		if(!checkUser(uid)){
			return;
		}
		relname = SensitiveWordFilter.doFilter(relname);
		orgname = SensitiveWordFilter.doFilter(orgname);
		comname = SensitiveWordFilter.doFilter(comname);
		//判断用户是否已经绑定
		/*String userPhone = "SELECT COUNT(*) FROM wm_user_index uindex"+
		"LEFT JOIN wm_user_stateInfo ustate ON ustate.UID = uindex.ID"+
				"	WHERE uindex.ID=? AND ustate.TELEVALIDSTATE=1";
		Long count = middlewareService.findCount(userPhone,uid);
		if(count==0){
			responseInfo("3","联系人手机号码未绑定！！");
			return ;
		}*/
		int status = DataUtils.praseNumber(state, 0);
		if(status==0){
			//第一次提交机构认证
			String checkorg = "SELECT COUNT(*) FROM wm_user_roleOrganization WHERE UID = ?";
			Long orgcount = middlewareService.findCount(checkorg,uid);
			if(orgcount!=0){
				responseInfo("-1","您已申请了机构认证，请耐心等待!");
				return;
			}
			String insertSql = "INSERT INTO wm_user_roleOrganization (UID,ORGANIZATIONNAME,ABBREVIATION,CONTACT,TELEPHONE,LICENSENUMBER,WEB,DUPLICATE,AUDITSTATE,LASTUPDATETIME,LASTUPDATEIP)VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			int insertRoleCount = middlewareService.update(insertSql,uid,comname,orgname,relname,relphone,license,web,path,2,DateUtils.getCurrentTime(),getIpAddr());
			if(insertRoleCount==0){
				logger.info("INSERT wm_user_roleOrganization false ON this class --> "+this.getClass().getSimpleName());
				responseInfo("-3","插入机构认证失败");
				return;
			}
			responseInfo("1","您已经提交了机构认证！请等待审核");
		}else{
			//重新提交机构认证
			String checkorg = "SELECT COUNT(*) FROM wm_user_roleOrganization WHERE UID = ? AND AUDITSTATE = 2";
			Long orgcount = middlewareService.findCount(checkorg,uid);
			if(orgcount!=0){
				responseInfo("-1","您已重新提交了机构认证，请耐心等待!");
				return;
			}
			String updateSql = "UPDATE wm_user_roleOrganization SET ORGANIZATIONNAME=?,ABBREVIATION=?,CONTACT=?,TELEPHONE=?,LICENSENUMBER=?,WEB=?,DUPLICATE=?,AUDITSTATE=?,LASTUPDATETIME=?,LASTUPDATEIP=? WHERE UID=?";
			int insertRoleCount = middlewareService.update(updateSql,comname,orgname,relname,relphone,license,web,path,2,DateUtils.getCurrentTime(),getIpAddr(),uid);
			if(insertRoleCount==0){
				logger.info("UPDATE wm_user_roleOrganization false ON this class --> "+this.getClass().getSimpleName());
				responseInfo("-3","插入机构认证失败");
				return;
			}
			responseInfo("1","您已经重新提交了机构认证！请等待审核");
		}
	}
	
	/**
	 * 分析师认证
	 * 第一步：判断字段是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：判断用户是否已经绑定
	 * 第四步：判断是否已经申请了认证记录
	 * 第五步：在分析师认证表添加一条申请认证记录
	 */
	public void analystAuthenticate(){
		if(!checkNull(new Object[]{"用户ID不能为空","认证状态不能为空","真实姓名不能为空","身份证号码不能为空","电话号码不能为空",
				"身份证图片不能为空","从业年限不能为空","交易方法 不能为空"},
				new Object[]{uid,state,realname,cardno,phone,cardpicture,workyear,tread}))
			return ; 
		
		if(!checkUser(uid)){
			return;
		}
		realname = SensitiveWordFilter.doFilter(realname);
		//判断用户是否已经绑定
		/*String userPhone = "select COUNT(*) from wm_user_index  uindex"+
						"	LEFT JOIN wm_user_stateInfo ustate ON ustate.UID=uindex.ID"+
						"	where uindex.ID=? AND ustate.TELEVALIDSTATE=1";
		Long count =(Long) middlewareService.findCount(userPhone,uid);
		if(count==0){
			responseInfo("3","用户手机号码未绑定！！");
			return ;
		}*/
		int status = DataUtils.praseNumber(state, 0);
		if(status==0){
			//防止用户重复提交认证申请
			String checkana = "SELECT COUNT(*) FROM wm_user_roleAnalyst WHERE UID = ?";
			Long orgcount = middlewareService.findCount(checkana,uid);
			if(orgcount!=0){
				responseInfo("-1","您已申请了分析师认证，请耐心等待!");
				return;
			}
			//第一次提交分析师认证
			//插入信息到分析师认证表
			String insertSql = "INSERT INTO wm_user_roleAnalyst(UID,WORKYEAR,CARDNUMBER,CARDPICTURE,REALNAME,PHONE,AUDITSTATE,LASTUPDATETIME,LASTUPDATEIP)VALUES(?,?,?,?,?,?,?,?,?,?)";
			String updateTrade = "UPDATE wm_user_info SET TARDEMETHOD = ? WHERE UID = ?";
			int insertRoleCount = middlewareService.update(insertSql,uid,workyear,cardno,cardpicture,realname,phone,2,DateUtils.getCurrentDate(),getIpAddr());
			middlewareService.update(updateTrade,tread,uid);
			if(insertRoleCount==0){
				logger.info("insert wm_user_roleAnalyst false on this class --> "+this.getClass().getSimpleName());
				responseInfo("-3","插入分析师认证失败");
				return;
			}
			responseInfo("1","您已提交了分析师认证！请等待审核");
			
		}else{
			//重新提交认证
			String checkana = "SELECT COUNT(*) FROM wm_user_roleAnalyst WHERE UID = ? AND AUDITSTATE = 2";
			Long orgcount = middlewareService.findCount(checkana,uid);
			if(orgcount!=0){
				responseInfo("-1","您已重新申请了分析师认证，请耐心等待!");
				return;
			}
			String updateSql = "UPDATE wm_user_roleAnalyst SET WORKYEAR = ?,CARDNUMBER=?,CARDPICTURE=?,REALNAME=?,PHONE=?,AUDITSTATE=?,LASTUPDATETIME=?,LASTUPDATEIP=? WHERE UID = ?";
			String updateTrade = "UPDATE wm_user_info SET TARDEMETHOD = ? WHERE UID = ?";
			middlewareService.update(updateTrade,tread,uid);
			middlewareService.update(updateSql,workyear,cardno,cardpicture,realname,phone,2,DateUtils.getCurrentDate(),getIpAddr(),uid);
			responseInfo("-1","您已重新提交分析师认证！请等待审核");
		}
	}
	
	/**
	 * 获取分析师或者机构的申请认证信息
	 */
	public void analystMessage(){
		if(!checkNull(new Object[]{"用户ID不能为空","认证状态不能为空"},new Object[]{uid,state}))
			return ; 
		if(!checkUser(uid))
			return;
		String anaSql ="SELECT urole.CARDNUMBER,urole.CARDPICTURE,urole.REALNAME,urole.PHONE,uinfo.TARDEMETHOD ,uinfo.WORKYEAR FROM wm_user_roleAnalyst urole LEFT JOIN wm_user_info uinfo ON urole.UID=uinfo.UID  WHERE urole.UID=?";
		String comSql = " SELECT ORGANIZATIONNAME,ABBREVIATION,CONTACT,TELEPHONE,LICENSENUMBER,WEB,DUPLICATE FROM wm_user_roleOrganization WHERE UID=?";
		int status = DataUtils.praseNumber(state, 0);
		List<Map<String,Object>> list_value = null;
		if(status == 0){
			list_value = middlewareService.find(anaSql,uid);
		}else{
			list_value = middlewareService.find(comSql,uid);
		}
		responseInfo("1","返回成功",list_value);
	}
	/**
	 * 是否满足分析师认证条件判断
	 */
	public void checkAnalyst(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		if(!checkUser(uid)){
			return;
		}
		String checkphone = "SELECT TELENO FROM wm_user_index  WHERE ID = ? " ;
		String phone = middlewareService.findBy(checkphone,"TELENO",uid);
		String checkBlog = "SELECT COUNT(*) FROM wm_blog_info WHERE UID = ? AND ISPUBLIC = 1 AND ISDELETE = 0";
		Long blogCount = middlewareService.findCount(checkBlog,uid);
		String checkPhoto = "SELECT PHOTO FROM wm_user_info where UID = ?";
		String photo  = middlewareService.findBy(checkPhoto, "PHOTO",uid);
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("phone", DataUtils.checkString(phone) ? "0":"1");
		map.put("blog", String.valueOf(blogCount));
		map.put("photo", DataUtils.checkString(photo) ? "0":"1");
		list_value.add(map);
		responseInfo("1","返回成功",list_value);
	}
	
	/**
	 * 是否满足机构认证条件判断
	 */
	public void checkOrgnation(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		if(!checkUser(uid)){
			return;
		}
		String checkPhoto = "SELECT PHOTO FROM wm_user_info WHERE UID = ?";
		String photo  = middlewareService.findBy(checkPhoto, "PHOTO",uid);
		String checkBlog = "SELECT COUNT(*) FROM wm_blog_info WHERE UID = ? AND ISPUBLIC = 1";
		Long blogCount = middlewareService.findCount(checkBlog,uid);
		String checkName = "SELECT REALNAME FROM wm_user_info WHERE UID = ?";
		String objName  = middlewareService.findBy(checkName, "REALNAME",uid);
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("photo", DataUtils.checkString(photo) ? "0":"1");
		map.put("name", DataUtils.checkString(objName) ? "0":"1");
		map.put("blog", String.valueOf(blogCount));
		list_value.add(map);responseInfo("1","返回成功",list_value);
	}
	
	public String getOrgname() {
		return orgname;
	}
	
	public void setOrgname(String orgname) {
		if(!DataUtils.checkString(orgname))
			orgname = Decoder.decode(orgname);
		this.orgname = orgname;
	}
	public String getComname() {
		return comname;
	}
	public void setComname(String comname) {
		if(!DataUtils.checkString(comname))
			comname  = Decoder.decode(comname);
		this.comname = comname;
	}
	public String getRelname() {
		return relname;
	}
	public void setRelname(String relname) {
		if(!DataUtils.checkString(relname))
			relname = Decoder.decode(relname);
		this.relname = relname;
	}
	public String getRelphone() {
		return relphone;
	}
	public void setRelphone(String relphone) {
		this.relphone = relphone;
	}
	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		if(!DataUtils.checkString(realname))
			realname = Decoder.decode(realname);
		this.realname = realname;
	}
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getWorkyear() {
		return workyear;
	}
	public void setWorkyear(String workyear) {
		this.workyear = workyear;
	}
	
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public String getTread() {
		return tread;
	}
	public void setTread(String tread) {
		this.tread = tread;
	}
	public String getCardpicture() {
		return cardpicture;
	}
	public void setCardpicture(String cardpicture) {
		this.cardpicture = cardpicture;
	}
	public String getWeb() {
		return web;
	}
	public void setWeb(String web) {
		this.web = web;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
}
