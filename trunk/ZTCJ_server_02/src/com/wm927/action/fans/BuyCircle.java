package com.wm927.action.fans;


import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 购买服务、策略、秘笈
 * @author chen
 *
 */
public class BuyCircle extends MiddlewareActionService{
	Logger logger = Logger.getLogger(BuyCircle.class);
	private String uid;
	private String ouid;
	private String cid;
	private String price;
	private String type;
	
	/**
	 * 购买策略服务
	 * 1，检查金币是否够
	 * 2，插入策略服务记录
	 * 3，插入礼物详细表
	 * 4，插入动态
	 * 5，减少金币+积分
	 * @throws ParseException 
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","发布策略ID不能为空","价格不能为空"},new Object[]{uid,ouid,price}))
			return;
		int money = DataUtils.praseNumber(price, 1);
		int integeral = money * 10;
		
		//查询金币是否够
		String checkMoney = "SELECT COUNT(*) FROM wm_user_communityInfo WHERE UID = ? AND GOLD > ?";
		Long laveMoney = middlewareService.findCount(checkMoney,uid,money);
		if(laveMoney == 0){
			responseInfo("-1","金币不够");
			return ;
		}
		//插入策略服务表,先判断是否已经存在记录，若存在记录则更新过期时间即可
		String expDate = DateUtils.dateCalculate(60*60*24*30, 0,false);
		String cirSql = "INSERT INTO wm_user_analyst_room (PASSTIME,ADDIP,UID,ANAID)VALUES(?,?,?,?)";
		String checkExpDate = "SELECT COUNT(*) FROM wm_user_analyst_room WHERE UID = ? AND ANAID = ?";
		Long expCount = middlewareService.findCount(checkExpDate,uid,ouid);
		if(expCount > 0){
			cirSql = "UPDATE wm_user_analyst_room SET PASSTIME = ? ,ADDIP = ? WHERE UID = ? AND ANAID = ?";
		}
		int cirCount = middlewareService.update(cirSql,expDate,getIpAddr(),uid,ouid);
		if(cirCount == 0){
			responseInfo("-1","购买策略失败");
			return;
		}
		String addtime = DateUtils.getCurrentTime();
		//插入礼物详细表//策略服务没有id
		String sql = "INSERT INTO wm_user_gift_detail (UID,GIFTID,TYPE,SENDID,NUMBER,PRICE,MONEY,ADDTIME)VALUES(?,?,?,?,?,?,?,?)";
		int count = middlewareService.update(sql,ouid,null,5,uid,1,money,money,addtime);
		if(count == 0){
			logger.info("购买策略成功，插入礼物表失败");
		}
				
		//减少金币+积分
		String deleteMoney = "UPDATE wm_user_communityInfo SET GOLD = GOLD-? , INTEGRAL=INTEGRAL+? WHERE UID = ?";
		//增加被赠送者人的金币数量
		String addMoney = "UPDATE wm_user_communityInfo SET GOLD = GOLD+?  WHERE UID = ?";
		int delMoney = middlewareService.update(deleteMoney,money,integeral,uid);
		int aMoney = middlewareService.update(addMoney,money,ouid);
		if(delMoney == 0){
			logger.info("购买策略服务减少金币失败");
		}
		if(aMoney == 0){
			logger.info("被购买策略服务增加金币失败");
		}
		//插入我的账户表
		//添加一条消费记录
		//查询当前金币
		String findGold = "SELECT  GOLD FROM wm_user_communityInfo WHERE UID = ?";
		String oldGold = middlewareService.findBy(findGold,"GOLD", uid);
		String insertSql = "INSERT INTO wm_user_money (UID,LAVE,TYPE,GIFTTYPE,GIFTID ,MONEY,DETAIL,INTEGRAL)VALUES(?,?,?,?,?,?,?,?)";
		int insertCount = middlewareService.update(insertSql,uid,DataUtils.praseNumber(oldGold),1,type,"",money,"购买策略服务",integeral);
		if(insertCount == 0){
			logger.info("购买策略服务添加消费记录失败 --> INSERT INTO wm_user_money FALSE");
		}
		
		String findGiftIdSql = "SELECT ID FROM wm_user_gift_detail WHERE UID = ? AND SENDID=? AND  ADDTIME = ? LIMIT 1";
		String findGiftId = middlewareService.findBy(findGiftIdSql,"ID",ouid,uid ,addtime);
		insertDynamic(uid,ouid,findGiftId,7,"插入购买策略服务动态");
		responseInfo("1","购买策略成功");
	}
	
	/**
	 * 购买策略与秘笈
	 * 1，检查金币是否够
	 * 2，插入策略或者秘笈记录
	 * 3，插入礼物详细表
	 * 4，插入动态
	 * 5，减少金币+积分
	 */
	public void buyPlot(){
		if(!checkNull(new Object[]{"用户ID不能为空","发布策略人ID不能为空","价格不能为空","策略ID不能为空","策略类型不能为空"},new Object[]{uid,ouid,price,cid,type}))
			return;
		int money = DataUtils.praseNumber(price, 1);
		int integeral = money * 10;
		
		//查询金币是否够
		String checkMoney = "SELECT COUNT(*) FROM wm_user_communityInfo WHERE UID = ? AND GOLD >= ?";
		Long laveMoney = middlewareService.findCount(checkMoney,uid,money);
		if(laveMoney == 0){
			responseInfo("-1","金币不够");
			return ;
		}
		String cirSql = "INSERT INTO wm_user_analyst_circle_sell (CID,ADDIP,UID,ANAID)VALUES(?,?,?,?)";
		int cirCount = middlewareService.update(cirSql,cid,getIpAddr(),uid,ouid);
		if(cirCount == 0){
			responseInfo("-1","购买策略失败");
			return;
		}
		//将当前策略购买数量+1
		String updatePlotSql = "UPDATE wm_blog_view SET BUYNUMBER=BUYNUMBER+1 WHERE ID = ?";
		int plotCount = middlewareService.update(updatePlotSql,cid);
		if(plotCount == 0 ){
			logger.info("策略购买数量+1失败");
		}
		String addtime = DateUtils.getCurrentTime();
		//插入礼物详细表
		String sql = "INSERT INTO wm_user_gift_detail (UID,GIFTID,TYPE,SENDID,NUMBER,PRICE,MONEY,ADDTIME)VALUES(?,?,?,?,?,?,?,?)";
		int count = middlewareService.update(sql,ouid,cid,type,uid,1,money,money,addtime);
		if(count == 0){
			logger.info("购买策略成功，插入礼物表失败");
		}
		//插入我的账户表
		//添加一条消费记录
		//查询当前金币
		String findGold = "SELECT  GOLD FROM wm_user_communityInfo WHERE UID = ?";
		String oldGold = middlewareService.findBy(findGold,"GOLD", uid);
		String insertSql = "INSERT INTO wm_user_money (UID,LAVE,TYPE,GIFTTYPE,GIFTID ,MONEY,DETAIL,INTEGRAL)VALUES(?,?,?,?,?,?,?,?)";
		int insertCount = middlewareService.update(insertSql,uid,DataUtils.praseNumber(oldGold),1,type,cid,money,"",integeral);
		if(insertCount == 0){
			logger.info("购买策略添加消费记录失败 --> INSERT INTO wm_user_money FALSE");
		}		
		//减少金币+积分
		String deleteMoney = "UPDATE wm_user_communityInfo SET GOLD = GOLD-? , INTEGRAL=INTEGRAL+? WHERE UID = ?";
		//增加被赠送者人的金币数量
		String addMoney = "UPDATE wm_user_communityInfo SET GOLD = GOLD+?  WHERE UID = ?";
		int delMoney = middlewareService.update(deleteMoney,money,integeral,uid);
		int aMoney = middlewareService.update(addMoney,money,ouid);
		if(delMoney == 0){
			logger.info("购买策略减少金币失败");
		}
		if(aMoney == 0){
			logger.info("被购买策略金币失败");
		}
		insertDynamic(uid,ouid,insertCount,7,"插入购买策略服务动态");
		responseInfo("1","购买策略成功");
	}
	
	/**
	 * 显示当前用户是否购买了分析师的策略服务
	 */
	public void isBuyPlot(){
		if(!checkNull(new Object[]{"用户ID不能为空","分析师ID不能为空"},new Object[]{uid,ouid}))
			return;
		String sql = "SELECT DATE_FORMAT(PASSTIME ,'%Y-%m-%d %H:%i:%s') AS 'PASSTIME' " +
				"     FROM wm_user_analyst_room WHERE ANAID=? AND PASSTIME > ? AND UID="+uid  ;
		
		if(uid.equals(ouid)){
			//表示分析师自己查看自己，返回有多少人购买
			sql = "SELECT COUNT(*) AS COUNT FROM wm_user_analyst_room WHERE ANAID=? AND PASSTIME > ?";
		}
		
		List<Map<String,Object>> list_value = middlewareService.find(sql,ouid,DateUtils.getCurrentTime());
		responseInfo("1","成功",list_value);
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
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
