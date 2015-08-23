package com.wm927.action.data;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import com.wm927.commons.Contants;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.Decoder;
import com.wm927.commons.MD5Utils;
import com.wm927.service.impl.MiddlewareActionService;

public class Glod extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(Glod.class);
	private String uid;
	private String amount;//交易金额
	private String date ;//查询时间
	private String detail;//交易详情
	private String orderid;//银行订单号
	private String transdate;//交易时间
	private String transtype;//交易类型
	private String status;//交易状态
	private	String page;
	private String key ;//MD5加密值
	private String number ;
	/**
	 * 我的金币与积分接口 
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		if(!checkUser(uid))
			return;
		String sql = "SELECT INTEGRAL , GOLD FROM wm_user_communityInfo WHERE UID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,uid);
		responseInfo("1","成功",list_value);
	}
	
	/**
	 * 添加金币接口
	 */
	public void addGlod(){
		if(!checkNull(new Object[]{"交易状态不能为空","交易时间不能为空","交易类型不能为空","订单号不能为空","充值的金币不能为空","用户ID不能为空"},
				new Object[]{status,transdate,transtype,orderid,amount,uid}))
			return ;
		/*if(!checkNull(new Object[]{"交易状态不能为空","交易时间不能为空","交易类型不能为空","订单号不能为空","充值的金币不能为空","用户ID不能为空","KEY不能为空"},
				new Object[]{status,transdate,transtype,orderid,amount,uid,key}))
			return ;
		String md5_value = MD5Utils.encrypt(uid,Contants.DEFAULT_MD5_KEY);
		if(!key.equals(md5_value)){
			responseInfo("-1","KEY不正确");
			return ; 
		}*/
		if(!checkUser(uid))
			return;
		
		int goldnumber2 = DataUtils.praseNumber(amount)/100;//支付宝以分为计算单位
		String transSql = "INSERT INTO wm_user_money_trans(UID,ORDERID,MONEY,STATUS,TRANSTYPE,TRANSDATE)VALUES(?,?,?,?,?,?)";
		int transCount = middlewareService.update(transSql,uid,orderid,goldnumber2,status,transtype,transdate);
		if(transCount == 0){
			responseInfo("-1","添加充值记录失败,并且未生成充值记录");
			return;
		}
		String sql = "UPDATE wm_user_communityInfo SET GOLD = GOLD+? WHERE UID = ?";
		int count = middlewareService.update(sql,goldnumber2,uid);
		if(count == 0){
			responseInfo("-1","添加金币失败");
			return;
		}
		//查询当前金币
		String findGold = "SELECT  GOLD FROM wm_user_communityInfo WHERE UID = ?";
		String oldGold = middlewareService.findBy(findGold,"GOLD", uid);
		//添加一条充值记录
		String insertSql = "INSERT INTO wm_user_money (UID,LAVE,TYPE,MONEY,DETAIL,INTEGRAL)VALUES(?,?,?,?,?,?)";
		int insertCount = middlewareService.update(insertSql,uid,DataUtils.praseNumber(oldGold),0,goldnumber2,"购买金币",0);
		if(insertCount == 0 ){
			logger.info("添加充值记录失败 ---->INSERT wm_user_money FALSE ");
		}
		responseInfo("1","充值金币成功");
	}
	
	/**
	 * 消费金币接口
	 */
	public void consumptionGlod(){
		if(!checkNull(new Object[]{"用户ID不能为空","消费的金币不能为空"},new Object[]{uid,amount}))
			return ; 
		if(!checkUser(uid))
			return;
		int goldnumber2 = DataUtils.praseNumber(amount);
		int integeral = goldnumber2 * 10;
		String checkGold = "SELECT COUNT(*) FROM wm_user_communityInfo WHERE GOLD >= ? AND UID = ?";
		Long checkCount = middlewareService.findCount(checkGold,goldnumber2,uid);
		if(checkCount == 0){
			responseInfo("-1","金币余额不足，请充值");
			return;
		}
		
		String sql = "UPDATE wm_user_communityInfo SET GOLD = GOLD-?, INTEGRAL = INTEGRAL + ? WHERE UID = ?";
		int count = middlewareService.update(sql,goldnumber2,integeral,uid);
		if(count == 0){
			responseInfo("-1","消费金币失败");
			return;
		}
		//查询当前金币
		String findGold = "SELECT  GOLD FROM wm_user_communityInfo WHERE UID = ?";
		String oldGold = middlewareService.findBy(findGold,"GOLD", uid);
		//添加一条消费记录
		String insertSql = "INSERT INTO wm_user_money (UID,LAVE,TYPE,MONEY,DETAIL,INTEGRAL)VALUES(?,?,?,?,?,?)";
		int insertCount = middlewareService.update(insertSql,uid,DataUtils.praseNumber(oldGold),1,goldnumber2,detail,integeral);
		if(insertCount == 0){
			logger.info("添加消费记录失败 --> INSERT INTO wm_user_money FALSE");
		}
		responseInfo("1","消费金币成功");
	}
	
	/**
	 * 交易明细接口(我的账户)
	 * @throws ParseException 
	 */
	public void consumptionDetail() throws ParseException{
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return ;
		}
		if(!checkUser(uid))
			return;
		int type2 = DataUtils.praseNumber(transtype,2);
		String dtranstype = DateUtils.praseBeginDate(date);
		String dateStr = "";
		if(!DataUtils.checkString(dtranstype)){
			dateStr = " AND ADDTIME BETWEEN '" + dtranstype + " 00:00:00' AND ";
			dateStr += "'"+DateUtils.getCurrentDate()+" 23:59:59'";
		}
		String typeStr = "";
		if(type2 != 2){
			typeStr = " AND TYPE = " + type2;
		}
		String sql = "SELECT ID,LAVE,GIFTID,GIFTTYPE,TYPE,MONEY,DETAIL,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME',INTEGRAL FROM wm_user_money WHERE UID =  "+uid+dateStr +typeStr +" ORDER BY ID DESC";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		for(Map<String,Object> map : list_value){
			map.put("detail", returnGiftName(map));
		}
		Map<String,Object> mapInfo = findPageSize(page, number, sql);
		responseInfo("1","成功",list_value,mapInfo);
	}
	
	/**
	 * 礼物分为5中，所以需要根据TYPE去拿对应的具体礼物名称
	 * @param type
	 * @param id
	 * @return
	 */
	private Object returnGiftName(Map<String,Object> map){
		Object name = "";
		int ttype = DataUtils.praseNumber(map.get("gifttype")+"", 0);
		String sql = "SELECT NAME FROM wm_user_gift_type WHERE ID = ? ";
		String sql1 = "SELECT TITLE FROM wm_blog_view WHERE ID = ? ";
		Object id = map.get("giftid");
		switch(ttype){
		case 0 : name = map.get("detail");
			break;
		case 1 : 
			name = "好评" ;
			break;
		case 2 : 
			name = middlewareService.findBy(sql, "NAME",id) ;
			break;
		case 3 : 
			name = middlewareService.findBy(sql1, "TITLE",id) ;
			break;
		case 4 : 
			name = middlewareService.findBy(sql1, "TITLE",id) ;
			break;
		case 5 : 
			name = "策略服务";
			break;
		default : name = "好评";
			break;
		}
		return name;
	}
	/**
	 * 根据订单号获取交易明细
	 */
	public void findConsumptionDetailOrder(){
		if(DataUtils.checkString(orderid)){
			responseInfo("-1","订单号不能为空");
			return;
		}
		String sql = "SELECT UID,MONEY,STATUS,TRANSTYPE,TRANSDATE FROM wm_user_money_trans WHERE ORDERID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,orderid);
		responseInfo("1","成功",list_value);
	}
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}


	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}


	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		if(!DataUtils.checkString(detail))
			detail = Decoder.decode(detail);
		this.detail = detail;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getTransdate() {
		return transdate;
	}

	public void setTransdate(String transdate) {
		this.transdate = transdate;
	}

	public String getTranstype() {
		return transtype;
	}

	public void setTranstype(String transtype) {
		this.transtype = transtype;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
}
