package com.wm927.action.msg;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.wm927.commons.Contants;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 沙龙策略服务过期提醒
 * 有两种要提醒的，一种是还有7天到期提醒，另外一种是已经过期了发送通知提醒
 * @author chen
 *
 */
public class ExpiresAdvice extends MiddlewareActionService{
	public void sendAdvice(){
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run(){sevenDaysExpires();}
			}, 0, 60000);//1000*60*60*24
		timer.schedule(new TimerTask(){
			public void run(){alreadyExpires();}
			}, 0, 60000);//1000*60*60
	}
	/**
	 * 还有7天过期提醒
	 */
	public void sevenDaysExpires(){
		String sql = "SELECT ID,UID,ANAID,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS ADDTIME FROM wm_user_analyst_room WHERE SEVENDAYSEND=0  AND PASSTIME BETWEEN '" + DateUtils.dateCalculate(3600*24*6, 0, false) +"' AND '"+DateUtils.dateCalculate(3600*24*8, 0, false)+"'";
		//插入通知记录表
		String insertMsg = "INSERT INTO wm_user_advice (UID,CONTENT,ADDTIME,SENDID,SENDIP) VALUES(?,?,?,?,?)";
		//更新策略服务表的发出通知的状态
		String updateState = "UPDATE wm_user_analyst_room SET SEVENDAYSEND=1 WHERE ID = ?";
		String updateAdviceSum = "UPDATE wm_user_communityInfo SET NOTICECHANGENUMBER=NOTICECHANGENUMBER+1 WHERE UID=?";
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		String analystNickName = "SELECT NICKNAME FROM wm_user_info WHERE UID = ?";
		String nickname = "";
		int count = 0;
		String content = "尊敬的智通财经网用户，您在%s购买%s分析师的策略服务还有7天到期！！";
		for(Map<String,Object> map : list_value){
			nickname = middlewareService.findBy(analystNickName, "NICKNAME",map.get("anaid"));
			count = middlewareService.update(insertMsg,map.get("uid"),String.format(content, map.get("addtime"),nickname),DateUtils.getCurrentTime(),Contants.DEFAULT_OID,"127.0.0.1");
			if(count == 1){
				middlewareService.update(updateState,map.get("id"));
				middlewareService.update(updateAdviceSum,map.get("uid"));
			}
		}
	}
	/**
	 * 已经过期提醒
	 */
	public void alreadyExpires(){
		String sql = "SELECT ID,UID,ANAID,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS ADDTIME FROM wm_user_analyst_room WHERE EXPIRESSEND=0  AND PASSTIME BETWEEN '" + DateUtils.dateCalculate(3600, 1, false) +"' AND '"+DateUtils.dateCalculate(3600, 0, false)+"'";
		//插入通知记录表
		String insertMsg = "INSERT INTO wm_user_advice (UID,CONTENT,ADDTIME,SENDID,SENDIP) VALUES(?,?,?,?,?)";
		//更新策略服务表的发出通知的状态
		String updateState = "UPDATE wm_user_analyst_room SET EXPIRESSEND=1 WHERE ID = ?";
		//更新当前用户的的通知记录数+1
		String updateAdviceSum = "UPDATE wm_user_communityInfo SET NOTICECHANGENUMBER=NOTICECHANGENUMBER+1 WHERE UID=?";
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		String analystNickName = "SELECT NICKNAME FROM wm_user_info WHERE UID = ?";
		String nickname = "";
		int count = 0;
		String content = "尊敬的智通财经网用户，您在%s购买%s分析师的策略服务已经到期！！";
		for(Map<String,Object> map : list_value){
			nickname = middlewareService.findBy(analystNickName, "NICKNAME",map.get("anaid"));
			count = middlewareService.update(insertMsg,map.get("uid"),String.format(content, map.get("addtime"),nickname),DateUtils.getCurrentTime(),Contants.DEFAULT_OID,"127.0.0.1");
			if(count == 1){
				middlewareService.update(updateState,map.get("id"));
				middlewareService.update(updateAdviceSum,map.get("uid"));
			}
		}
	}
}
