package com.wm927.action.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.CallCodeUtils;
import com.wm927.commons.Contants;
import com.wm927.commons.DataUtils;
import com.wm927.commons.MoneyCodeUtils;
import com.wm927.commons.PageUtils;
import com.wm927.commons.ResponseCodeUtils;
import com.wm927.service.impl.MiddlewareActionService;
/**
 * 动态在这里有3个地方出现，这里是一个总的入口，根据各个终端穿来不同的参数来返回不同的动态数据
 * 默认是加载首页动态的内容
 * 动态加载接口
 * @作者  chen
 * 
 */
public class DynamicLoad extends MiddlewareActionService {
	//传人的uid为0，取所有人的动态，传人的uid不为0，根据isself的参数确定取自己或他人动态
	private String live;//0代表拿动态数据，1代表拿直播数据  默认0
	private String lastid;//最后1条动态ID
	private String type = "1";//翻页类型   (0代表加载新的数据，1代表加载老的数据)
	private String condition = "1,2,3,4,5,6,7";//0发布喊单，1发布平仓，2发布博客，3发布微博，4发布分析，5发布策略，
								   //6发布秘笈，7获利(包括购买服务，赠送好评，赠送鲜花等具体往礼物详细表中查找),8问答
								   //默认查看除问答以外的所有动态(首页动态)
	private String uid;//用户id
	private String isself = "0";//是否自己动态标识(0代表自己关注人的动态，1代表自己的动态)
	private String size = "10"; //加载的数据条数
	
	
	/**
	 * 动态加载入口
	 * @作者 chen
	 * 2013/10/16
	 */
	
	public void execute(){
			if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
				return ; 
			if(!checkUser(uid))
				return;
			StringBuilder sql = new StringBuilder();
			StringBuilder friend = new StringBuilder();
			
			sql.append("SELECT ID,UID,RID,TYPE,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME'  FROM wm_user_dynamic WHERE LENGTH(RID)>0 AND ");
			
			friend.append(uid + "," + Contants.DEFAULT_OID);//不管好友动态还是自己的动态，都需要加上本身的UID,和公司系统ID
			if("0".equals(isself)){//自己关注的人动态
				//加载的自己关注的人的所有动态
				String friendSql = "SELECT FID FROM wm_user_friends WHERE UID = ?";
				List<Map<String,Object>> friend_list = middlewareService.find(friendSql,uid);
				for( Map<String,Object> map : friend_list){
					friend.append(", " );
					friend.append(map.get("fid"));
				}
			}
			//拼接好友UID
			if(condition.indexOf("7") != -1){
				//分类为获利的
				sql.append(" ( UID IN (" + friend.toString() + ")") ;
				sql.append(" OR OUID IN (" + friend.toString() + ") )") ;
			}else{
				sql.append(" UID IN (" + friend.toString() + ")") ;
			}
			
			
			//分类条件
			//此处仅为了防止他人传入错误的参数，导致程序出错
			try{
				condition.split(",");
			}catch(Exception e){
				condition = "1,2,3,4,5,6,7";
			}
			sql.append(" AND TYPE IN (" + condition +")");
			
			//首次加载数据是否是没有存在新老数据的，只有是点击加载更多的时候才会出现加载新老数据，这里就必须配套使用lastid
			if(!DataUtils.checkString(lastid)){
				if("0".equals(type)){//新的数据
					sql.append(" AND ID > " + lastid);
				}else{//老数据
					sql.append(" AND ID < " + lastid);
				}
			}
			sql.append(" ORDER BY ID DESC LIMIT " + PageUtils.getPageCount(size));
			
			List<Map<String,Object>> list_value = middlewareService.find(sql.toString());
			if(list_value == null || list_value.isEmpty()){
				responseInfo("1","返回成功");
				return;
			}
			getDynamicInfo(list_value);
	}
	
	
	/**
	 * 获取动态数据
	 * @param list_value
	 */
	private void getDynamicInfo(List<Map<String,Object>> list){
		String attentionstate = "0"; 
		//分析师鲜花数//普通用户鲜花为0
		String flowCount = "0";
		String countflow = "SELECT FLOWCOUNT FROM wm_user_communityInfo WHERE UID = ?";
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = null;
		Map<String,Object> map_value1 = null;
		for(Map<String,Object> map:list){
			map_value = conditionInfo(map);
			if(map_value == null || map_value.isEmpty()){
				//由于时间的原因，当动态中的一个条数数据已经被删除了，但是动态还是显示
				//这里的数据就不应该显示出来，所以返回一个空数据
				//让各个终端去判断，如果为空，则显示当前数据已删除
				list_value.add(map_value);
				continue;
			}
			//手机端需要关注状态和用户角色
			if(ResponseCodeUtils.ANDROID_PORT.equals(terminal)||ResponseCodeUtils.IPHONE_PORT.equals(terminal)){
				attentionstate = attentionState(uid, map.get("uid")+"");
				map_value.put("attentionstate", attentionstate);
			}
			flowCount = middlewareService.findBy(countflow,"FLOWCOUNT",map.get("uid"));
			map_value1 = findUserInfo(map.get("uid"));
			map_value.putAll(map_value1);
			map_value.put("flowcount",flowCount);
			map_value.put("addtime", map.get("addtime"));//发布动态时间
			map_value.put("uid", map.get("uid"));//用户id
			map_value.put("did", map.get("id"));//动态id
			map_value.put("rid", map.get("rid"));//实际数据id
			map_value.put("type", map.get("type"));//数据类型
			list_value.add(map_value);
		}
		responseInfo("1","成功",list_value);
	}
	
