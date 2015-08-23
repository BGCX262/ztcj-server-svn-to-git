package com.wm927.action.blog;

import java.text.ParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.Decoder;
import com.wm927.commons.SensitiveWordFilter;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 写博客
 * @author chen
 * 修改于2013-10-22
 */
public class BlogWriter extends MiddlewareActionService{
	private static final Logger logger = Logger.getLogger(BlogWriter.class);
	// 编写博客用户ID
	private String uid;
	//博客的内容
	private String content;
	//博客的主题
	private String title;
	//博客的分类
	private String blogclass;
	//关键字
	private String keyword;
	//文章权限/默认0 0 对所有网站公开，1对智通财经社区公开
	private String purview;
	//是否为草稿(若为草稿则不发表--0，否则代表发表--1)
	private String blogtype;
	//博客ID，判断是更新博客还是新增博客
	private String blogid;
	//热图
	private String coverarturl;
	//博客简介
	private String blogsummy;
	/**
	 * 编写博客 
	 * 第一步：判断字段是否为空
	 * 第二步：判断用户和 博客是否存在
	 * 第三步：判断是发表博客还是保存到草稿箱博客(在数据库中以一个ISPUBLIC为基准)
	 * 第四步：判断是博客更新还是新增博客(如果前端发了blogid代表保存博客，否则代表新增博客)
	 * 第五步：进行相应的博客操作
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","博客提交类型不能为空"},new Object[]{uid,blogtype}))
			return ; 
		
		if(!checkUser(uid))
			return;
		//判断是发表还是保存
		if(DataUtils.checkString(blogid)){
			//发表博客
			insertBlog();
		}else{
			//更新博客
			updateBlog();
		}
	}
	
	
	
	/**
	 * 新增发表博客
	 */
	private void insertBlog() {
		if(!checkNull(new Object[]{"博客内容不能为空","博客分类不能为空","关键字不能为空","博客权限不能为空","博客标题不能为空"},
				new Object[]{content,blogclass,keyword,purview,title}))
			return ; 
		
		String cur_time = DateUtils.getCurrentTime();
		String msg = "博客保存成功!";
		if("1".equals(blogtype)){
			if(checkBlogTime()){
				return;
			}
			msg = "博客发表成功!";
		}
		
		String sql = "INSERT INTO wm_blog_info (UID,BLOGTITLE,BLOGCONTENT,IMGLIST,CLASSID,PURVIEWID,KEYWORD,PRAISENUMBER,COMMENTNUMBER,ISDELETE,ISPUBLIC,ADDIP,ADDTIME,LASTUPDATETIME,LASTUPDATEIP,BROWSENUMBER,TERMINAL,COVERARTURL,BLOGSUMMY)"
				+"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		filter();//过滤
		Object [] params = new Object[]{uid,title,content,imgUrl(),blogclass,purview,keyword,0,0,0,blogtype,getIpAddr(),cur_time,cur_time,getIpAddr(),0,terminal,coverarturl,blogsummy};
		int insertBlogCount = middlewareService.update(sql,params);
		if(insertBlogCount==0){
			logger.info("insert wm_blog_info false on this class --> "+this.getClass().getSimpleName());
			responseInfo("-3","新增博客失败");
			return;
		}
		if("1".equals(blogtype)){
			//更新博客总数量+1
			String updateCom = "UPDATE wm_user_communityInfo SET BLOGSUM = BLOGSUM+1 WHERE UID = ?";
			int blogCountSum = middlewareService.update(updateCom,uid);
			if(blogCountSum == 0){
				logger.info("更新博客总数量+1失败 ----"+this.getClass().getSimpleName());
			}
			//2013 11 1 新增发布成功增加动态
			insertDynamic(uid,uid,insertBlogCount,2,"新增博客成功，但是添加用户动态失败");
		}
		
		responseInfo("1",msg);
	}
	
	/**
	 * 验证发表博客时候只能一分钟发表一篇博客
	 * @return
	 */
	private boolean checkBlogTime (){
		//博客只能一分钟发布一篇
			String checkBlogTime = "SELECT MAX(ADDTIME) AS ADDTIME FROM wm_blog_info WHERE UID = ?";
			String adddate = middlewareService.findBy(checkBlogTime, "ADDTIME",uid);
			//判断日期是否当前日期的前一分钟
			long sqldate ;
			try {
				sqldate = DateUtils.getTimestamp(adddate);
					boolean checkdate= (System.currentTimeMillis() - sqldate)<60000;
				if(checkdate){
					responseInfo("-3","一分钟之内只能发表一篇博客");
					return true;
				}
			} catch (ParseException e) {
				logger.info(this.getClass().getSimpleName()+"格式化日期失败"+e.getMessage());
			}
			return false;
	}
	
