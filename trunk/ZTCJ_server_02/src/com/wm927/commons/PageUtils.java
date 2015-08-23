package com.wm927.commons;

/**
 * 分页
 * @author chen
 *
 */
public class PageUtils {
	private final static int page = 1;//当前页
	private final static int pageSize = 5;//每页记录数
	private final static int totalCount = 0;//总记录数
	private final static int totalPage = 0;//总页数
	
	
	/**
	 * 获取当前页(若传入参数""或者null，或者不是正常的int类型数据，则返回1)
	 * @param p
	 * @return
	 */
	public static int getPage(String p){
		try{
			return Integer.parseInt(p);
		}catch(Exception e){
			return page;
		}
	}
	/**
	 * 获取每页记录数(若传入参数""或者null，或者不是正常的int类型数据，则返回5)
	 * @return
	 */
	public static int getPageCount(String pc){
		try{
			return Integer.parseInt(pc);
		}catch(Exception e){
			return pageSize;
		}
	}
	/**
	 * 获取总记录数
	 * @return
	 */
	public static int getTotalCount(long tc){
		return tc >= 0? (int) tc:totalCount;
		
	}
	/**
	 * 获取总记录数
	 * @return
	 */
	public static int getTotalCount(String tc){
		try{
			return Integer.parseInt(tc);
		}catch(Exception e){
			return totalCount;
		}
		
	}
	/**
	 * 获取总页数
	 * @param tcount 总记录数
	 * @param pcount 每页记录数
	 * @return
	 */
	public static int getTotalPage(long tcount,String pcount){
		try{
			return getTotalPage(tcount,Integer.parseInt(pcount));
		}catch(Exception e){
			return execute(tcount,pageSize);
		}
		
		
	}
	
	/**
	 * 获取总页数
	 * @param tcount 总记录数
	 * @param pcount 每页记录数
	 * @return
	 */
	public static int getTotalPage(long tcount,int pcount){
		try{
			return execute(tcount,pcount);
		}catch(Exception e){
			return execute(tcount,pageSize);
		}
		
		
	}
	
	/**
	 * 获取总页数
	 * @param tcount 总记录数
	 * @param pcount 每页记录数
	 * @return
	 */
	public static int getTotalPage(String tcount,String pcount){
		try{
			return getTotalPage(Integer.parseInt(tcount),pcount);
		}catch(Exception e){
			return totalPage;
		}
	}
	private static int execute(long tcount,int pcount ){
		if(tcount<=0){
			return  totalCount;
		}
		if(pcount<=0){
			pcount = pageSize;
		}
		int tpage = (int)(tcount%pcount==0?tcount/pcount:tcount/pcount+1);
		return tpage;
	}
}
