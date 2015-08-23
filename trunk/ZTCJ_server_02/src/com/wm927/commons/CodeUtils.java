package com.wm927.commons;

import java.util.Random;

public class CodeUtils {
	/**
	 * 生成6个不同数字随机验证码
	 * @return
	 */
	public static String createCode(){
		String msgCode= "";
		int[] array = {0,1,2,3,4,5,6,7,8,9};
		Random rand = new Random();
		for (int i = 10; i > 1; i--) {
		    int index = rand.nextInt(i);
		    int tmp = array[index];
		    array[index] = array[i - 1];
		    array[i - 1] = tmp;
		}
		int result = rand.nextInt(9)+1;
		for(int i = 1; i < 6; i++)
		    result = result * 10 + array[i];
		msgCode = String.valueOf(result);
		return msgCode;
	}
}