	/**
	 * 做一个工厂分类，根据获取的类型值传入对应的方法
	 * @param map_value
	 * @param id
	 * @return
	 */
	private Map<String,Object> conditionInfo(Map<String,Object> map_value){
		int ttype = Integer.parseInt(String.valueOf(map_value.get("type")));
		Map<String,Object> map = null;
		switch(ttype){
			case 0 : 
				map = getCallInfo(map_value);
				break;
			case 1 : 
				map = getCallInfo(map_value);
				break;
			case 2 :
				map = getBlogInfo(map_value);
				break;
			case 3 : 
				map = getMicroBlogInfo(map_value);
				break;
			case 4 : 
				map = getAnalysisInfo(map_value);
				break;
			case 5 : 
				map = getCircleInfo(map_value);
				break;
			case 6 : 
				map = getCheats(map_value);
				break;
			case 7 : 
				map = getGiftInfo(map_value);
				break;
			case 8 : 
				map = getQuestionInfo(map_value);
				break;
			default : break;
		}
		return map ;
	}
	
	/**
	 * 获取博客信息
	 * @return
	 */
	private Map<String,Object> getBlogInfo( Map<String,Object> map_value   ){
		String blogSql = "SELECT BLOGTITLE,IMGLIST, BLOGSUMMY,COMMENTNUMBER ,PRAISENUMBER FROM wm_blog_info WHERE ISDELETE=0 AND  ID =  " +  map_value.get("rid");
		Map<String,Object> map = middlewareService.findFirst(blogSql);
		if(map==null || map.isEmpty()){
			return new HashMap<String,Object>() ; 
		}
			String commentid = "0";
			if(!"0".equals(map.get("commentnumber"))){
				//将当前博客最新的评论id返回出去，防止用户显示评论数6但是实际加载的评论数已经是7了
				String commentSql = "SELECT ID AS COMMENTID FROM wm_blog_comment WHERE COMMENTBLOGID = ? AND COMMENTLEVEL = 1  AND ISDELETE=0 AND  UID = ? ORDER BY ID DESC";
				commentid = middlewareService.findBy(commentSql, "COMMENTID" , map.get("id"),map_value.get("uid"));
			}
			map.put("commentid", commentid);
		
		return map;
	}
	/**
	 * 获取微博信息
	 * @return
	 */
	private Map<String,Object> getMicroBlogInfo( Map<String,Object> map_value   ){
		String blogSql = "SELECT CONTENT,IMGLIST,COMMENTNUMBER ,PRAISENUMBER,VIDEOURI,VIDEOIMG,VIDEOTITLE,VIDEOHOSTS,VIDEOLINK,BROWSENUMBER FROM wm_blog_micro_info WHERE ISDELETE=0 AND ID =  " +  map_value.get("rid");
		Map<String,Object> map = middlewareService.findFirst(blogSql);
		if(map==null || map.isEmpty()){
			return new HashMap<String,Object>() ; 
		}
			String commentid = "0";
			if(!"0".equals(map.get("commentnumber"))){
				//将当前博客最新的评论id返回出去，防止用户显示评论数6但是实际加载的评论数已经是7了
				String commentSql = "SELECT ID AS COMMENTID FROM wm_blog_micro_comment WHERE ISDELETE=0 AND  COMMENTBLOGID = ? AND COMMENTLEVEL = 1 AND UID = ? ORDER BY ID DESC";
				commentid = middlewareService.findBy(commentSql, "COMMENTID" , map.get("id"),map_value.get("uid"));
			}
			map.put("commentid", commentid);
		
		return map;
	}
	
