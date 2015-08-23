package com.wm927.interceptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.wm927.dao.MiddlewareDao;



/**
 * 定时器
 * @author chen
 * 在规定的时间点执行该方法
 * 已废弃
 */
public class Scheduler{
	private MiddlewareDao middlewareDao;
	private Logger logger = Logger.getLogger(Scheduler.class);
	
	/**
	 *每周更新的所有喊单数据 
	 */
	public void checkUpdateForWeek(){
		String checkUpdateSql = "SELECT UID FROM wm_bill_info GROUP BY UID";
		List<Map<String,Object>> list_value = middlewareDao.find(checkUpdateSql);
		if(list_value==null||list_value.isEmpty()){
			return;
		}
		for(Map<String,Object> map:list_value){
			updateOnceWeek(String.valueOf(map.get("uid")));
		}
	}
	
	
	/**
	 * 每日凌晨点定时更新数据
	 * @throws ParseException 
	 */
	public void updateDaylyReturn() throws ParseException{
		//更新当日收益
		String hdSumSql = " UPDATE wm_user_communityInfo SET DAYLYRETURN = 0 ";
		middlewareDao.update(hdSumSql);
		String selectAllUid = "SELECT UID FROM wm_bill_info GROUP BY UID";
		//查询第一次喊单时间
		String selectFirstCall = "SELECT MIN(OPENTIME) AS OPENTIME FROM wm_bill_info WHERE UID=? AND PRICETYPE = 1";
		//查询喊单总数
		String selectCallListSum = "SELECT HDSUM FROM wm_user_communityInfo WHERE UID=?";
		//更新日均喊单数量
		String updateDaylyCount = "UPDATE wm_user_communityInfo SET DAYAVGHDSUM = ? WHERE UID = ?";
		String firstCallTime = "";
		String hdSumCount = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Long time = null; 
		
		//日均喊单数
		String dayAvgHdSumCount = null;
		List<Map<String,Object>> id_list = middlewareDao.find(selectAllUid);
		for(Map<String,Object> id:id_list){
			firstCallTime = middlewareDao.findBy(selectFirstCall,"OPENTIME",id.get("uid"));
			//判断是否有平仓记录，没有则返回
			if("".equals(firstCallTime))
				continue;
			time = (System.currentTimeMillis() - sdf.parse(firstCallTime).getTime())/(24*3600*1000);
			time = time == 0 ? 1 :time;
			
			//判断喊单总数量是否为0，如果为零则没有进行过喊单交易，则后面的操作不用执行(数据库出现不存在记录问题，也不执行下面操作)
			hdSumCount = middlewareDao.findBy(selectCallListSum,"HDSUM",id.get("uid"));
			if(StringUtils.isEmpty(hdSumCount)||"0".equals(hdSumCount))
				continue;
			//日均喊单数
		    dayAvgHdSumCount = String.format("%.2f", (double)Long.parseLong(hdSumCount)/time);
			middlewareDao.update(updateDaylyCount,dayAvgHdSumCount,id.get("uid"));
		}
		
	}
	/**
	 * 每周更新一次的数据
	 */
	public void updateOnceWeek(String uid){
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(new Date());
		 //以当前月的前一个月1号开始算起，结束时间为本月的1号
		 String monthStartTime = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)-1)+"-01";
		 String dateEndTime = calendar.get(Calendar.YEAR)+"-"+calendar.get(Calendar.MONTH)+"-01";
		 
		//喊单总数量SQL
		String hdSumSql = " SELECT  HDSUM FROM wm_user_communityInfo WHERE UID = ?";
		String hdSum = middlewareDao.findBy(hdSumSql,"hdsum",uid);
		//判断喊单总数量是否为0，如果为零则没有进行过喊单交易，则后面的操作不用执行(数据库出现不存在记录问题，也不执行下面操作)
		if(StringUtils.isEmpty(hdSum)||"0".equals(hdSum))
			return;
		Long hdSumCount = Long.parseLong(hdSum);
		//月收益
		String monthlyReturnSql = "SELECT SUM(PROFIT) AS MONTHLYRETURN FROM wm_bill_info WHERE UID = ? AND PRICETYPE = 1 AND CLOSEOUTTIME BETWEEN '"+monthStartTime+" 00:00:00' AND ' "+dateEndTime+" 00:00:00'";
		String monthlyReturn = middlewareDao.findBy(monthlyReturnSql, "MONTHLYRETURN",uid);
		monthlyReturn = StringUtils.isEmpty(monthlyReturn)?"0.00":monthlyReturn;
		//最大持仓周期
		String maxMonthDaySql = " SELECT MAX(DATEDIFF(CLOSEOUTTIME,OPENTIME)) AS MAXMONTHDAY FROM wm_bill_info WHERE UID = ? AND PRICETYPE = 1";
		String maxMonthDay = middlewareDao.findBy(maxMonthDaySql, "MAXMONTHDAY",uid);
		//最小持仓周期
		String minMonthDaySql = " SELECT MIN(DATEDIFF(CLOSEOUTTIME,OPENTIME)) AS MINMONTHDAY FROM wm_bill_info WHERE UID = ? AND PRICETYPE = 1";
		String minMonthDay = middlewareDao.findBy(minMonthDaySql, "MINMONTHDAY",uid);
				
		
		//周交易数按周末为本周第一天,以上周末开始计算
	    calendar.setTime(new Date());
	    int dayweek = calendar.get(Calendar.DAY_OF_WEEK);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-dayweek);
		//calendar以1月返回0开始计算，所以10月返回9因此要加1
		String enddate = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DATE);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-dayweek-6);
		
		String begindate = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DATE);
		String weekMethodSql = "SELECT COUNT(*) AS WEEKMETHOD FROM wm_bill_info WHERE CLOSEOUTTIME BETWEEN '"+begindate+" 00:00:00' and '"+enddate+" 23:59:59' AND UID=? AND PRICETYPE = 1";
		Long weekMethodCount =  middlewareDao.findCount(weekMethodSql,uid);	
		
		//货币活跃度
		String currencys  = "";
		String checkcurrency = "SELECT `CODE` FROM wm_bill_info WHERE UID = ? AND PRICETYPE = 1 GROUP BY `CODE` ORDER BY COUNT(`CODE`) DESC LIMIT 3 ";
		List<Map<String,Object>> list_cur = middlewareDao.find(checkcurrency,uid);
		for(Map<String,Object> map :list_cur){
			currencys+=","+map.get("code");
		}
		currencys = StringUtils.isEmpty(currencys)?"":currencys.substring(1);
		//交易货币所占%
		String sql = "SELECT `CODE` ,COUNT(*) AS codecount FROM wm_bill_info bill " +
				"WHERE UID = ? AND PRICETYPE=1 GROUP BY `CODE` ORDER BY codecount DESC LIMIT 3";
		List<Map<String,Object>> list_value = middlewareDao.find(sql,uid);
		
		String currencysCount = "";
		int codeCount = 0;
		int currentCodeCount = 0;
		//拆分成%比形式发送给前端
		for(Map<String,Object> map :list_value){
			currentCodeCount = Integer.parseInt(String.valueOf(map.get("codecount")));
			codeCount +=  currentCodeCount;
			currencysCount += ","+map.get("code") + "="+String.format("%.2f", (double)currentCodeCount/hdSumCount*100);
		}
		//如果交易的货币超过三个以上，则其它的以其它形式显示在前端
		Long otherCodeCount = hdSumCount-codeCount ;
		if(otherCodeCount!=0){
			currencysCount += ",other="+String.format("%.2f", (double)otherCodeCount/hdSumCount*100);
		}
		currencysCount = StringUtils.isEmpty(currencysCount)?"":currencysCount.substring(1);
		
		String doUpdateSql = "UPDATE wm_user_communityInfo SET MONTHLYRETURN=?,MAXMONTHDAY=?,MINMONTHDAY=?,WEEKMETHOD=?,ACTIVECURRENCY=?,CURRENCYPERCENT=? WHERE UID=?";
		int count = middlewareDao.update(doUpdateSql,monthlyReturn,maxMonthDay,minMonthDay,weekMethodCount,currencys,currencysCount,uid);
		if(count == 0){
			logger.info("每周更新的喊单数据失败");
		}
	}
	
	
	public void setMiddlewareDao(MiddlewareDao middlewareDao) {
		this.middlewareDao = middlewareDao;
	}
	
	
}
