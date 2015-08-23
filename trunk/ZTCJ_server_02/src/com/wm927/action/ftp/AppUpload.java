package com.wm927.action.ftp;

import java.io.File;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.RoleUtils;
import com.wm927.commons.ServerDomainUtils;
import com.wm927.dbutils.ContextHolder;
import com.wm927.service.impl.MiddlewareActionService;

public class AppUpload extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(AppUpload.class);
	private String uid;
	private FileUpload fileUpload;
	private String down_ip;
	private String down_name;
	private String down_pwd;
	private String up_ip;
	private String up_name;
	private String up_pwd;
	
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		//修改分析师为支付状态,分析师APP下载地址
		String updateState = "UPDATE wm_userApp_info SET ISAPP = 0 WHERE UID = ?";
		middlewareService.update(updateState,uid);
		//修改分析师角色
		String updateRole = "UPDATE wm_user_info SET ROLETAG = ? WHERE UID = ?";
		middlewareService.update(updateRole,RoleUtils.APPANALYST_ROLE,uid);
		responseInfo("1","成功");
		
	}
	
	public void createAppDown(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		String sql = "SELECT UID,APPNAME,HOTLINE,APPLINKPHOTO,APPICON FROM wm_userApp_info WHERE UID = ?";
		String userInfoSql = "SELECT REALNAME FROM wm_user_info WHERE UID = ?";
		
		ContextHolder.setCustomerType(ContextHolder.DATA_SOURCE2);
		Map<String,Object> map_value = middlewareService.findFirst(sql,uid);
		String realname = middlewareService.findBy(userInfoSql,"REALNAME", uid);
		ContextHolder.clearCustomerType();
		//执行图片下载到服务器
		String outputpath = ServerDomainUtils.APP_DOWN_URI+uid +"/";//上传到本地地址
		String path = map_value.get("applinkphoto")+"";
		String childpath[] = path.split("\\|");
		String icon = map_value.get("appicon")+"";
		if(DataUtils.checkString(icon) || DataUtils.checkString(path)){
			responseInfo("-1","APP头像图片或者图标不能为空");
			return;
		}
		boolean result = false;
		logger.info("picture down begin");
		String filename = "";
		if(!down_connect()){
			responseInfo("-1","连接FTP服务器失败");
			return ;
		}
		for(String p : childpath){
			filename = uid + "_"+p.substring(p.indexOf("-")+1,p.lastIndexOf("-"))+".png";
			result = fileUpload.downLoad("/"+ServerDomainUtils.APP_URI+p,outputpath,filename);
			logger.info("/"+ServerDomainUtils.APP_URI+p);
			if(!result)
				break;
		}
		if(result){
			filename = uid + "_"+icon.substring(icon.indexOf("-")+1,icon.lastIndexOf("-"))+".png";
			result = fileUpload.downLoad("/"+ServerDomainUtils.ICON_URI+icon,outputpath,filename);
		}
		//释放下载资源
		fileUpload.relaseServerConnection();
		if(!result ){
			logger.info("picture down false");
			responseInfo("-1","APP生成失败,所上传的图片不存在");
			return;
		}
			
		logger.info("picture down success");
		//打包生成APP
		String upfilepath = ServerDomainUtils.APP_UP_URI+uid+"/";
		/*String upfile = ServerDomainUtils.APP_UP_URI+uid+"/wisegeek_"+uid+".apk";
		AnalystsBean analyst = new AnalystsBean();
		analyst.setAnalystsID(uid);
		analyst.setAnalystsName(realname);
		analyst.setTitlename(map_value.get("appname")+"");
		analyst.setTelno(map_value.get("hotline")+"");
		analyst.setResPath(outputpath);
		analyst.setOutDir(upfilepath);
		GenerateApk apk =  GenerateApk.getInstance();
		try{
			apk.packages(analyst);
			logger.info(analyst.toString());
		}catch(Exception e){
			logger.info(e.getMessage());
		}
		
		logger.info("apk create success");
		//将生成的APP上传到服务器
		logger.info(upfile);
		if(!up_connect()){
			responseInfo("-1","连接FTP服务器失败");
			return ;
		}
		result = fileUpload.upload(new File(upfile),upfilepath);
		//释放上传资源
		fileUpload.relaseServerConnection();
		if(!result){
			logger.info("apk up false");
			responseInfo("-1","APP文件上传失败");
		}
		String appDownFile = ServerDomainUtils.SERVER_DOMAIN_URI_APP +upfile;
		//修改分析师为支付状态,分析师APP下载地址
		String updateState = "UPDATE wm_userApp_info SET APPDOWN = ? WHERE UID = ?";
		ContextHolder.setCustomerType(ContextHolder.DATA_SOURCE2);
		middlewareService.update(updateState,appDownFile,uid);
		ContextHolder.clearCustomerType();
		responseInfo("1","成功");
		*/
	}
	
	/**
	 * 上传连接
	 * @return
	 * @throws Exception 
	 */
	private boolean up_connect() {
		boolean result = false;
		fileUpload.relaseServerConnection();     
		result = fileUpload.connect(up_ip,21,up_name,up_pwd);
		return result;
	}
	/**
	 * 下载连接
	 * @return
	 * @throws Exception 
	 */
	private boolean down_connect() {
		boolean result = false;
		fileUpload.relaseServerConnection();
		result = fileUpload.connect(down_ip,21,down_name,down_pwd);
		return result;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}

	public void setFileUpload(FileUpload fileUpload) {
		this.fileUpload = fileUpload;
	}
	public String getDown_name() {
		return down_name;
	}
	public void setDown_name(String down_name) {
		this.down_name = down_name;
	}
	public String getDown_pwd() {
		return down_pwd;
	}
	public void setDown_pwd(String down_pwd) {
		this.down_pwd = down_pwd;
	}
	public String getUp_name() {
		return up_name;
	}
	public void setUp_name(String up_name) {
		this.up_name = up_name;
	}
	public String getUp_pwd() {
		return up_pwd;
	}
	public void setUp_pwd(String up_pwd) {
		this.up_pwd = up_pwd;
	}
	public String getDown_ip() {
		return down_ip;
	}
	public void setDown_ip(String down_ip) {
		this.down_ip = down_ip;
	}
	public String getUp_ip() {
		return up_ip;
	}
	public void setUp_ip(String up_ip) {
		this.up_ip = up_ip;
	} 
}
