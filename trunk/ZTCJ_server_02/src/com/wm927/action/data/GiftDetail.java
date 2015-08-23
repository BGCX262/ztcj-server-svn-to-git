package com.wm927.action.data;

import java.util.List;
import java.util.Map;

import com.wm927.service.impl.MiddlewareActionService;

/**
 * 根据传来礼物ID获取详细信息
 * @author chen
 *
 */
public class GiftDetail extends MiddlewareActionService{
	private String uid;
	private String giftid;
	private String cid;
	
	/**
	 * //策略、秘笈
	 */
	public void execute(){
		if(!checkNull(new Object[]{"秘笈ID不能为空"},new Object[]{cid}))
			return;
		String sql = "SELECT UID,CONTENT,ATTACHNAME ,CLASSID, PURVIEWID , IMGLIST,VIDEOURI,VIDEOIMG,VIDEOTITLE ,VIDEOLINK,VIDEOHOSTS, TITLE ,ATTACHMENT,ATTACHSIZE, PRICE ,EXPIRES,TIMELINESS,BUYNUMBER" +
						",DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME' FROM wm_blog_view WHERE ID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,cid);
		Map<String ,Object> mapInfo = null;
		for(Map<String,Object> map : list_value){
			mapInfo  = findUserInfo(map.get("uid"));
			map.putAll(mapInfo);
		}
		responseInfo("1","成功",list_value);
	}
	
	/**
	 * 获取礼物详情
	 */
	public void getGiftDetail(){
		if(!checkNull(new Object[]{"礼物ID不能为空"},new Object[]{giftid}))
			return;
		String  sql = "SELECT type.`NAME` AS GIFTCONTENT,type.GIFTIMG, detail.SENDID,detail.UID,DATE_FORMAT(detail.ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME'," +
				"detail.PRICE,detail.NUMBER FROM wm_user_gift_detail detail LEFT JOIN wm_user_gift_type type" +
				" ON type.ID=detail.GIFTID WHERE detail.ID = ? ";
		List<Map<String,Object>> list_value = middlewareService.find(sql,giftid);
		Map<String ,Object> sendMap = null;
		Map<String ,Object> recMap = null;
		for(Map<String,Object> map : list_value){
			recMap  = findUserInfo(map.get("uid"));
			sendMap  = findUserInfo(map.get("sendid"));
			map.put("sendusername", sendMap.get("username"));
			map.put("sendnickname", sendMap.get("nickname"));
			map.put("recusername", recMap.get("username"));
			map.put("recnickname", recMap.get("nickname"));
		}
		responseInfo("1","成功",list_value);
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getGiftid() {
		return giftid;
	}
	public void setGiftid(String giftid) {
		this.giftid = giftid;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
	
}
