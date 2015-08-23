package com.wm927.action.validate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.wm927.commons.CodeWordString;
import com.wm927.commons.DES;

/**
 * 发送邮件
 * @author chen
 *
 */
public class SpringtoMail{
	private Logger logger = Logger.getLogger(SpringtoMail.class);
	private JavaMailSenderImpl mailSender;
	public void setMailSender(JavaMailSenderImpl mailSender) {
		this.mailSender = mailSender;
	}
	
	public  boolean send(String id,String vlidateNum,long currentTime,String tomail,String type) {
		MimeMessage mailMessage = mailSender.createMimeMessage();
		 //设置utf-8或GBK编码，否则邮件会有乱码
   	  	MimeMessageHelper messageHelper = null;
   	  	boolean flag = true;
   	  	//激活邮箱
   	  	String href = "http://account.wm927.com/service/do?mod=active";
   	  	String msg = "激活邮箱";
   	  	if("1".equals(type)){
   	  		//修改密码
   	  		msg = "修改密码";
   	  		href = "http://account.wm927.com/service/do?mod=chpwd";
   	  	}else if("2".equals(type)){
   	  		href = "http://home.wm927.com/user/info/emailsucc?";
   	  	}
   	  	try {
			messageHelper = new MimeMessageHelper(mailMessage,true,"utf-8");
		    messageHelper.setTo(tomail);//接受者   
		    messageHelper.setFrom(mailSender.getUsername());//发送者
		    messageHelper.setSubject(msg);//主题
		    String idcode = DES.GetDES(id);
		    String vlidatecode = DES.GetDES(vlidateNum);
		    String longtime = Long.toString(currentTime);
		    String timecode = DES.GetDES(longtime);
		    String code = "uid="+idcode+"&code="+vlidatecode+"&date="+timecode;
		    String content =  "<a href="+href+"&"+code+">"+msg+"</a>";
		    if("1".equals(type)){
		    	messageHelper.setText(CodeWordString.update_one+content+CodeWordString.update_two,true);
		    }else{
		    	messageHelper.setText(CodeWordString.register_one+content+CodeWordString.register_two,true);
		    }
		    
		    mailSender.send(mailMessage);
	   } catch (Exception e) {
		   logger.info("send email error for ---->>>>"+e.getMessage());
		   flag = false;
	   }
		return flag;
	}

	/**
	 * @param toemail 发送人邮箱地址
	 * @param subject 发送的主题
	 * @param hrefcontent 要发送的连接内容
	 */
	/*public  boolean send(String toemail,String subject,String hrefcontent) {
		 MimeMessage mailMessage = mailSender.createMimeMessage();
		 //设置utf-8或GBK编码，否则邮件会有乱码
		 MimeMessageHelper messageHelper = null;
		 boolean flag = true;
  		 try {
			messageHelper = new MimeMessageHelper(mailMessage,true,"utf-8");
			messageHelper.setTo(toemail);//接受者   
		    messageHelper.setFrom(mailSender.getUsername());//发送者
		    messageHelper.setSubject(subject);//主题
		    messageHelper.setText(hrefcontent,true);
		    mailSender.send(mailMessage); 
		} catch (MessagingException e) {
			logger.info("send email error for ---->>>>"+e.getMessage());
			flag = false;
		}
  		return flag;
	     
	}*/
}
