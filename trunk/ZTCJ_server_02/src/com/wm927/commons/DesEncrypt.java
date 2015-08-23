package com.wm927.commons;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
 
/**   
 *     
 *   使用DES加密与解密,可对byte[],String类型进行加密与解密   
 *   密文可使用String,byte[]存储.   
 *     
 *   方法:   
 *   void   getKey(String   strKey)从strKey的字条生成一个Key   
 *     
 *   String   getEncString(String   strMing)对strMing进行加密,返回String密文   
 *   String   getDesString(String   strMi)对strMin进行解密,返回String明文   
 *     
 *byte[]   getEncCode(byte[]   byteS)byte[]型的加密   
 *byte[]   getDesCode(byte[]   byteD)byte[]型的解密   
*/   

public class DesEncrypt {
	
	Key   key;
	
	/**   
	    *   根据参数生成KEY   
	    *   @param   strKey
	    */
	
	public   void   getKey(String   strKey) {   
        try{   
            KeyGenerator   _generator   =   KeyGenerator.getInstance("DES");   
            _generator.init(new   SecureRandom(strKey.getBytes()));   
            this.key   =   _generator.generateKey();
            _generator=null;   
        }catch(Exception   e){   
            e.printStackTrace();   
        }   
    }  
	
	 /**   
	    *   加密String明文输入,String密文输出   
	    *   @param   strMing   
	    *   @return   
	    */   
	    public   String   getEncString(String   strMing) {   
	        byte[]   byteMi   =   null;   
	        byte[]   byteMing   =   null;   
	        String   strMi   =   "";    
	        try {    
	            return byte2hex(getEncCode (strMing.getBytes() ) );

//	            byteMing   =   strMing.getBytes("UTF8");   
//	            byteMi   =   this.getEncCode(byteMing);   
//	            strMi   =  new String( byteMi,"UTF8");
	        }   
	        catch(Exception   e){   
	            e.printStackTrace();   
	        }   
	        finally {   
	            byteMing   =   null;   
	            byteMi   =   null;   
	        }   
	        return   strMi;   
	    }   
	    /**   
	     *   解密   以String密文输入,String明文输出   
	     *   @param   strMi   
	     *   @return   
	     */   
	     public   String   getDesString(String   strMi)  {   
	         byte[]   byteMing   =   null;   
	         byte[]   byteMi   =   null;   
	         String   strMing   =   "";   
	         try  {   
	             return new String(getDesCode(hex2byte(strMi.getBytes()) ));   

//	             byteMing   =   this.getDesCode(byteMi);   
//	             strMing   =   new   String(byteMing,"UTF8");   
	         }   
	         catch(Exception   e) {   
	             e.printStackTrace();   
	         }   
	         finally {   
	             byteMing   =   null;   
	             byteMi   =   null;   
	         }   
	         return   strMing;   
	     }   

