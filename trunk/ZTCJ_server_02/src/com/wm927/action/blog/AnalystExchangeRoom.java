package com.wm927.action.blog;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.Decoder;
import com.wm927.commons.PageUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 分析师房间交流区
 * @author chen
 *
 */
public class AnalystExchangeRoom extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(AnalystExchangeRoom.class);
	private String blogid;
	private String uid;
	private String ouid;
	private String type;//0代表向分析师提问，1代表向网友提问，2分析师回答
	private String content;//提问的内容
	private String comid;//分析师回答的问题ID
	private String comcontent;//分析师回复时候需要前端将提问的内容返回过来
	private String transtype;
	private String page;
	private String number;
	private String lastid;
	private String size;
	private String bdate;
	private String edate;
	/**
	 * 此方法包含用户向分析师提问，用户向网友提问，分析师回答网友与分析师回答用户(这里都称作分析师回答)
	 * 1，根据前端传来type值判断是那种交流形式
	 * 2，如果是分析师回答的话需要插入另外一张新的表，用户做分析师问答的记录
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","分析师ID不能为空","交流类型不能为空","交流内容不能为空"}
					,new Object[]{uid,ouid,type,content}))
			return ;
		String addtime = DateUtils.getCurrentTime();
		if(!"2".equals(type)){
			//插入普通交流表
			String sql = "INSERT INTO wm_blog_exchange (UID,ANAID,TYPE,COMID,CONTENT,ADDTIME,ADDIP)VALUES(?,?,?,?,?,?,?)";
			int count = middlewareService.update(sql,uid,ouid,type,comid,content,addtime,getIpAddr());
			if(count == 0){
				responseInfo("-1","发表交流信息失败");
				return ;
			}
			//如果是0，代表需要统计向分析师提问的数量
			if("0".equals(type)){
				String questionSql = "UPDATE wm_user_communityInfo SET QUESTIONSUM=QUESTIONSUM+1 WHERE UID = ?";
				int  quesCount = middlewareService.update(questionSql,ouid);
				if(quesCount == 0){
					logger.info("统计向分析师提问数量失败");
				}
			}
			
			}
		
			else{
				if(!checkNull(new Object[]{"提问ID不能为空","分析师回复内容不能为空"},new Object[]{comid,comcontent}))
					return ;
				//插入普通交流表
				String sql = "INSERT INTO wm_blog_exchange (UID,ANAID,TYPE,COMID,CONTENT,ADDTIME,ADDIP)VALUES(?,?,?,?,?,?,?)";
				int count = middlewareService.update(sql,uid,ouid,type,comid,comcontent,addtime,getIpAddr());
				if(count == 0){
					responseInfo("-1","发表交流信息失败");
					return ;
				}
				//更新交流表是否已经回答
				String updateAnswerSql = "UPDATE wm_blog_exchange SET ISANSWER=1 WHERE ID=?";
				middlewareService.update(updateAnswerSql,comid);
				//如果是分析师回答则
				//插入分析师房间交流表
				//插入分析师交流表
				String sendInfo = "SELECT UID,CONTENT,ADDTIME FROM wm_blog_exchange WHERE ID = ?";
				Map<String,Object> mapInfo = middlewareService.findFirst(sendInfo,comid);
				String insertAnalystSql = "INSERT INTO wm_blog_question(UID,QUESTIONUID,QUESTIONCONTENT,ANSWERCONTENT,QUESTIONTIME,ADDTIME)VALUES(?,?,?,?,?,?)";
				int insertCount = middlewareService.update(insertAnalystSql,ouid,mapInfo.get("uid"),mapInfo.get("content"),comcontent,mapInfo.get("addtime"),addtime);
				if(insertCount == 0){
					logger.info("插入分析师交流表失败");
				}else{
					insertDynamic(ouid,ouid, insertCount, 8, "插入交流动态失败");
				}
				
		}
				 responseInfo("1","成功");
	}
	
	/**
	 * 答疑记录
	 */
	public void exchangeRecord(){
		if(!checkNull(new Object[]{"用户ID不能为空","查询开始日期不能为空","查询结束日期不能为空"},new Object[]{uid,bdate,edate}))
			return;
		String dateStr = "";
		dateStr = " AND ADDTIME BETWEEN '" + bdate + " 00:00:00' AND ";
		dateStr += "'"+edate+" 23:59:59'";
		
		String sql = "SELECT ID ,UID AS ANSWERUID, QUESTIONUID,QUESTIONCONTENT,ANSWERCONTENT," +
				"  DATE_FORMAT(QUESTIONTIME ,'%Y-%m-%d %H:%i:%s') AS QUESTIONTIME," +
				"  DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS ANSWERTIME FROM wm_blog_question" +
				"  WHERE UID = "+uid+dateStr +" ORDER BY ID DESC";
		String questionInfo = "SELECT NICKNAME AS QUESTIONNICKNAME , USERNAME AS QUESTIONUSERNAME , PHOTO AS QUESTIONPHOTO,ROLETAG AS QUESTIONROLETAG FROM wm_user_info WHERE UID = ?";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> ans_info = findUserInfo(uid);
		Map<String,Object> que_info = null;
		for(Map<String,Object> map : list_value){
			que_info = middlewareService.findFirst(questionInfo,map.get("questionuid"));
			map.putAll(que_info);map.putAll(ans_info);
		}
		Map<String,Object> mapInfo = findPageSize(page, number, sql);
		responseInfo("1","成功",list_value,mapInfo);
	}
	
	/**
	 * 今日未回答提问数目，今日回答提问数目，今日提问人数数目
	 */
	public void answerQuestionCount(){
		if(!checkNull(new Object[]{"用户ID不能为空","获取类型不能为空","被索引的ID不能为空"},new Object[]{uid,type,ouid}))
			return;
		int ttype = DataUtils.praseNumber(type, 0);
		String sql = "";
		//type类型0代表今日未回答数目，1代表今日回答数目,2代表今日提问人数，3代表向分析师提问数目
		String dateStr = "";
		String dtranstype = DateUtils.praseBeginDate(transtype);
		if( !DataUtils.checkString(dtranstype)){
			dateStr = " AND ADDTIME BETWEEN '" + dtranstype + " 00:00:00' AND ";
			dateStr += "'"+DateUtils.getCurrentDate()+" 23:59:59'";
		}
		switch(ttype){
			case 0:
				sql = "SELECT COUNT(*) AS COUNT FROM wm_blog_exchange WHERE ANAID="+ouid+" AND TYPE=0  AND ISANSWER=0 AND ISDELETE=0 ";
				break;
			case 1:
				sql = "SELECT COUNT(*) AS COUNT FROM wm_blog_exchange WHERE ANAID="+ouid+"  AND ISANSWER=1 AND ISDELETE=0";
				break;
			case 2:
				sql = "SELECT COUNT(DISTINCT(UID)) AS COUNT FROM wm_blog_exchange WHERE  ANAID="+ouid+"  AND TYPE=0 AND ISDELETE=0  AND ISANSWER=0  ";
				break;
			case 3:
				sql = "SELECT COUNT(*) AS COUNT FROM wm_blog_exchange WHERE UID = "+uid+" AND ANAID="+ouid+" AND TYPE=0 AND ISDELETE=0  ";
				break;
		}
		sql +=dateStr;
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		responseInfo("1","成功",list_value);
		
	}
	
	/**
	 * 今日未回答的问题
	 */
	public void unAnswerQuestion(){
		String dateStr = "";
		String dtranstype = DateUtils.praseBeginDate(transtype);
		if( !DataUtils.checkString(dtranstype)){
			dateStr = " AND ADDTIME BETWEEN '" + dtranstype + " 00:00:00' AND ";
			dateStr += "'"+DateUtils.getCurrentDate()+" 23:59:59'";
		}
		String sql = "SELECT ID,CONTENT,UID,ANAID,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS ADDTIME FROM wm_blog_exchange WHERE ANAID="+uid+" AND TYPE=0 AND ISANSWER=0 AND ISDELETE=0 "+dateStr+" ORDER BY ID DESC";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> map_info = null;
		for(Map<String,Object> map : list_value){
			map_info = findUserInfo(map.get("uid"));
			map.putAll(map_info);
		}
		Map<String,Object> mapInfo = findPageSize(page, number, sql);
		responseInfo("1","成功",list_value,mapInfo);
	}
	
	/**
	 * 用户的提问包括已回答的
	 */
	public void userQuestion(){
		String dateStr = "";
		String dtranstype = DateUtils.praseBeginDate(transtype);
		if( !DataUtils.checkString(dtranstype)){
			dateStr = " AND ADDTIME BETWEEN '" + dtranstype + " 00:00:00' AND ";
			dateStr += "'"+DateUtils.getCurrentDate()+" 23:59:59'";
		}
		String sql = "SELECT ID,CONTENT,ISANSWER,UID,ANAID,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS ADDTIME FROM wm_blog_exchange WHERE UID="+uid+" AND TYPE=0 AND ISDELETE=0 "+dateStr+"ORDER BY ID DESC";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> map_info = null;
		String ansSql = "SELECT ID AS ANSID,CONTENT AS ANSCONTENT,ANAID AS ANSUID,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS ANSWERTIME FROM wm_blog_exchange WHERE COMID=?  AND ISDELETE=0 ";
		Map<String,Object> ansMap = null;
		for(Map<String,Object> map : list_value){
			//0代表未回答，1代表已回答
			if("0".equals(map.get("isanswer")))
				continue ;
			ansMap = middlewareService.findFirst(ansSql,map.get("id"));
			if(ansMap != null && !ansMap.isEmpty()){
				map.putAll(ansMap);
			}
			map_info = findUserInfo(map.get("anaid"));
			map.putAll(map_info);
		}
		Map<String,Object> mapInfo = findPageSize(page, number, sql);
		responseInfo("1","成功",list_value,mapInfo);
		
	}
	
	/**
	 * 获取网友交流列表
	 */
	public void findOldExchange(){
		if(!checkNull(new Object[]{"分析师UID不能为空"},new Object[]{uid}))
			return ; 
		String sql = "SELECT ID,UID,CONTENT,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS ADDTIME FROM wm_blog_exchange WHERE ANAID="+uid+" AND TYPE=1 AND ISDELETE=0 ";
		if(!DataUtils.checkString(lastid)){
			if("0".equals(type)){
				//新数据
				sql += " AND ID > "+lastid;
			}else{
				//旧数据
				sql += " AND ID < "+lastid;
			}
		}
		sql += " ORDER BY ID DESC LIMIT " +PageUtils.getPageCount(size);
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		Map<String,Object> map_value = null;
		for(Map<String,Object> map : list_value){
			map_value = findUserInfo(map.get("uid"));
			map.putAll(map_value);
		}
		responseInfo("1","成功",list_value);
	}
	
	/**
	 * 删除问答
	 */
	public void delQuestion(){
		if(!checkNull(new Object[]{"问答ID不能为空","用户UID不能为空"},new Object[]{blogid,uid}))
			return ;
		String sql = "UPDATE wm_blog_exchange SET ISDELETE=1 WHERE ID = ? AND UID = ?";
		int count = middlewareService.update(sql,blogid,uid);
		if(count == 0){
			responseInfo("-1","问答删除失败,问答不存在或者已经删除");
			return ;
		}
		responseInfo("1","问答已删除");
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getOuid() {
		return ouid;
	}

	public void setOuid(String ouid) {
		this.ouid = ouid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		if(!DataUtils.checkString(content))
			content = Decoder.decode(content);
		this.content = content;
	}

	public String getComid() {
		return comid;
	}

	public void setComid(String comid) {
		this.comid = comid;
	}

	public String getComcontent() {
		return comcontent;
	}

	public void setComcontent(String comcontent) {
		if(!DataUtils.checkString(comcontent))
			comcontent = Decoder.decode(comcontent);
		this.comcontent = comcontent;
	}

	public String getTranstype() {
		return transtype;
	}

	public void setTranstype(String transtype) {
		this.transtype = transtype;
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

	public String getBdate() {
		return bdate;
	}

	public void setBdate(String bdate) {
		this.bdate = bdate;
	}

	public String getEdate() {
		return edate;
	}

	public void setEdate(String edate) {
		this.edate = edate;
	}

	public String getLastid() {
		return lastid;
	}

	public void setLastid(String lastid) {
		this.lastid = lastid;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getBlogid() {
		return blogid;
	}

	public void setBlogid(String blogid) {
		this.blogid = blogid;
	}



}
