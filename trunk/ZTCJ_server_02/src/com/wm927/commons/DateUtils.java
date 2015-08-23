package com.wm927.commons;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 格式化日期公共包
 * @author chen
 *
 */
public class DateUtils{
	private static final String BEGIN_DATE="1970-01-01 08:00:00";
	public static int strToTime( String string, String format ) {
		if( string == null || string.length( ) == 0 ){
			return 0;
		}

		SimpleDateFormat sdf = getSimpleDateFormat( format );
		try{
			Date ndate = sdf.parse( string );
			return ( int ) ( ndate.getTime( ) / 1000 );
		}catch( Exception e ){
			e.printStackTrace( );
			return 0;
		}
	}

	public static String formatDate( Date date, String format ) {
		SimpleDateFormat dataFormat = getSimpleDateFormat( format );

		if( date == null ){
			date = new Date( );
		}
		return dataFormat.format( date );
	}
	
	public static Date parseDate( String date) throws ParseException {
		SimpleDateFormat dataFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dataFormat.parse(date);
	}
	
	/**
	 * 
	 * @param format
	 * @param timeoffset
	 * @return
	 */
	public static SimpleDateFormat getSimpleDateFormat( String format ) {
		SimpleDateFormat sdf = new SimpleDateFormat( format );
		return sdf;
	}

	public static String getCurrentDate( ) {
		return getCurrentTime( "yyyy-MM-dd" );
	}

	public static String getCurrentTime( String format ) {
		return formatDate( null, format );
	}

	public static String getCurrentTime( ) {
		return getCurrentTime( "yyyy-MM-dd HH:mm:ss" );
	}
	
	public static String formatDateByAddYear( Date d, int year ){
		Calendar c = Calendar.getInstance( );
		c.setTime( d );
		c.add( Calendar.YEAR, year );
		
		Date newDate = c.getTime( );
		
		return formatDate( newDate, "yyyy-MM-dd");
	}
	
	/**
	 * date日期转换时间戳
	 * @return
	 * @param 时间字符串
	 * @throws ParseException
	 */
	public static long getTimestamp(String date) throws ParseException{
		Date nowdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .parse(date);
		Date olddate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(BEGIN_DATE);
	    return nowdate.getTime() - olddate.getTime() > 0 ? 
	    		nowdate.getTime() - olddate.getTime() : olddate.getTime() - nowdate.getTime();
	}


	/**
	 * 时间戳日期转换date
	 * @return
	 * @param 时间戳
	 * @throws ParseException
	 */
	public static String formatTimestamp(Long time) throws ParseException{
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    return time.toString().length() == 10 ? date.format(new Date(time*1000)) : date.format(new Date(time));
	}
	
	/**
	 * 0代表一天，1代表一周，2代表一月，3代表三个月，4代表一年，5代表全部，默认5
	 * @param number
	 * @return yyyy-MM-dd
	 * @若返回为null则代表返回全部信息 
	 */
	public static String praseBeginDate(String number) {
		Long beginTime = System.currentTimeMillis()/1000;
		int type = DataUtils.praseNumber(number, 5);
		if(type == 5){
			return null;
		}
		Long dateCount = new Long(0);
		switch(type){
			case 0 :dateCount = new Long(24*60*60);
				break;
			case 1 :dateCount = new Long(7*24*60*60);
				break;
			case 2 :dateCount = new Long(30*24*60*60);
				break;
			case 3 :dateCount = new Long(90*24*60*60);
				break;
			case 4 :dateCount = new Long(365*24*60*60);
				break;
			default : dateCount = new Long(24*60*60);
				break;
		}
		beginTime = beginTime - dateCount ;
		String date = "";
		try {
			date = DateUtils.formatTimestamp(beginTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.substring(0,10);
	}
	/**
	 * 做时间计算
	 * 默认返回yyyy-MM-dd的时间格式
	 * @param time 以秒为单位传入时间(返回时间以秒为单位)
	 * @param type 0往前面+时间，1往历史推时间
	 * @param format 若为false则返回yyyy-MM-dd HH:mm:ss类型格式日期 否则返回yyyy-MM-dd格式日期 
	 * @return
	 */
	public static String dateCalculate(int time,int type,boolean format){
		String date = "";
		Long beginTime = System.currentTimeMillis()/1000;
		if(type == 0){
			beginTime = beginTime + time ;
		}else{
			beginTime = beginTime - time ;
		}
		try {
			date = DateUtils.formatTimestamp(beginTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(format){
			date = date.substring(0,10);
		}
		return date;
	}
	
	/**
	 * 计算时间差
	 * fDate =现在
	 * oDate =目标时间
	 */
	public static int daysOfTwo(Date fDate, Date oDate) {

	       Calendar aCalendar = Calendar.getInstance();

	       aCalendar.setTime(fDate);

	       int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);

	       aCalendar.setTime(oDate);

	       int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);

	       return day2 - day1;

	}
	
	
	public static void main( String [ ] args ) {
		
		 System.out.println(dateCalculate(60*60*24*365*10,0,false));
		/*Calendar c = Calendar.getInstance( );
		Date date = new Date( );
		c.setTime( date );
		
		c.add( Calendar.HOUR_OF_DAY, 1 );
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println( sdf.format(c.getTime( )) );*/
		
		
	}
	
}