	/**
	 * 获取分析信息
	 * @param map_value
	 * @param id
	 * @return
	 */
	private Map<String,Object> getAnalysisInfo(Map<String,Object> map_value  ){
		String sql = "SELECT CONTENT ,IMGLIST, PURVIEWID,VIDEOURI,VIDEOIMG,VIDEOTITLE,VIDEOLINK,VIDEOHOSTS FROM wm_blog_view WHERE ISDELETE = 0 AND ID = ? ";
		Map<String,Object> map = middlewareService.findFirst(sql,map_value.get("rid"));
		if(map==null || map.isEmpty()){
			return new HashMap<String,Object>() ; 
		}
		Object purid;//0代表对所有人开放，1代表对关注的人开放
		String attention = "0";
		String open = "0";//查看权限，0代表可以观看，1代表隐藏
		Object map_uid = "";
			purid = map.get("purviewid");
			map_uid = map_value.get("uid");
			if(!uid.equals(map_uid)){//不是自己的动态
				if("1".equals(purid)){
					attention = attentionState(uid, map_uid);
					//当前发布分析的人未关注当前观看此分析用户
					if("0".equals(attention) || "1".equals(attention)){
						//是否购买了发布分析人的策略服务
						if(!checkByCircleService(uid,map_uid)){
							open = "1";
						}
					}
				}
			}
			map.put("open", open);
		return map;
	}
	
	/**
	 * 获取策略信息
	 * 策略可以单条出售，也可以购买策略服务
	 * @return
	 */
	private Map<String,Object> getCircleInfo(Map<String,Object> map_value ){
		//策略
		String sql = "SELECT CONTENT , IMGLIST ,PURVIEWID, TITLE ,VIDEOURI,VIDEOIMG,VIDEOTITLE,VIDEOLINK,VIDEOHOSTS, PRICE ,EXPIRES,TIMELINESS,BUYNUMBER" +
					  " FROM wm_blog_view WHERE ISDELETE = 0 AND ID = "+map_value.get("rid") ;
		Map<String,Object> map = middlewareService.findFirst(sql);
		if(map==null || map.isEmpty()){
			return new HashMap<String,Object>() ; 
		}
		String open = "0";
		String attention = "0";
		Object map_uid = map_value.get("uid");
			if(!uid.equals(map_uid)){//不是自己的秘笈
			//只对自己关注的人查看
			if("1".equals(map.get("purviewid"))){
				attention = attentionState(uid, map_uid);
				//当前发布分析的人未关注当前观看此分析用户
				if("0".equals(attention) || "1".equals(attention)){
					//是否购买了发布分析人的策略服务
					if(!checkByCircleService(uid, map_uid)){
						//判断是否购买了单条策略
						if(!checkByCircle(uid, map_uid, map_value.get("rid"))){
							open = "1";
						}
					}
				}
			//对策略进行单价出售
			}else{
				//按照价格出售
				//是否购买了发布分析人的策略服务
				if(!checkByCircleService(uid, map_value.get("uid"))){
					//判断是否购买了单条策略
					if(!checkByCircle(uid, map_uid, map_value.get("rid"))){
						open = "1";
					}
				}
			}
		}
		map.put("open", open);
		return map;
	}
	
