package com.wm927.commons;

import java.security.MessageDigest;

public class MD5Utils {
	 private static byte[] encryptMD5(byte[] data) throws Exception {
	        MessageDigest md5 = MessageDigest.getInstance("MD5");
	        md5.update(data);
	        return md5.digest();
	    }
	 private static String toHex(byte input[])
	    {
	        if(input == null)
	            return null;
	        StringBuffer output = new StringBuffer(input.length * 2);
	        for(int i = 0; i < input.length; i++)
	        {
	            int current = input[i] & 0xff;
	            if(current < 16)
	                output.append("0");
	            output.append(Integer.toString(current, 16));
	        }

	        return output.toString();
	    }
	 /**
	  * 登录时候调用的MD5加密
	  * @param str
	  * @return
	  */
	   public static String encrypt(String str){
		   return encrypt(str,Contants.DEFAULT_MD5_LOGIN_KEY);
	   }
	   public static String encrypt(String str,String key){
		   String word = "";
		   try {
			   word=  toHex(encryptMD5((str+key).getBytes()));
		} catch (Exception e) {
			word = "!!!!!!!!!!!!!!加密失败!!!!!!!!!!!!";
		}
		   return word.substring(8,24);
	   }
}
