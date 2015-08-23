package com.wm927.action.userinfo;

import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 头像上传
 * @author chen
 * 修改于2013-10-22
 */
public class HeadUpload extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(HeadUpload.class);
	//头像路径
	private String photo;
	//用户id
	private String uid;
	/**
	 * 第一步：判断头像路径是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：更新用户的头像路径
	 */
	public void execute(){
		if(DataUtils.checkString(photo)){
			responseInfo("-1","上传头像路径为空");
			return;
		}
		if(!checkUser(uid)){
			return ;
		}
		String uploadSql = "UPDATE wm_user_info SET PHOTO = ? WHERE UID = ?";
		int count = middlewareService.update(uploadSql,photo,uid);
		if(count==0){
			logger.info("UPDATE wm_user_info false ON this class --> "+this.getClass().getSimpleName());
			responseInfo("-3","头像上传失败");
			return;
		}
		responseInfo("1","修改成功");
	}
	
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}

}
