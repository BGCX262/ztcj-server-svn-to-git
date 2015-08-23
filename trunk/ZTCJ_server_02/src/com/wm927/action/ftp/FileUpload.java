package com.wm927.action.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;

/**
 * 生成APP
 * @author chen
 *
 */
public class FileUpload {
	private Logger logger = Logger.getLogger(FileUpload.class);

	private  FTPClient ftpClient;
	

	/**  
	 *   
	 * @param addr 地址  
	 * @param port 端口号  
	 * @param username 用户名  
	 * @param password 密码  
	 * @return  
	 * @throws Exception  
	 */    
	 protected  boolean connect(String addr,int port,String username,String password)  {      
	     boolean result = false;      
		 ftpClient = new FTPClient();      
		 int reply;     
		 try {
			 logger.info("connect ftp begin");
			 ftpClient.connect(addr,port);
			 logger.info("connect ftp success");
			 ftpClient.login(username,password);
			 logger.info("login ftp success");
			 ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			 ftpClient.enterLocalActiveMode();
			 reply = ftpClient.getReplyCode();
			 if (!FTPReply.isPositiveCompletion(reply)) {      
				 ftpClient.disconnect();      
			      return result;      
			    }      
			  result = true;      
			 
		} catch (Exception e) {
			logger.info(e.getMessage());
		} 
		 return result;    
		}      
	
	 public static void main(String args[]){
		 FileUpload fileUpload = new FileUpload();
		 fileUpload.connect("61.145.163.87",21,"ftpapk","ftpapk@apk.com");
		 fileUpload.upload(new File("d:\\wisegeek_1805.apk"),"/webapp/appapk/andriod/1805/");
		
	 }
	 
	/**  
	 *   
	 * @param file 上传的文件或文件夹  
	 * @throws Exception  
	 */    
	 protected boolean upload(File downfile,String uppath) {
		 boolean flag = false;
		 if(downfile == null )
			 return flag;
		 if(!downfile.exists()){
			 return flag;
		 }
		 FileInputStream input = null;
		 try {
			 mkDir(uppath);
			 logger.info("change working directory is "+ftpClient.changeWorkingDirectory(uppath));   
		     input = new FileInputStream(downfile);    
		     flag = ftpClient.storeFile(uppath+downfile.getName(), input);      
			 } catch (IOException e) {
				 logger.info(e.getMessage());
			}finally{
				if(input!= null ){
					try {
						input.close();
					} catch (IOException e) {
						logger.info(e.getMessage());
					} 
				 }
			}                
	    	 return flag;
	   } 
	 private boolean mkDir(String path) throws IOException{
		 boolean flag = false;
		 if(DataUtils.checkString(path))
		 	return flag;
		 String paths[] = path.split("/");
		 for(String p: paths){
			 logger.info(p);
			 logger.info("create directory is "+ftpClient.makeDirectory(p));;
			 logger.info("change directory  is "+ftpClient.changeWorkingDirectory(p));
		 }
			 
		 return flag;
	 }
	 
	 /**
	  * 图片下载
	  * @param url
	  * @return
	  * @throws IOException
	  */
	 protected boolean downLoad(String inputurl,String outputurl,String filename) {
		 boolean flag = false;
		 if(DataUtils.checkString(inputurl)){
			 return flag;
		 }
		 File file = new File(outputurl);
		 if(!file.exists())
			 file.mkdirs();
		 OutputStream output = null;
         try {
			output = new FileOutputStream(outputurl+"/"+filename);
			flag = ftpClient.retrieveFile(inputurl, output);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}finally{
			if(output!= null ){
				try {
					output.close();
				} catch (IOException e) {
					logger.info(e.getMessage());
				} 
			 }
		}
		 return flag;
	 }
	 /**
	  * 释放资源
	  * @param ftpClient
	  */
	 protected void relaseServerConnection(){
		 try {
			 if(ftpClient !=null ){
				 if(ftpClient.isConnected())
					 ftpClient.disconnect();
			 }
		} catch (IOException e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}   
	 }
	 
	 
}    