	/**
	 * 更新博客
	 */
	private void updateBlog(){
		if(!checkBlog(blogid,"wm_blog_info"))
			return;
		String sql = "UPDATE wm_blog_info SET BLOGTITLE = ?,BLOGCONTENT = ?,CLASSID = ?," +
				"     KEYWORD = ?,PURVIEWID = ?,IMGLIST = ?, " +
				"     LASTUPDATETIME = ?, ISDELETE = ?,ISPUBLIC = ?,COVERARTURL = ?,BLOGSUMMY = ? WHERE ID = ?";
		filter();//过滤
		int updateBlogCount = middlewareService.update(sql,title,content,blogclass,keyword,purview,imgUrl(),DateUtils.getCurrentTime(),0,blogtype,coverarturl,blogsummy,blogid);
		if(updateBlogCount==0){
			logger.info("UPDATE wm_blog_info false ON this class --> "+this.getClass().getSimpleName());
			responseInfo("-3","更新博客失败");
			return;
		}
		
		String msg = "博客保存成功!";
		//博客已经发表可以更新为草稿状态
		//博客若是草稿状态可以更新为发表状态
		//所以统计发布的博客总数量时候需要进入数据库查询当前博客的状态
		String findBlogInfo = "SELECT UID,ISPUBLIC FROM wm_blog_info WHERE ID = ?";
		Map<String,Object> blogInfo = middlewareService.findFirst(findBlogInfo,blogid);
		if("1".equals(blogtype) ){
			//这里的状态是发表博客，所以判断当前博客曾经是未发表的话，则更新发表博客数量，否则不更新
			if("0".equals(blogInfo.get("ispublic"))){
				String updateCom = "UPDATE wm_user_communityInfo SET BLOGSUM = BLOGSUM+1 WHERE UID = ?";
				int blogCountSum = middlewareService.update(updateCom,blogInfo.get("uid"));
				if(blogCountSum == 0){
					logger.info("更新博客总数量+1失败 ----"+this.getClass().getSimpleName());
				}
			}
			msg = "博客更新成功!";
		}else{
			//当前状态是保存博客，所以判断当前博客是发表状态则减少发表的博客数量，否则不用更新
			if("1".equals(blogInfo.get("ispublic"))){
				String updateCom = "UPDATE wm_user_communityInfo SET BLOGSUM = BLOGSUM-1 WHERE UID = ?";
				int blogCountSum = middlewareService.update(updateCom,blogInfo.get("uid"));
				if(blogCountSum == 0){
					logger.info("更新博客总数量+1失败 ----"+this.getClass().getSimpleName());
				}
			}
		}
		responseInfo("1",msg);
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
		title = SensitiveWordFilter.doFilter(title);//过滤敏感词
		
		//过滤所有的HTML标签
		String regex2 = "<[^>]*>";
		Pattern pattern2 = Pattern.compile(regex2);
		Matcher mater2 = pattern2.matcher(content);
		blogsummy = mater2.replaceAll("");
		
		String regex3 = "\\&[a-zA-Z]{1,10};";
		Pattern pattern3 = Pattern.compile(regex3);
		Matcher mater3 = pattern3.matcher(blogsummy);
		blogsummy = mater3.replaceAll("");
		
		if(!DataUtils.checkString(blogsummy)){
			blogsummy = blogsummy.length() >100 ?blogsummy.substring(0,100):blogsummy;
		}
	}
	
	/**
	 * 筛选imgurl地址 
	 * 拼接成缩率图_thumb
	 * @return
	 */
	private String imgUrl(){
		String imgStr = "";
		String regex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
		Pattern pattern = Pattern.compile(regex);
		Matcher mater = pattern.matcher(content);
		StringBuilder str = new StringBuilder();
		String maters = "";
		int len = 0;
		while(mater.find()){
			maters = mater.group(1);
			len = maters.indexOf("blogimg");
			if( len != -1){
				maters = maters.substring(len+7,maters.length());
			}
			str.append(","+maters);
		}
		if(str.length()!=0)
			imgStr=str.substring(1);
		return imgStr;
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
	public void setContent(String content){
		if(!DataUtils.checkString(content))
			content = Decoder.decode(content);
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title){
		if(!DataUtils.checkString(title))
			title = Decoder.decode(title);
		this.title = title;
	}
	
	public String getBlogclass() {
		return blogclass;
	}

	public void setBlogclass(String blogclass) {
		this.blogclass = blogclass;
	}

	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		if(!DataUtils.checkString(keyword))
			keyword = Decoder.decode(keyword);
		this.keyword = keyword;
	}
	public String getPurview() {
		return purview;
	}
	public void setPurview(String purview) {
		this.purview = purview;
	}
	public String getBlogid() {
		return blogid;
	}
	public void setBlogid(String blogid) {
		this.blogid = blogid;
	}

	public String getCoverarturl() {
		return coverarturl;
	}

	public void setCoverarturl(String coverarturl) {
		this.coverarturl = coverarturl;
	}

	public String getBlogsummy() {
		return blogsummy;
	}

	public void setBlogsummy(String blogsummy) {
		this.blogsummy = blogsummy;
	}

	public String getBlogtype() {
		return blogtype;
	}

	public void setBlogtype(String blogtype) {
		this.blogtype = blogtype;
	}

}
