package com.example.photoer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Camera extends Activity {
	private ImageView buttonShot,buttonLeft,buttonRight,buttonUp,buttonDown,buttonZoomIn,buttonZoomOut;

    private final static String ALBUM_PATH  
            = Environment.getExternalStorageDirectory() + "/download/";
	private static int MOTION_LEFT = 1;
	private static int MOTION_RIGHT = 2;
	private static int MOTION_UP = 3;
	private static int MOTION_DOWN = 4; 
	private static int MESSAGE_GETFILENAME = 1;
	private static int MESSAGE_SIZE_REPORT = 2;
	private static int MESSAGE_PERCENTAGE_REPORT = 3;
	private static int MESSAGE_DLFinish_REPORT = 4;
	private static int MESSAGE_Storage_file = 5;

	private int small_width = 120;
	private int small_height = 80;
private static String shotUrlSuffix = "/cgi-bin/capture.cgi";
	//private static String shotUrlSuffix = "/ccgbin/capture.cgi";
	private static String zoomUrlSuffix = "/cgi-bin/zoom.cgi?";
	private static String moveUrlSuffix = "/cgi-bin/move.cgi?";
	private static String pictureUrlFolder = "/img/";
	private String cameraurl,ftpurl,uname,dlFilename;
	TextView textView ;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		Bundle bundle = (Camera.this.getIntent().getExtras());
		uname = bundle.getString("uname");
		ftpurl =  bundle.getString("ftpurl");
		cameraurl =  bundle.getString("cameraurl");
		buttonShot = (ImageView) findViewById(R.id.shotView);
		buttonLeft = (ImageView) findViewById(R.id.leftView);
		buttonRight = (ImageView) findViewById(R.id.rightView);
		buttonUp = (ImageView) findViewById(R.id.upView);
		buttonDown = (ImageView) findViewById(R.id.downView);
		buttonZoomIn = (ImageView) findViewById(R.id.zoomInView);
		buttonZoomOut = (ImageView) findViewById(R.id.zoomOutView);
		textView = (TextView) findViewById(R.id.textsize);
	    WebView webview = (WebView) findViewById(R.id.webView1);  
	    webview.getSettings().setJavaScriptEnabled(true);  
	    String url2 = cameraurl+":8080/javascript_simple.html";
	    webview.loadUrl(url2);
	    webview.getSettings().setSupportZoom(true);
	    webview.getSettings().setBuiltInZoomControls(true);
	        
		Button buttonBrowser = (Button) findViewById(R.id.buttonbrowser);
		networkBusy = false;
		mhandler = new MyHandler();
		buttonLeft.setOnClickListener(new myOnClickListener2(cameraurl+moveUrlSuffix+"L"));
		buttonRight.setOnClickListener(new myOnClickListener2(cameraurl+moveUrlSuffix+"R"));
		buttonUp.setOnClickListener(new myOnClickListener2(cameraurl+moveUrlSuffix+"U"));
		buttonDown.setOnClickListener(new myOnClickListener2(cameraurl+moveUrlSuffix+"D"));
		buttonZoomIn.setOnClickListener(new myOnClickListener(cameraurl+zoomUrlSuffix,true,0,9));
		buttonZoomOut.setOnClickListener(new myOnClickListener(cameraurl+zoomUrlSuffix,false,0,9));
		
		new myOnClickListener(cameraurl+zoomUrlSuffix,false,0,9).zoomTo(0);
		buttonBrowser.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(Camera.this, Browser.class);
				intent.putExtras((Camera.this.getIntent().getExtras()));
				startActivity(intent);
				Camera.this.finish();
			}
		});
		buttonShot.setOnClickListener(new ImageView.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				TriggerShot();
			}
		
		});
		buttonLeft.setOnClickListener(new ImageView.OnClickListener(){
			@Override
			public void onClick(View arg0) {TriggerCameraMove(MOTION_LEFT);}});
		buttonRight.setOnClickListener(new ImageView.OnClickListener(){
			@Override
			public void onClick(View arg0) {TriggerCameraMove(MOTION_RIGHT);}});
		buttonUp.setOnClickListener(new ImageView.OnClickListener(){
			@Override
			public void onClick(View arg0) {TriggerCameraMove(MOTION_UP);}});
		buttonDown.setOnClickListener(new ImageView.OnClickListener(){
			@Override
			public void onClick(View arg0) {TriggerCameraMove(MOTION_DOWN);}});
		
	}
	private boolean networkBusy;
	private void TriggerCameraMove(int MODE){
		if (networkBusy) displayBusyToken(); else{
			
		}
	}
	private void displayBusyToken(){
		Toast.makeText(Camera.this,"正在与相机通讯，请稍候",Toast.LENGTH_SHORT).show();
	}
	private void TriggerShot() {
		//../cgi-bin/capture.cgi
		if (networkBusy) displayBusyToken(); else {
		networkBusy = true;
		new Thread(new getFileName()).start();
		}
	}
	private void triggerDownload(){		
		networkBusy = true;
		new Thread(new downloadFile()).start();
	}

	private class downloadFile implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String surl = cameraurl + pictureUrlFolder+dlFilename;
			System.out.println(surl);
			try{
			 URL url = new URL(surl);     
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setDoInput(true);
		        conn.connect();
		        InputStream is = conn.getInputStream();
		        int length = (int) conn.getContentLength();
		        mhandler.obtainMessage(MESSAGE_SIZE_REPORT, 0, length);
		        if (length <=0) {mhandler.obtainMessage(-2).sendToTarget();} else {
		        	byte [] imgData = new byte[length];
		        	int siz = 512;
		        	while (siz*100< length) siz *=2;
		        	byte[] tmp = new byte[siz];
		        	int readLen =0, destPos = 0;
		        	while ((readLen = is.read(tmp))>0){
		        		System.arraycopy(tmp, 0, imgData, destPos, readLen);
		        		mhandler.obtainMessage(MESSAGE_PERCENTAGE_REPORT, 0,destPos);
		        		//System.out.println(destPos);
		        		destPos += readLen;
		        	}
		        	is.close();
		        	System.gc();
		        	BitmapFactory.Options opts = new BitmapFactory.Options();
		        	opts.inSampleSize = 4;
		        	Bitmap downloadedBitmap = BitmapFactory.decodeByteArray(imgData, 0, length,opts);
		            if (downloadedBitmap == null) mhandler.obtainMessage(-6).sendToTarget();  
		            else ;// display image  
		            mhandler.obtainMessage(MESSAGE_DLFinish_REPORT,0,0,downloadedBitmap).sendToTarget();
		            mhandler.obtainMessage(MESSAGE_Storage_file,0,0,imgData).sendToTarget();
		            
		        }
			}catch (ClientProtocolException e){
				mhandler.obtainMessage(-5).sendToTarget();
			}catch (IOException e) {
				mhandler.obtainMessage(-6).sendToTarget();			
			}
		}
		
		
	}

	
	private class getFileName implements Runnable{

		@Override
		public void run() {
			String url = cameraurl + shotUrlSuffix;
			HttpGet httpRequest = new HttpGet(url);
			try{
				
				BasicHttpParams hpms = new BasicHttpParams();
				DefaultHttpClient dfh = new DefaultHttpClient(hpms);
				dfh.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
				dfh.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 11000);
				HttpResponse response = dfh.execute(httpRequest);
				InputStream is = response.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				dlFilename = br.readLine()+".jpg";
				//TODO 
				
				System.out.println(dlFilename);
				mhandler.obtainMessage(MESSAGE_GETFILENAME, response.getStatusLine().getStatusCode()).sendToTarget();
				
			}catch (ClientProtocolException e){
				mhandler.obtainMessage(-1).sendToTarget();
			}catch (IOException e) {
				mhandler.obtainMessage(-2).sendToTarget();			
			}catch (Exception e) {
				mhandler.obtainMessage(-3).sendToTarget();
			}
		}
		
		
	}
	String saveFilename;
	private void Finishdownload(Bitmap bmp) {
		
	    File dirFile = new File(ALBUM_PATH);  
		if(!dirFile.exists()){     dirFile.mkdir();       }  
		saveFilename = uname + (new SimpleDateFormat(".MMddHHmmss")).format(new Date()) +".jpg";
		File myCaptureFile = new File(ALBUM_PATH + saveFilename);  
		File mySmallFile = new File(ALBUM_PATH +saveFilename+".jpg");
		BufferedOutputStream bos,bos2;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
			bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);  
			bos.flush();  
			bos.close();  
            Bitmap smp = Bitmap.createScaledBitmap(bmp, small_width, small_height, false);
            bos2 = new BufferedOutputStream(new FileOutputStream(mySmallFile));
			smp.compress(Bitmap.CompressFormat.JPEG, 80, bos2);  
			bos2.flush(); 
			bos2.close();
			bmp.recycle(); smp.recycle();
			Toast.makeText(Camera.this,"图片已保存",Toast.LENGTH_SHORT).show();
			System.gc();
		} catch (FileNotFoundException e) {
			Toast.makeText(Camera.this,"建立文件错误 FileNotFoundException",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(Camera.this,"读写错误 IOException",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}  
	}

	MyHandler mhandler;
	int picturesize;
	private class MyHandler extends Handler{
		public void dispatchMessage (Message msg){
			System.out.println(msg.what);
			if (msg.what == MESSAGE_SIZE_REPORT) {
				picturesize = msg.arg1;
			   return;
			}
			if (msg.what == MESSAGE_PERCENTAGE_REPORT) {
				textView.setText(String.valueOf(msg.arg1)+"/"+String.valueOf(picturesize));
				textView.invalidate();
				return;
		    }
			if (msg.what == MESSAGE_Storage_file) {
				byte[] res  = (byte []) msg.obj;
				File myRawFile = new File(ALBUM_PATH +saveFilename +".raw.jpg"); 
				
				BufferedOutputStream bos;
				try {
					bos = new BufferedOutputStream(new FileOutputStream(myRawFile));
					bos.write(res);
					bos.flush();  
					bos.close();  
					System.gc();
				} catch (FileNotFoundException e) {
					Toast.makeText(Camera.this,"建立文件错误 FileNotFoundException",Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				} catch (IOException e) {
					Toast.makeText(Camera.this,"读写错误 IOException",Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}  
			}
				
			if (msg.what == MESSAGE_GETFILENAME){
				networkBusy = false;
				if (msg.arg1 >=0) {
			    	Toast.makeText(Camera.this,"拍照成功 正在下载",Toast.LENGTH_SHORT).show();
				    triggerDownload();
				}
				else Toast.makeText(Camera.this,"拍照失败",Toast.LENGTH_SHORT).show();
				return;
			}		        		
			if (msg.what == MESSAGE_DLFinish_REPORT) {
				networkBusy = false;
				Finishdownload((Bitmap)msg.obj);
			}
			if (msg.what<0) networkBusy = false;
			if (msg.what == -1) {
				Toast.makeText(Camera.this,"拍照：网络链接错误:ClientProtocolException",Toast.LENGTH_SHORT).show();
			}else if (msg.what == -2) {
				Toast.makeText(Camera.this,"拍照：请检查链路有效",Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -3){
				Toast.makeText(Camera.this,"拍照：一般网络错误",Toast.LENGTH_SHORT).show();
			}
			if (msg.what == -9) {
				//Toast.makeText(Camera.this,"Motion：网络链接错误:ClientProtocolException",Toast.LENGTH_SHORT).show();
			}else if (msg.what == -10) {
				//Toast.makeText(Camera.this,"Motion：请检查链路有效",Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -11){
				//Toast.makeText(Camera.this,"Protocal：一般网络错误",Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -5) {
				Toast.makeText(Camera.this, "下载照片：一般网络错误", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -6) {
				Toast.makeText(Camera.this, "下载照片：文件错误", Toast.LENGTH_SHORT).show();
			}
			return;
			
		}
	}
	private int zoomLevel ;
	private class myOnClickListener implements Button.OnClickListener {
		private String motionURL,lUrl;
		
		private boolean iszoom,isZoomIn;
		int zoomMin,zoomMax;

		myOnClickListener(final String str,boolean isZoomIn,int zoomMin,int zoomMax) {
			super();
			this.lUrl = str;
			this.iszoom = true;
			this.isZoomIn=isZoomIn;
			this.zoomMax = zoomMax;
			this.zoomMin = zoomMin;
		}
		@Override
		public void onClick(View arg0) {
			if (iszoom){
				if (isZoomIn) zoomLevel ++;
				else zoomLevel --;
				if (zoomLevel<zoomMin) zoomLevel = zoomMin;
				if (zoomLevel>zoomMax) zoomLevel = zoomMax;
				zoomTo(zoomLevel);
			}
            new Thread(new getMotion()).start();
		}
		
		private void zoomTo(int zoomLevel) {
			motionURL = lUrl+String.valueOf(zoomLevel);
			new Thread(new getMotion()).start();
		}

		private class getMotion implements Runnable{
			@Override
			public void run() {
				String url = motionURL;
				System.out.println(url);
				HttpGet httpRequest = new HttpGet(url);
				try{
					BasicHttpParams hpms = new BasicHttpParams();
					DefaultHttpClient dfh = new DefaultHttpClient(hpms);
					dfh.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
					dfh.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 11000);
					dfh.execute(httpRequest);
				}catch (ClientProtocolException e){
					mhandler.obtainMessage(-9).sendToTarget();
				}catch (IOException e) {
					mhandler.obtainMessage(-10).sendToTarget();			
				}catch (Exception e) {
					mhandler.obtainMessage(-11).sendToTarget();
				}
			}
			
			
		}
	}
	private class myOnClickListener2 implements Button.OnClickListener {
		private String motionURL;
		myOnClickListener2(final String str) {
			super();
			this.motionURL = str;
		}

		@Override
		public void onClick(View arg0) {
            new Thread(new getMotion()).start();
		}
		private class getMotion implements Runnable{
			@Override
			public void run() {
				String url = motionURL;
				System.out.println(url);
				HttpGet httpRequest = new HttpGet(url);
				try{
					BasicHttpParams hpms = new BasicHttpParams();
					DefaultHttpClient dfh = new DefaultHttpClient(hpms);
					dfh.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
					dfh.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 11000);
					dfh.execute(httpRequest);
				}catch (ClientProtocolException e){
					mhandler.obtainMessage(-9).sendToTarget();
				}catch (IOException e) {
					mhandler.obtainMessage(-10).sendToTarget();			
				}catch (Exception e) {
					mhandler.obtainMessage(-11).sendToTarget();
				}
			}
			
			
		}
	}
}
