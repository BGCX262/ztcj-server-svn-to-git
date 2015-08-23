package com.wm927.action.userinfo;

import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.Decoder;
import com.wm927.commons.SensitiveWordFilter;
import com.wm927.service.impl.MiddlewareActionService;

public class UpdateUser extends MiddlewareActionService{
		private Logger logger = Logger.getLogger(UpdateUser.class);
		//被查找人id
		private String uid;
		//昵称
		private String nickname;
		//真实姓名
		private String realname;
		//性别
		private String sex;
		//生日
		private String birthday;
		//所属地区
		private String place;
		//所属人群
		private String belongs;
		//投资领域
		private String investment;
		//工作年限
		private String workyear;
		//交易方法
		private String tardemethod;
		private String job;//职业
		private String addtime;//加入时间
		//兴趣爱好
		private String interest;
		//0代表普通用户或分析师1代表机构
		private String type;
		
		private String orgname;//机构名称
		private String web;//机构官网
		private String summy;//机构简介
		private String contact;//机构联系人
		private String phone;//机构电话
		private String createtime;//机构创建时间
		private String applogo;
		
	/**
	 * 个人基本信息修改
	 * 第一步：判断字段是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：判断是普通用户还是分析师(分析师需要判断交易方法不能为空)
	 * 第四步：更新用户的信息
	 */
	public void execute(){
		if("1".equals(type)){
			updateOrgInfo();//修改机构信息
			return;
		}
		if(!checkNull(new Object[]{"用户ID不能为空","投资领域不能为空","工作年限不能为空","所属人群不能为空","归属地不能为空","生日不能为空","性别不能为空"},
				new Object[]{uid,investment,workyear,belongs,place,birthday,sex}))
			return ; 
		if(!checkUser(uid)){
			return;
		}
		if(!DataUtils.checkString(summy)){
			summy = SensitiveWordFilter.doFilter(summy);
		}
		if(!DataUtils.checkString(nickname)){
			nickname = SensitiveWordFilter.doFilter(nickname);
		}
		if(!DataUtils.checkString(realname)){
			realname = SensitiveWordFilter.doFilter(realname);
		}
		if(DataUtils.checkString(job)){
			job = null;
		}else{
			job = SensitiveWordFilter.doFilter(job);
		}
		if(DataUtils.checkString(tardemethod)){
			tardemethod = null;
		}
		String update = "UPDATE wm_user_info SET TARDEMETHOD = ?,NICKNAME = ?,JOB = ?,REALNAME = ?,BELONGS = ?,INVESTMENT = ?,GENDER = ?,BIRTHDAY = ?,PLACE = ?,WORKYEAR = ?,SUMMY = ?,ADDTIME = ? WHERE UID = ?";
		int updateCount = middlewareService.update(update,tardemethod,nickname,job,realname,belongs,investment,sex,birthday,place,workyear,summy,addtime,uid);
		if(updateCount==0){
			logger.info("update wm_user_info false on this class --> "+this.getClass().getSimpleName());
			responseInfo("-3","修改基本信息失败");
			return;
		}
		//更新最后修改时间
		responseInfo("1","个人信息修改成功");
	}
	
