package com.wm927.commons;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.util.StringUtils;

/**
 * 获取session，获取httpservetrequest公共包
 * @author chen
 *
 */
public final class RequestUtils{

	private RequestUtils( ) {

	}

	public static HttpSession getSession( HttpServletRequest request ) {

		HttpSession session = request.getSession( false );

		if( session == null ){
			session = request.getSession( true );
		}

		return session;
	}

	@SuppressWarnings( "unchecked" )
	public static < T > T getEntityInSession( String key, HttpSession session ) {
		return ( T  ) session.getAttribute( key );
	}
	
	
	/**
	 * 获取访问者IP
	 * 
	 * 在一般情况下使用Request.getRemoteAddr()即可，但是经过nginx等反向代理软件后，这个方法会失效。
	 * 
	 * 本方法先从Header中获取X-Real-IP，如果不存在再从X-Forwarded-For获得第一个IP(用,分割)，
	 * 如果还不存在则调用Request .getRemoteAddr()。
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}
		ip = request.getHeader("X-Forwarded-For");
		if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个IP值，第一个为真实IP。
			int index = ip.indexOf(',');
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		} else {
			return request.getRemoteAddr();
		}
	}
}
