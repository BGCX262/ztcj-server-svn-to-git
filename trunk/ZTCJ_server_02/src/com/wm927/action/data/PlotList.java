package com.wm927.action.data;

import java.util.List;
import java.util.Map;

import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 显示策略和秘笈
 * @author chen
 *
 */
public class PlotList extends MiddlewareActionService{
	private String uid;
	private String ouid;
	private String page;
	private String number;
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","被索引的用户ID不能为空"},new Object[]{uid,ouid}))
			return;
		String sql = "SELECT ID,ATTACHNAME,UID,CONTENT,CLASSID,BUYNUMBER,IMGLIST,PURVIEWID,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS ADDTIME ," +
				" ATTACHMENT,ATTACHSIZE,VIDEOTITLE,VIDEOLINK,VIDEOHOSTS,VIDEOIMG,VIDEOURI,TITLE,PRICE,EXPIRES,TIMELINESS" +
				" FROM wm_blog_view WHERE UID = "+ouid+" AND ISDELETE=0 AND CLASSID!=0 AND IF(LENGTH(EXPIRES)>0,EXPIRES >'"+DateUtils.getCurrentTime()+"', 1=1) ORDER BY ID DESC";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		for(Map<String,Object> map : list_value){
			map.put("open", checkOpen(map));
			map.putAll(findUserInfo(map.get("uid")));
		}
		Map<String,Object> mapInfo = findPageSize(sql, number, sql);
		responseInfo("1","成功",list_value,mapInfo);
	}
	/**
	 * 0显示，1隐藏
	 * @param type /0分析，1策略，2秘笈
	 * @return
	 */
	private Object checkOpen(Map<String,Object> map){
		String open = "0";
		String attention ;
		Object type = map.get("classid");
		Object purview = map.get("purviewid");
		Object blogid = map.get("id");
		if(!uid.equals(ouid)){//不是自己的秘笈
			if("0".equals(type)){
				if("1".equals(purview)){//对关注的人查看
					//当前发布分析的人未关注当前观看此分析用户
					attention = attentionState(uid, ouid);
					if("0".equals(attention) || "1".equals(attention)){
						open = "1";
					}
				}
			}else 
				
				if ("1".equals(type)){
				if("1".equals(purview)){
					attention = attentionState(uid, ouid);
					//当前发布策略的人未关注当前发布策略用户
					if("0".equals(attention) || "1".equals(attention)){
						//是否购买了发布分析人的策略服务
						if(!checkByCircleService(uid, ouid)){
							//判断是否购买了单条策略
							if(!checkByCircle(uid, ouid, blogid)){
								open = "1";
							}
						}
					}
				//对策略进行单价出售
				}else{
					//按照价格出售
					//是否购买了发布分析人的策略服务
					if(!checkByCircleService(uid, ouid)){
						//判断是否购买了单条策略
						if(!checkByCircle(uid, ouid, blogid)){
							open = "1";
						}
					}
				}
			}else{
				
				if("1".equals(purview)){
					attention = attentionState(uid, ouid);
					//当前发布秘笈的人未关注当前观看此秘笈用户
					if("0".equals(attention) || "1".equals(attention)){
						//判断是否购买了单条秘籍
						if(!checkByCircle(uid, ouid, blogid)){
							open = "1";
						}
					}
				//对策略进行单价出售
				}else{
					//按照价格出售
					//判断是否购买了单条策略
					if(!checkByCircle(uid, ouid, blogid)){
						open = "1";
					}
				}
			}
		}
		return open ; 
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
	
}
