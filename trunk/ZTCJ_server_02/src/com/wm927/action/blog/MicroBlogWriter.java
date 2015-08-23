package com.wm927.action.blog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * 写微博
 * @author chen
 *
 */
public class MicroBlogWriter extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(MicroBlogWriter.class);
	private String uid;
	private String content;//微博内容
	private String purview ="0";//0代表对所有人公开，1代表对自己关注的人公开
	private String videouri;//视频地址
	private String videoimg;//视频缩略图
	private String videotitle;//视频标题
	private String videolink;
	private String videohosts;
	private String classid ;//0代表观点，1代表策略，2代表秘笈
	private String price ;
	private String attachment;//附件地址
	private String attachsize;//附件大小
	private String attachname;//附件名称
	private String title;//策略或者秘笈标题
	private String timeliness = "3";//0代表一天，1代表一周，2代表一个月，3代表永久
	private String imglist;//图片地址---这里不同博客，图片是不放在发表的内容里面嵌入的
	/**
	 * 发表微博
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空,","微博内容不能为空"},new Object[]{uid,content}))
			return;
		String addtime = DateUtils.getCurrentTime();
		filter();
		String sql = "INSERT INTO wm_blog_micro_info(UID,CONTENT,IMGLIST,VIDEOURI,VIDEOIMG,VIDEOTITLE,VIDEOLINK,VIDEOHOSTS,ADDTIME)VALUES(?,?,?,?,?,?,?,?,?)";
		int insertCount = middlewareService.update(sql,uid,content,imglist,videouri,videoimg,videotitle,videolink,videohosts,addtime);
		if(insertCount == 0){
			responseInfo("-1","发表微博失败");
			return;
		}
		int dyCount = insertDynamic(uid,uid, insertCount, 3, "插入微博动态失败");
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("did", dyCount+"");map_value.put("rid", insertCount+"");
		list_value.add(map_value);
		responseInfo("1","发表微博成功",list_value);
		
	}
	
	/**
	 * 发表观点、策略、秘笈
	 */
	public void publishView(){
		if(!checkNull(new Object[]{"用户ID不能为空","内容不能为空","博客类型不能为空","时效性不能为空"},new Object[]{uid,content,classid,timeliness}))
			return ; 
		String addtime = DateUtils.getCurrentTime();
		String sql = "INSERT INTO wm_blog_view (UID,CONTENT,CLASSID,PURVIEWID,IMGLIST,ATTACHMENT" +
				",ATTACHNAME,ATTACHSIZE,VIDEOTITLE,VIDEOHOSTS,VIDEOLINK,VIDEOIMG,VIDEOURI,TITLE,PRICE,EXPIRES,TIMELINESS,ADDTIME,ADDIP)" +
				" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		int insertCount = middlewareService.update(sql,uid,content,classid,purview,imglist,
				attachment,attachname,attachsize,videotitle,videohosts,videolink,videoimg,videouri,title,price,expiresTime(),timeliness,addtime,getIpAddr());
		if(insertCount == 0){
			responseInfo("-1","发表失败");
			return ; 
		}
		String updateViewSql = "UPDATE wm_user_communityInfo SET VIEWSUM=VIEWSUM+1 WHERE UID = ?";
		if(!"0".equals(classid)){
			updateViewSql = "UPDATE wm_user_communityInfo SET PLOTSUM=PLOTSUM+1 WHERE UID = ?";
		}
		int viewCount = middlewareService.update(updateViewSql,uid);
		if(viewCount == 0){
			logger.info("插入累积观点或者策略失败");
		}
		int dyCount = insertDynamic(uid,uid, insertCount, DataUtils.praseNumber(classid, 0)+4, "插入策略动态失败");
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("did", dyCount+"");map_value.put("rid", insertCount+"");
		list_value.add(map_value);
		responseInfo("1","发表成功",list_value);
	}
	
	/**
	 * 添加过期时间，0代表一天，1代表一周，2代表一个月，3代表永久
	 * @return
	 */
	private String expiresTime(){
		String expires = "";
		int time = DataUtils.praseNumber(timeliness, 0);
		switch(time){
		case 0 : 
			expires = DateUtils.dateCalculate(60*60*24, 0, false);
			break;
		case 1 : 
			expires = DateUtils.dateCalculate(60*60*24*7, 0, false);
			break;
		case 2 : 
			expires = DateUtils.dateCalculate(60*60*24*30, 0, false);
			break;
		case 3 : 
			expires = "";
			break;
		}
		return expires;
	}
	
	/**
	 * 筛选内容，将content带有js脚本的代码删除
	 * @return
	 */
	private void filter(){
		String regex = "<javascript>[^>]*</javascript>";
		Pattern pattern = Pattern.compile(regex);
		Matcher mater = pattern.matcher(content);
		content = mater.replaceAll("");//替换带有脚本的内容
		content = SensitiveWordFilter.doFilter(content);//过滤敏感词
		if(!DataUtils.checkString(attachname)){
			attachname = SensitiveWordFilter.doFilter(attachname);//过滤敏感词
		}
		if(!DataUtils.checkString(videotitle)){
			videotitle = SensitiveWordFilter.doFilter(videotitle);//过滤敏感词
		}
		if(!DataUtils.checkString(title)){
			title = SensitiveWordFilter.doFilter(title);//过滤敏感词
		}
	}
	
	
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		if(!DataUtils.checkString(content))
		content = Decoder.decode(content);
		this.content = content;
	}
	public String getPurview() {
		return purview;
	}
	public void setPurview(String purview) {
		this.purview = purview;
	}
	public String getVideouri() {
		return videouri;
	}
	public void setVideouri(String videouri) {
		if(!DataUtils.checkString(videouri))
			videouri = Decoder.decode(videouri);
		this.videouri = videouri;
	}
	public String getVideoimg() {
		return videoimg;
	}
	public void setVideoimg(String videoimg) {
		this.videoimg = videoimg;
	}
	public String getVideotitle() {
		return videotitle;
	}
	public void setVideotitle(String videotitle) {
		if(!DataUtils.checkString(videotitle))
			videotitle = Decoder.decode(videotitle);
		this.videotitle = videotitle;
	}

	public String getClassid() {
		return classid;
	}

	public void setClassid(String classid) {
		this.classid = classid;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		if(!DataUtils.checkString(attachment))
			attachment = Decoder.decode(attachment);
		this.attachment = attachment;
	}

	public String getAttachsize() {
		return attachsize;
	}

	public void setAttachsize(String attachsize) {
		this.attachsize = attachsize;
	}

	public String getAttachname() {
		return attachname;
	}

	public void setAttachname(String attachname) {
		if(!DataUtils.checkString(attachname))
			attachname = Decoder.decode(attachname);
		this.attachname = attachname;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		if(!DataUtils.checkString(title))
			title = Decoder.decode(title);
		this.title = title;
	}

	public String getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(String timeliness) {
		this.timeliness = timeliness;
	}

	public String getImglist() {
		return imglist;
	}

	public void setImglist(String imglist) {
		if(!DataUtils.checkString(imglist))
			imglist = Decoder.decode(imglist);
		this.imglist = imglist;
	}

	public String getVideolink() {
		return videolink;
	}

	public void setVideolink(String videolink) {
		if(!DataUtils.checkString(videolink))
			videolink = Decoder.decode(videolink);
		this.videolink = videolink;
	}

	public String getVideohosts() {
		return videohosts;
	}

	public void setVideohosts(String videohosts) {
		if(!DataUtils.checkString(videohosts))
			videohosts = Decoder.decode(videohosts);
		this.videohosts = videohosts;
	}

	
	
}