	/**
	 * 获取秘籍信息
	 * 秘籍只能是单条发售，购买策略服务的人也是不能看的
	 * @param map_value
	 * @return
	 */
	private Map<String,Object> getCheats(Map<String,Object> map_value  ){
		//策略
		String sql = "SELECT CONTENT ,ATTACHNAME,CLASSID, PURVIEWID , IMGLIST,VIDEOURI,VIDEOIMG,VIDEOTITLE,VIDEOLINK,VIDEOHOSTS , TITLE ,ATTACHMENT,ATTACHSIZE, PRICE ,EXPIRES,TIMELINESS,BUYNUMBER" +
						  " FROM wm_blog_view WHERE ISDELETE = 0 AND ID = "+map_value.get("rid");
		Map<String,Object> map = middlewareService.findFirst(sql);
		if(map==null || map.isEmpty()){
			return new HashMap<String,Object>() ; 
		}
		String open = "0";
		String attention = "0";
		Object map_uid = map_value.get("uid");
			if(!uid.equals(map_uid)){//不是自己的秘笈
			//只对自己关注的人查看
			if("1".equals(map.get("purviewid"))){
				attention = attentionState(uid, map_uid);
				//当前发布分析的人未关注当前观看此分析用户
				if("0".equals(attention) || "1".equals(attention)){
					//判断是否购买了单条秘籍
					if(!checkByCircle(uid, map_uid, map_value.get("rid"))){
						open = "1";
					}
				}
			//对策略进行单价出售
			}else{
				//按照价格出售
				//判断是否购买了单条策略
				if(!checkByCircle(uid, map_uid, map_value.get("rid"))){
					open = "1";
				}
			}
		}
		map.put("open", open);
		return map;
	}
	