	     /**   
	      *   加密以byte[]明文输入,byte[]密文输出   
	      *   @param   byteS   
	      *   @return   
	      */   
	      private   byte[]   getEncCode(byte[]   byteS) {   
	          byte[]   byteFina   =   null;   
	          Cipher   cipher;   
	          try {   
	              cipher   =   Cipher.getInstance("DES");   
	              cipher.init(Cipher.ENCRYPT_MODE,   key);   
	              byteFina   =   cipher.doFinal(byteS);   
	          }   
	          catch(Exception   e) {   
	              e.printStackTrace();   
	          }   
	          finally {   
	              cipher   =   null;   
	          }   
	          return   byteFina;   
	      }   
	      /**   
	       *   解密以byte[]密文输入,以byte[]明文输出   
	       *   @param   byteD   
	       *   @return   
	       */   
	       private   byte[]   getDesCode(byte[]   byteD) {   
	           Cipher   cipher;   
	           byte[]   byteFina=null;   
	           try{   
	               cipher   =   Cipher.getInstance("DES");   
	               cipher.init(Cipher.DECRYPT_MODE,   key);   
	               byteFina   =   cipher.doFinal(byteD);   
	           }catch(Exception   e){   
	               e.printStackTrace();   
	           }finally{   
	               cipher=null;   
	           }   
	           return   byteFina;   
	       }  
	       /**  
	       * 二行制转字符串  
	       * @param b  
	       * @return  
	       */   
	           public static String byte2hex(byte[] b) {   //一个字节的数，
	               // 转成16进制字符串
	              String hs = "";   
	              String stmp = "";   
	              for (int n = 0; n < b.length; n++) {   
	                  //整数转成十六进制表示
	                  stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));  
	                  if (stmp.length() == 1)   
	                      hs = hs + "0" + stmp;   
	                  else   
	                      hs = hs + stmp;   
	              }   
	              return hs.toUpperCase();   //转成大写
	         }  
	           
	           public static byte[] hex2byte(byte[] b) {   
	        	      if((b.length%2)!=0)  
	        	         throw new IllegalArgumentException("长度不是偶数");   
	        	       byte[] b2 = new byte[b.length/2];   
	        	       for (int n = 0; n < b.length; n+=2) {   
	        	         String item = new String(b,n,2);   
	        	         // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
	        	         b2[n/2] = (byte)Integer.parseInt(item,16);   
	        	       }   
	        	   
	        	       return b2;   
	        	 }   
	           
	       	/**
	       	 * xyp(10.24)
	       	 * 对链接参数加密
	       	 */
//	       	String mima = "uid="+id+"&code="+vlidateNum+"&date="+currentTime;
	       	public static String Encryptparame(String parameter){
//	       		String mima = "uid="+id+"&code="+vlidateNum+"&date="+currentTime;
	       		DesEncrypt   des=new   DesEncrypt();
	       		des.getKey("aadd");//生成密匙
	       	 String   strEnc   =   des.getEncString(parameter);
	       	 return strEnc;
	       	}
	       	
	       	@Test
	       	public void test(){
	       		String mima = "uid="+12+"&code="+234567+"&date="+123456;
	       		String code = Encryptparame(mima);
	       		System.out.println(code);
	       	}

	           
	           public   static   void   main(String[]   args){
	        	  long millis = System.currentTimeMillis();
	        	  String a = "rrr";
	        	  String b = a+millis;
	        	  System.out.println(b);

	               System.out.println("hello");   
	               DesEncrypt   des=new   DesEncrypt();//实例化一个对像   
	               des.getKey("aaaaaaaa");//生成密匙   

	               String   strEnc   =   des.getEncString("云海飞舞雲122");//加密字符串,返回String的密文   
	               System.out.println("strEnc= "+strEnc);   

	               String   strDes   =   des.getDesString(strEnc);//把String   类型的密文解密   
	               System.out.println("strDes= "+strDes);   
	               new DesEncrypt();
	           }   
//	           F312E7E531C5DE29BF0AD4443EB7D354B6A08B8C88CEB20B
	           @Test
	           public void fae(){
	        	   String[] a  = {"a","b","c","d","e","f"};
	        	   String join = StringUtils.join(a, "");
	        	   System.out.println(join);
	           }
	           @Test
	           public void jfwe(){
	        	   String a = "swaioeroe";
	        	   String[] split = a.split("");
	        	   System.out.println(split);
	           }
	           
	           //将一个字符串的字符随机生成新的字符串
	           public String GetStrings(String s){
	        	   String[] split = s.split(",");
	        	   return null;
	           }
	       	@Test
	    	public void suiji(){
	    		StringBuffer sb = new StringBuffer();
	    		String[] a  = {"a","b","c","d","e","f"};
	    		int index;
	    		String b ="";
	    		for(int i = 0;i<a.length;i++){
	    			index =  new Random().nextInt(3);
	    			b=a[i];
	    			a[i] = a[index];
	    			a[index] = b;
	    		}
	    		for(int i = 0; i < a.length; i++){
	    			 sb. append(a[i]);
	    			}
	    			String s = sb.toString();
	    			System.out.println(s);
//	    		for(String c:a){
//	    			System.out.println(c);
//	    		}
	    	}

//	           14BE8F8A69FCAFE81E31F8B58B7F64A3798DD1400A0F2D8A
}
