package com.wm927.action.fans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

public class GiftList extends MiddlewareActionService{
	Logger logger = Logger.getLogger(GiftList.class);
	private String page;
	private String number;
	private String type ;
	private String uid;
	private String ouid;
	private String price;
	private String giftid;
	/**
	 * 礼物动态列表(包括好评，策略服务，策略，秘笈，礼物等)
	 * 这里的礼物类型因为在不同的表(分为策略服务，好评，鲜花等)
	 * 显示出来都为名字，所以需要进行判断查询
	 */
	public void execute(){
		String sql = "SELECT ID,UID , GIFTID, TYPE,SENDID,NUMBER,PRICE,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME'" +
				"    FROM wm_user_gift_detail detail ORDER BY ID DESC" ;
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);;
		Map<String,Object> uid_info = null;//赠送礼物人的信息
		Map<String,Object> fid_info = null;//接收礼物人的信息
		for(Map<String,Object> map : list_value){
			uid_info = findUserInfo(map.get("sendid"));
			fid_info = findUserInfo(map.get("uid"));
			map.putAll(giftName(map.get("type"),map.get("giftid")));
			map.put("recusername", fid_info.get("username"));
			map.put("recnickname", fid_info.get("nickname"));
			map.put("sendusername", uid_info.get("username"));
			map.put("sendnickname", uid_info.get("nickname"));
		}
		Map<String,Object> map_info = findPageSize(page, number, sql);
		responseInfo("1","成功",list_value,map_info);
	}

	/**
	 * 礼物列表
	 * 这里主要是显示要赠送的礼物(包括鲜花，中华烟等，，好评、策略等不包括在内)
	 */
	public void giftList(){
		String sql = "SELECT ID,NAME,PRICE, GIFTIMG FROM wm_user_gift_type ORDER BY SORT DESC";
		responseInfo(page,number,"1","成功",sql);
	}
	
	/**
	 * 送礼物接口
	 * 这里需要送礼物人的UID与被赠送者UID，
	 * 礼物的类型TYPE以及礼物的ID(这里注意，好评的ID可以为空)
	 * 1,先检查金币是否够买礼物
	 * 2，将数据插入礼物详细表
	 * 3，减少用户的金币，+用户的积分
	 * 4，插入动态
	 */
	public void sendGift(){
		if(!checkNull(new Object[]{"用户ID不能为空","被赠送者ID不能为空","礼物类型不能为空","价格不能为空"},new Object[]{uid,ouid,type,price}))
			return;
		int money = DataUtils.praseNumber(price,1)*DataUtils.praseNumber(number,1);
		int integeral = money * 10;
		//查询金币是否够
		String checkMoney = "SELECT COUNT(*) FROM wm_user_communityInfo WHERE UID = ? AND GOLD >= ?";
		Long laveMoney = middlewareService.findCount(checkMoney,uid,money);
		if(laveMoney == 0){
			responseInfo("-1","金币不够");
			return ;
		}
		String addtime = DateUtils.getCurrentTime();
		//插入礼物详细表
		String sql = "INSERT INTO wm_user_gift_detail (UID,GIFTID,TYPE,SENDID,NUMBER,PRICE,MONEY,ADDTIME)VALUES(?,?,?,?,?,?,?,?)";
		int insertGiftCount = middlewareService.update(sql,ouid,giftid,type,uid,number,price,money,addtime);
		if(insertGiftCount == 0){
			responseInfo("-1","礼物赠送失败");
			return;
		}
		
		//减少金币+积分
		String deleteMoney = "UPDATE wm_user_communityInfo SET GOLD = GOLD-? , INTEGRAL=INTEGRAL+? WHERE UID = ?";
		//增加被赠送者人的金币数量
		String addMoney = "UPDATE wm_user_communityInfo SET GOLD = GOLD+?  WHERE UID = ?";
		int delMoney = middlewareService.update(deleteMoney,money,integeral,uid);
		int aMoney = middlewareService.update(addMoney,money,ouid);
		if(delMoney == 0){
			logger.info("赠送礼物减少金币失败");
		}
		if(aMoney == 0){
			logger.info("被赠送礼物增加金币失败");
		}
		//插入我的账户表
		//添加一条消费记录
		//查询当前金币
		String findGold = "SELECT  GOLD FROM wm_user_communityInfo WHERE UID = ?";
		String oldGold = middlewareService.findBy(findGold,"GOLD", uid);
		String insertSql = "INSERT INTO wm_user_money (UID,LAVE,TYPE,GIFTTYPE,GIFTID ,MONEY,DETAIL,INTEGRAL)VALUES(?,?,?,?,?,?,?,?)";
		int insertCount = middlewareService.update(insertSql,uid,DataUtils.praseNumber(oldGold),1,type,giftid,money,"",integeral);
		if(insertCount == 0){
			logger.info("赠送礼物添加消费记录失败 --> INSERT INTO wm_user_money FALSE");
		}
		
		insertDynamic(uid,ouid,insertGiftCount,7,"插入礼物动态");
		responseInfo("1","赠送礼物成功");
	}
	
	/**
	 * 礼物分为5中，所以需要根据TYPE去拿对应的具体礼物名称
	 * @param type
	 * @param id
	 * @return
	 */
	private Map<String,Object> giftName(Object type ,Object id){
		int ttype = DataUtils.praseNumber(type+"", 1);
		String sql = "SELECT NAME AS GIFTCONTENT,GIFTIMG FROM wm_user_gift_type WHERE ID = ? ";//获取详细礼物
		String sql1 = "SELECT TITLE FROM wm_blog_view WHERE ID = ? ";//获取策略标题
		String sql2 = "SELECT NAME AS GIFTCONTENT,GIFTIMG FROM wm_user_gift WHERE ID=? ";//获取礼物类型数据
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> map_value = middlewareService.findFirst(sql2,ttype);
		switch(ttype){
		case 2 : 
			map_value = middlewareService.findFirst(sql, id) ;
			break;
		case 3 : 
			map = middlewareService.findFirst(sql1, id) ;
			map_value.putAll(map);
			break;
		case 4 : 
			map = middlewareService.findFirst(sql1, id) ;
			map_value.putAll(map);
			break;
		default : 
			break;
		}
		return map_value;
		
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

	public String getOuid() {
		return ouid;
	}

	public void setOuid(String ouid) {
		this.ouid = ouid;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}


	public String getGiftid() {
		return giftid;
	}

	public void setGiftid(String giftid) {
		this.giftid = giftid;
	}
	
}