	/**
	 * 获取获利信息(包括好评，礼物)
	 * @return
	 */
	private Map<String,Object> getGiftInfo(Map<String,Object> map_value ){
		String sql = "SELECT UID, GIFTID,TYPE AS GIFTTYPE ,SENDID  ,NUMBER ,PRICE ,MONEY FROM wm_user_gift_detail WHERE ID = "+map_value.get("rid");
		Map<String,Object> map = middlewareService.findFirst(sql);
		if(map==null || map.isEmpty()){
			return new HashMap<String,Object>() ; 
		}
		if("2".equals(map.get("type"))){//2分类为礼物，里面只存在礼物类型id，需要转化成礼物名称
			String sql1 = "SELECT NAME AS GIFTCONTENT,GIFTIMG FROM wm_user_gift_type WHERE ID = ? ";
			map.putAll(middlewareService.findFirst(sql1, map.get("giftid")));
		}else{
			String sql1 = "SELECT NAME AS GIFTCONTENT,GIFTIMG FROM wm_user_gift WHERE ID = ? ";
			map.putAll(middlewareService.findFirst(sql1, map.get("gifttype")));
		}
		String sendInfo = "SELECT NICKNAME AS SENDNICKNAME , USERNAME AS SENDUSERNAME , PHOTO AS SENDPHOTO FROM wm_user_info WHERE UID = ?";
		String recInfo = "SELECT NICKNAME AS RECNICKNAME , USERNAME AS RECUSERNAME , PHOTO AS RECPHOTO FROM wm_user_info WHERE UID = ?";
		map.putAll(middlewareService.findFirst(sendInfo,map.get("sendid")));
		map.putAll(middlewareService.findFirst(recInfo,map.get("uid")));
		return map;
	}
	/**
	 * 获取喊单信息
	 * @return
	 */
	private Map<String,Object> getCallInfo(Map<String,Object> map_value  ){
		StringBuilder hdMessage = new StringBuilder();
		Map<String,Object> map = new HashMap<String,Object>();
		String trade = "买进";
		String profit = "";//盈利点数
		String open = "0";//是否查看
		//获取喊单数据
		String callInfo = "SELECT EXITPRICE,CODE,TRADE,PRICE,PROFIT FROM wm_bill_info WHERE ID = " + map_value.get("rid");
		Map<String,Object> callMap = middlewareService.findFirst(callInfo);
		if(callMap==null || callMap.isEmpty()){
			return map ; 
		}
		Object code  = callMap.get("code");//货币代码
		Object chinaCode = MoneyCodeUtils.CODE_MAP.get(code);
		Object map_uid = map_value.get("uid");
		if("0".equals(map_value.get("type"))){
			//这里主要是喊单作权限控制
			String openCallSql = "SELECT OPENCALL FROM wm_user_info WHERE UID = ?";
			String openCall = middlewareService.findBy(openCallSql,"OPENCALL", map_value.get("uid"));
			//1代表对喊单权限是关闭的
			if(!uid.equals(map_uid)){
				if("1".equals(openCall)){
					//是否购买了发布喊单的策略服务
					if(!checkByCircleService(uid, map_uid)){
						open = "1";
					}
				}
			}
			//喊单
			if("1".equals(callMap.get("trade"))){
				trade = "卖出";
			}
			String price = returnCallMessage(callMap.get("price"),code);
			hdMessage.append("喊单 :在"+price+trade +" "+chinaCode);
			map.put("price", price);
		}else{
			//盈利
			profit = String.valueOf(callMap.get("profit"));
			code = callMap.get("code");
			if(DataUtils.checkString(profit)){
				profit = "0.00";
			}
			//查询当前人的姓名
			Map<String,Object> userinfo = findUserInfo(map_value.get("uid"));
			Object name = userinfo.get("nickname");
			if(DataUtils.checkString(name)){
				name = userinfo.get("username");
			}
			hdMessage.append("平仓 ： 在"+chinaCode+" 收益"+profit+"点"+"大家给"+name+"一点礼物鼓励下吧");
			map.put("profit", profit);
		}
		map.put("hdmessage",hdMessage);
		map.put("code", chinaCode);
		map.put("trade", trade);
		map.put("open", open);
		return map;
	}
	/**
	 * 获取问答信息
	 * 动态插入的分析师的UID和分析师回答的ID
	 * @return
	 */
	private Map<String,Object> getQuestionInfo(Map<String,Object> map_value   ){
		String sql = "SELECT ID ,UID AS ANSWERUID, QUESTIONUID,QUESTIONCONTENT,ANSWERCONTENT," +
				"  DATE_FORMAT(QUESTIONTIME ,'%Y-%m-%d %H:%i:%s') AS QUESTIONTIME," +
				"  DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS ANSWERTIME FROM wm_blog_question WHERE ID = ?";
		Map<String,Object> map = middlewareService.findFirst(sql,map_value.get("rid"));
		String questionInfo = "SELECT NICKNAME AS QUESTIONNICKNAME , USERNAME AS QUESTIONUSERNAME , PHOTO AS QUESTIONPHOTO,ROLETAG AS QUESTIONROLETAG FROM wm_user_info WHERE UID = ?";
		map.putAll(middlewareService.findFirst(questionInfo,map.get("questionuid")));
		return map;
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
	
	
	public String getLastid() {
		return lastid;
	}

	public void setLastid(String lastid) {
		this.lastid = lastid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getIsself() {
		return isself;
	}

	public void setIsself(String isself) {
		this.isself = isself;
	}


	public String getCondition() {
		return condition;
	}


	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}


	public String getLive() {
		return live;
	}


	public void setLive(String live) {
		this.live = live;
	}

	
}
