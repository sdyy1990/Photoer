package com.example.photoer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;

import android.os.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final static String ALBUM_PATH  
            = Environment.getExternalStorageDirectory() + "/download/";
	private static String urlstring = "http://192.168.1.111/fname";
	private static String urlstringbase = "http://192.168.1.111/";
	private static int MESSAGE_SIZE_REPORT = 1048576+1;
	private static int MESSAGE_PERCENTAGE_REPORT = 1048576+2;
	private boolean isGettingFilename = false;
	public ArrayList<String> downloadingFiles; 
	public ArrayList<Integer>    FileRIDs;
	ImageView imgTest;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		downloadingFiles = new ArrayList<String>();
		FileRIDs = new ArrayList<Integer> ();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mhandler= new MyHandler();
		ImageView buttonTest = (ImageView) findViewById(R.id.buttontest);
		//TextView textTest = (TextView) findViewById(R.id.textTest);
		 imgTest = (ImageView) findViewById(R.id.imageTest);
		 imgTest.setOnTouchListener(new MulitPointTouchListener(imgTest));
		buttonTest.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				triggerNewPicture();
			}
			
		});
		ImageView uploadButton = (ImageView) findViewById(R.id.uploadView);
		uploadButton.setOnClickListener(new ImageView.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				FTPClass ftp = new FTPClass();
				try {
					ftp.ftpUpload("192.168.1.111", "21", "anonymous", "hi", "/public/", lastname, new FileInputStream
							(new File(ALBUM_PATH + lastname)));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		});
	}

	private boolean triggerNewPicture(){
		if (isGettingFilename) return false;
		isGettingFilename = true;
		toDlFilename = null;
		new Thread(new getFileName()).start();
		return true;
	}
	private void triggerDownload(String filename){		
		int mRID = (int) (Math.random()*1048576.0);
		downloadingFiles.add(filename);
		FileRIDs.add(Integer.valueOf(mRID));
		isGettingFilename = false;
		new Thread(new downloadFile()).start();
	}

	private class getFileName implements Runnable{

		@Override
		public void run() {
			String url = urlstring;
			HttpGet httpRequest = new HttpGet(url);
			try{
				
				BasicHttpParams hpms = new BasicHttpParams();
				DefaultHttpClient dfh = new DefaultHttpClient(hpms);
				dfh.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
				dfh.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 11000);
				
				HttpResponse response = dfh.execute(httpRequest);
				InputStream is = response.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				toDlFilename = br.readLine();
				mhandler.obtainMessage(2000+ response.getStatusLine().getStatusCode()).sendToTarget();
				
			}catch (ClientProtocolException e){
				mhandler.obtainMessage(-1).sendToTarget();
			}catch (IOException e) {
				mhandler.obtainMessage(-2).sendToTarget();			
			}catch (Exception e) {
				mhandler.obtainMessage(-3).sendToTarget();
			}
		}
		
		
	}
	private class downloadFile implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int myRIDn = (FileRIDs.size()-1);
			int myRID = FileRIDs.get((FileRIDs.size()-1)).intValue();
			String surl = urlstringbase+((String)downloadingFiles.get(myRIDn));
			System.out.println(surl);
			try{
			
			 URL url = new URL(surl);     
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setDoInput(true);
		        conn.connect();
		        InputStream is = conn.getInputStream();
		        int length = (int) conn.getContentLength();
		        mhandler.obtainMessage(MESSAGE_SIZE_REPORT, myRID, length);
		        if (length <=0) {mhandler.obtainMessage(-2).sendToTarget();} else {
		        	byte [] imgData = new byte[length];
		        	int siz = 512;
		        	while (siz*100< length) siz *=2;
		        	byte[] tmp = new byte[siz];
		        	int readLen =0, destPos = 0;
		        	while ((readLen = is.read(tmp))>0){
		        		System.arraycopy(tmp, 0, imgData, destPos, readLen);
		        		mhandler.obtainMessage(MESSAGE_PERCENTAGE_REPORT, myRID,destPos);
		        		destPos += readLen;
		        	}
		        	Bitmap downloadedBitmap = BitmapFactory.decodeByteArray(imgData, 0, length);
		            if (downloadedBitmap == null) mhandler.obtainMessage(-6).sendToTarget();  
		            else ;// display image  
		            mhandler.obtainMessage(200,myRID,0,downloadedBitmap).sendToTarget();
		        }
			}catch (ClientProtocolException e){
				mhandler.obtainMessage(-5).sendToTarget();
			}catch (IOException e) {
				mhandler.obtainMessage(-6).sendToTarget();			
			}
		}
		
		
	}
	String toDlFilename  = null;
	String dlFilename = null;
	MyHandler mhandler;
	private class MyHandler extends Handler{
		public void dispatchMessage (Message msg){
			System.out.println(msg.what);
			if (msg.what == MESSAGE_SIZE_REPORT) {
			   return;
			}
			if (msg.what == MESSAGE_PERCENTAGE_REPORT) {
				
				   return;
		    }
			if (msg.what >=2000){
				System.out.println(toDlFilename);
				Toast.makeText(MainActivity.this,"RET VALUE"+String.valueOf(msg.what),Toast.LENGTH_SHORT).show();
				triggerDownload(toDlFilename);
				toDlFilename = null;
				return;
			}		        		
			if (msg.what == 200) {
				Finishdownload((Bitmap)msg.obj,msg.arg1);
			}
			
			if (msg.what == -1) {
				Toast.makeText(MainActivity.this,"拍照：网络链接错误:ClientProtocolException",Toast.LENGTH_SHORT).show();
			}else if (msg.what == -2) {
				Toast.makeText(MainActivity.this,"拍照：请检查链路有效",Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -3){
				Toast.makeText(MainActivity.this,"拍照：一般网络错误",Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -5) {
				Toast.makeText(MainActivity.this, "下载照片：一般网络错误", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -6) {
				Toast.makeText(MainActivity.this, "下载照片：文件错误", Toast.LENGTH_SHORT).show();
			}
			return;
			
		}
	}
	private void displayNewPic(Bitmap bmp ){
		imgTest.setImageBitmap(bmp);
		imgTest.invalidate();
	}
	private String lastname;
	private void Finishdownload(Bitmap bmp,int myRID) {
		displayNewPic(bmp);
		int id = FileRIDs.indexOf(Integer.valueOf(myRID));
		FileRIDs.remove(id);
		String name = downloadingFiles.get(id); 
		lastname = name;
		downloadingFiles.remove(id);
		//save file
		
	    File dirFile = new File(ALBUM_PATH);  
		if(!dirFile.exists()){     dirFile.mkdir();       }  
		File myCaptureFile = new File(ALBUM_PATH + name);  
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));

			bmp.compress(Bitmap.CompressFormat.JPEG, 99, bos);  
			bos.flush();  
			bos.close();  
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
