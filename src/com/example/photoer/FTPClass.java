package com.example.photoer;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.os.Handler;
import android.os.Message;

public class FTPClass {

 	/**
		 * 通过ftp上传文件
		 * @param url ftp服务器地址 如： 192.168.1.110
		 * @param port 端口如 ： 21
		 * @param username  登录名
		 * @param password   密码
		 * @param remotePath  上到ftp服务器的磁盘路径
		 * @param fileNamePath  要上传的文件路径
		 * @param fileName		要上传的文件名
		 * @return
		 */
	 private FTPClient ftpClient;
	 private String murl,mport,musername,mpwd,mrmp,mfname;
	 FileInputStream mfis;
	 public String ftpUpload(String url, String port, String username,String password, String remotePath,String fileName,FileInputStream fis) {
		 ftpClient = new FTPClient();
		 String returnMessage = "0";
		 murl=url; mport=port;musername=username;mpwd=password;mrmp=remotePath;
		 mfname=fileName;
		 mfis=fis;
		 new Thread(new runFTP()).start();
	     return returnMessage;
			 
	 }
	 private class runFTP implements Runnable{

			@Override
			public void run() {
				 try {
					 System.out.println(murl);
					 ftpClient.connect(murl, Integer.parseInt(mport));
					 boolean loginResult = ftpClient.login(musername, mpwd);
					 int returnCode = ftpClient.getReplyCode();
					 if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
						 ftpClient.makeDirectory(mrmp);
						 // 设置上传目录
						 ftpClient.changeWorkingDirectory(mrmp);
						 ftpClient.setBufferSize(1024);
						 ftpClient.setControlEncoding("UTF-8");
						 ftpClient.enterLocalPassiveMode();
						 ftpClient.storeFile(mfname, mfis);
						 
						 }
					 		 
			
				 } catch (IOException e) {
					 e.printStackTrace();
					 throw new RuntimeException("FTP客户端出错！", e);
				 } finally {
					 //IOUtils.closeQuietly(fis);
				 try {
					 ftpClient.disconnect();
				 } catch (IOException e) {
					 	e.printStackTrace();
					 	throw new RuntimeException("关闭FTP连接发生异常！", e);
				 	}
				 }

					handler.sendEmptyMessage(0);
			}

	 }

		 //定义Handler对象
		 private Handler handler =new Handler(){
		 @Override
		 //当有消息发送出来的时候就执行Handler的这个方法
		 public void handleMessage(Message msg){
		 super.handleMessage(msg);
		 //处理UI
		 }
		 };


}
