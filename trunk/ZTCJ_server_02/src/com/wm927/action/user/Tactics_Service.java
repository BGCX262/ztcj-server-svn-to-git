package com.wm927.action.user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 策略服务
 * @author 郭瑜嘉
 * 2014/3/11
 */
public class Tactics_Service extends MiddlewareActionService{
	private static final Logger logger = Logger.getLogger(Tactics_Service.class);
	private String uid;//用户ID
	private String aid;//分析师ID
	
	public void invoke(){
		if(uid == null || uid.equals("")){
			responseInfo("-1", "uid不能为空");
			return;
		}
		if(aid == null || aid.equals("")){
			responseInfo("-1", "aid不能为空");
			return;
		}
		String sql = "SELECT * FROM wm_user_analyst_room WHERE UID = ? AND ANAID = ?";
		List<Map<String,Object>> map = middlewareService.find(sql,uid,aid);
		try {
			if(map != null && map.size() > 0){
				Date now = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String curTime = sdf.format(now);
				Date passTime = sdf.parse(map.get(0).get("passtime").toString());
				int remain = DateUtils.daysOfTwo(now, passTime);
				if(remain < 0 && remain < -2 && remain > -8){//最后的7-2天内发提醒，剩下最后2天只返回登记信息
					String insert = "INSERT INTO wm_user_advice (UID,CONTENT,ADDTIME,SENDID,SENDIP,TYPE)VALUES(?,?,?,?,?,?)";
					int result = middlewareService.update(insert,uid,"您的策略服务还有"+(remain * -1)+"天到期!",curTime,1352,"127.0.0.1",1);
					int rst = 0;
					if(result > 0){
						String upd = "UPDATE wm_user_communityInfo SET NOTICECHANGENUMBER = NOTICECHANGENUMBER+1 WHERE UID = ?";
						rst = middlewareService.update(upd,uid);
					}
					if(result > 0 && rst > 0){
						responseInfo("1", "策略将在1星期内过期，已发送提醒", new ArrayList<Map<String,Object>>());
						return;
					}else{
						responseInfo("-1", "策略查询失败，在mysql插入提醒时失败", new ArrayList<Map<String,Object>>());
						return;
					}
				}else if(remain < 0 && remain >= -2){
					responseInfo("1", "策略2天内过期", map);
					return;
				}else{
					responseInfo("1", "策略已过期", new ArrayList<Map<String,Object>>());
					return;
				}
			}else{
				responseInfo("1", "不存在策略", new ArrayList<Map<String,Object>>());
				return;
			}
		} catch (ParseException e) {
			logger.error("策略查询服务,日期转换出错。",e);
			//e.printStackTrace();
		}
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getAid() {
		return aid;
	}

	public void setAid(String aid) {
		this.aid = aid;
	}
}
