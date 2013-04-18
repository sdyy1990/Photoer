package com.example.photoer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	 FTPClass(Handler handler){
		 this.handler = handler;
	 }
	 
	 private FTPClient ftpClient;
	 private String murl,mport,musername,mpwd,mrmp,mfname;
	 FileInputStream mfis;
	 
	 public String ftpUpload0(String url, String port, String username,String password, String remotePath,String fileName,String fname) {
		 ftpClient = new FTPClient();
		 String returnMessage = "0";
		 murl=url; mport=port;musername=username;mpwd=password;mrmp=remotePath;
		 mfname=fileName;
		 try {
			mfis=new FileInputStream(new File(fname));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
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
						 ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
						 ftpClient.enterLocalPassiveMode();
						 ftpClient.storeFile(mfname, mfis);
						 handler.obtainMessage(0, 0, 0, mrmp).sendToTarget();
						 }
					 		 
			
				 } catch (IOException e) {
					 e.printStackTrace();
					 handler.obtainMessage(1).sendToTarget();
				 } finally {
					 //IOUtils.closeQuietly(fis);
				 try {
					 ftpClient.disconnect();
					 mfis.close();
				 } catch (IOException e) {
					 	e.printStackTrace();
					 	handler.obtainMessage(2).sendToTarget();
				 	}
				 }


			}

	 }

		 //定义Handler对象
		 private Handler handler;
	


}