	/**
	 * 修改机构信息
	 */
	private void updateOrgInfo(){
		if(!checkNull(new Object[]{"用户ID不能为空","机构联系人不能为空","机构官网不能为空","投资领域不能为空","机构名称不能为空",
				"所属人群不能为空","归属地不能为空","机构创建时间不能为空"},
				new Object[]{uid,contact,web,investment,orgname,belongs,place,createtime}))
			return ; 
		if(DataUtils.checkString(summy)){
			summy = null;
		}else{
			summy = SensitiveWordFilter.doFilter(summy);
		}
		orgname = SensitiveWordFilter.doFilter(orgname);
		contact = SensitiveWordFilter.doFilter(contact);
		if(DataUtils.checkString(phone)){
			phone = null;
		}
		String updateInfo = "UPDATE wm_user_info SET NICKNAME = ? ,PLACE = ? ,BELONGS = ?,INVESTMENT = ? WHERE UID = ?";
		String sql = "UPDATE wm_user_roleOrganization SET ORGANIZATIONNAME = ?,WEB = ?,ABBREVIATION = ?,CONTACT = ?,TELEPHONE = ?,CREATETIME = ?,APPLOGO = ? WHERE UID = ?";
		int count1 = middlewareService.update(updateInfo,nickname,place,belongs,investment,uid);
		if(count1 == 0){
			responseInfo("-3","修改信息失败");
			return;
		}
		int count2 = middlewareService.update(sql,orgname,web,summy,contact,phone,createtime,applogo,uid);
		if( count2 == 0){
			responseInfo("-3","修改机构信息失败");
			return;
		}
		responseInfo("1","修改信息成功");
	}
	/**
	 * 修改兴趣爱好
	 * 第一步：判断兴趣爱好是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：更新用户的兴趣爱好
	 */
	public void updateInterest(){
		if(DataUtils.checkString(interest)){
			interest = null;
		}
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户不能为空");
			return;
		}
		if(!checkUser(uid)){
			return;
		}
		String sql = "UPDATE wm_user_info SET INTEREST = ? WHERE UID = ?";
		int count   = middlewareService.update(sql,interest,uid);
		if(count==0){
			responseInfo("-3","修改兴趣爱好失败");
			return;
		}
		responseInfo("1","修改兴趣爱好成功");
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		if(!DataUtils.checkString(nickname))
			nickname = Decoder.decode(nickname);
		this.nickname = nickname;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		if(!DataUtils.checkString(realname))
			realname = Decoder.decode(realname);
		this.realname = realname;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		if(!DataUtils.checkString(birthday))
			birthday = Decoder.decode(birthday);
		this.birthday = birthday;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		if(!DataUtils.checkString(place))
			place = Decoder.decode(place);
		this.place = place;
	}

	public String getBelongs() {
		return belongs;
	}

	public void setBelongs(String belongs) {
		this.belongs = belongs;
	}

	public String getInvestment() {
		return investment;
	}

	public void setInvestment(String investment) {
		this.investment = investment;
	}

	public String getWorkyear() {
		return workyear;
	}

	public void setWorkyear(String workyear) {
		this.workyear = workyear;
	}

	public String getTardemethod() {
		return tardemethod;
	}

	public void setTardemethod(String tardemethod) {
		this.tardemethod = tardemethod;
	}

	public String getInterest() {
		return interest;
	}

	public void setInterest(String interest) {
		this.interest = interest;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOrgname() {
		return orgname;
	}

	public void setOrgname(String orgname) {
		if(!DataUtils.checkString(orgname))
			orgname = Decoder.decode(orgname);
		this.orgname = orgname;
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		if(!DataUtils.checkString(web))
			web = Decoder.decode(web);
		this.web = web;
	}

	public String getSummy() {
		return summy;
	}

	public void setSummy(String summy) {
		if(!DataUtils.checkString(summy))
			summy = Decoder.decode(summy);
		this.summy = summy;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		if(!DataUtils.checkString(contact))
			contact = Decoder.decode(contact);
		this.contact = contact;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		if(!DataUtils.checkString(createtime))
			createtime = Decoder.decode(createtime);
		this.createtime = createtime;
	}

	public String getApplogo() {
		return applogo;
	}

	public void setApplogo(String applogo) {
		if(!DataUtils.checkString(applogo))
			applogo = Decoder.decode(applogo);
		this.applogo = applogo;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		if(!DataUtils.checkString(job))
			job = Decoder.decode(job);
		this.job = job;
	}

	public String getAddtime() {
		return addtime;
	}

	public void setAddtime(String addtime) {
		if(!DataUtils.checkString(addtime))
			addtime = Decoder.decode(addtime);
		this.addtime = addtime;
	}
	
}
