package com.example.photoer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;

import android.os.*;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;
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
	private static String urlstring = "http://192.168.1.111/fname";
	private static String urlstringbase = "http://192.168.1.111/";
	
	ImageView imgTest;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mhandler= new MyHandler();
		Button buttonTest = (Button) findViewById(R.id.buttontest);
		TextView textTest = (TextView) findViewById(R.id.textTest);
		 imgTest = (ImageView) findViewById(R.id.imageTest);
		 imgTest.setOnTouchListener(new MulitPointTouchListener(imgTest));
		buttonTest.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				triggerNewPicture();
			}
			
		});
	}

	private void triggerNewPicture(){
		toDlFilename = null;
		new Thread(new getFileName()).start();
	}
	private void triggerDownload(String filename){
		dlFilename = filename;
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
			
			String surl = urlstringbase+dlFilename;
			System.out.println(surl);
			dlFilename = null;
			try{
			
			 URL url = new URL(surl);     
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setDoInput(true);
		        conn.connect();
		        InputStream is = conn.getInputStream();
		        int length = (int) conn.getContentLength();
		        if (length <=0) {mhandler.obtainMessage(-2).sendToTarget();} else {
		        	byte [] imgData = new byte[length];
		        	byte[] tmp = new byte[512];
		        	int readLen =0, destPos = 0;
		        	while ((readLen = is.read(tmp))>0){
		        		System.arraycopy(tmp, 0, imgData, destPos, readLen);
		        		destPos += readLen;
		        		System.out.println(destPos);
		        	}
		        	downloadedBitmap = BitmapFactory.decodeByteArray(imgData, 0, length);
		            if (downloadedBitmap == null) mhandler.obtainMessage(-6).sendToTarget();  
		            else ;// display image  
		            mhandler.obtainMessage(200).sendToTarget();
		        }
			}catch (ClientProtocolException e){
				mhandler.obtainMessage(-1).sendToTarget();
			}catch (IOException e) {
				mhandler.obtainMessage(-2).sendToTarget();			
			}
		}
		
		
	}
	String toDlFilename  = null;
	String dlFilename = null;
	Bitmap downloadedBitmap = null;
	MyHandler mhandler;
	private class MyHandler extends Handler{
		public void dispatchMessage (Message msg){
			System.out.println(msg.what);
			
			if (msg.what >=2000){
				System.out.println(toDlFilename);
				Toast.makeText(MainActivity.this,"RET VALUE"+String.valueOf(msg.what),Toast.LENGTH_SHORT).show();
				triggerDownload(toDlFilename);
				toDlFilename = null;
				return;
			}
			if (msg.what == 200) {
				displayNewPic();
			}
			
			if (msg.what == -1) {
				Toast.makeText(MainActivity.this,"Fail to upload:ClientProtocolException",Toast.LENGTH_SHORT).show();
			}else if (msg.what == -2) {
				Toast.makeText(MainActivity.this,"Fail to upload:Please check the network",Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -3){
				Toast.makeText(MainActivity.this,"Fail to upload:General Exception",Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -5) {
				Toast.makeText(MainActivity.this, "GET picture not OK", Toast.LENGTH_SHORT).show();
			}
			else if (msg.what == -6) {
				Toast.makeText(MainActivity.this, "GET picture data error", Toast.LENGTH_SHORT).show();
			}
			return;
			
		}
	}
	private void displayNewPic(Bitmap bmp ){
		imgTest.setImageBitmap(bmp);
		imgTest.invalidate();
	}
	private void displayNewPic(){
		displayNewPic(downloadedBitmap);
		downloadedBitmap = null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class MulitPointTouchListener implements OnTouchListener {    
		  
	    Matrix matrix = new Matrix();    
	    Matrix savedMatrix = new Matrix();    
	  
	    public ImageView image;    
	    static final int NONE = 0;    
	    static final int DRAG = 1;    
	    static final int ZOOM = 2;    
	    int mode = NONE;    
	  
	    PointF start = new PointF();    
	    PointF mid = new PointF();    
	    float oldDist = 1f;    
	  
	  
	    public MulitPointTouchListener(ImageView image) {    
	        super();    
	        this.image = image;    
	    }    
	  
	    @Override    
	    public boolean onTouch(View v, MotionEvent event) {    
	        this.image.setScaleType(ScaleType.MATRIX);    
	  
	        ImageView view = (ImageView) v;    
//	      dumpEvent(event);    
	  
	        switch (event.getAction() & MotionEvent.ACTION_MASK) {  
	          
	        case MotionEvent.ACTION_DOWN:    
	  
	  //          Log.w("FLAG", "ACTION_DOWN");  
	            matrix.set(view.getImageMatrix());    
	            savedMatrix.set(matrix);    
	            start.set(event.getX(), event.getY());    
	            mode = DRAG;    
	            break;    
	        case MotionEvent.ACTION_POINTER_DOWN:    
	    //        Log.w("FLAG", "ACTION_POINTER_DOWN");  
	            oldDist = spacing(event);    
	            if (oldDist > 10f) {    
	                savedMatrix.set(matrix);    
	                midPoint(mid, event);    
	                mode = ZOOM;    
	            }    
	            break;    
	        case MotionEvent.ACTION_UP:    
	      //      Log.w("FLAG", "ACTION_UP");  
	        case MotionEvent.ACTION_POINTER_UP:    
	        //    Log.w("FLAG", "ACTION_POINTER_UP");  
	            mode = NONE;    
	            break;    
	        case MotionEvent.ACTION_MOVE:    
	          //  Log.w("FLAG", "ACTION_MOVE");  
	            if (mode == DRAG) {    
	                matrix.set(savedMatrix);    
	                matrix.postTranslate(event.getX() - start.x, event.getY()    
	                        - start.y);    
	            } else if (mode == ZOOM) {    
	                float newDist = spacing(event);    
	                if (newDist > 10f) {    
	                    matrix.set(savedMatrix);    
	                    float scale = newDist / oldDist;    
	                    matrix.postScale(scale, scale, mid.x, mid.y);    
	                }    
	            }    
	            break;    
	        }    
	  
	        view.setImageMatrix(matrix);    
	        return true;  
	    }    
	  
	      
	    private float spacing(MotionEvent event) {    
	        float x = event.getX(0) - event.getX(1);    
	        float y = event.getY(0) - event.getY(1);    
	        return FloatMath.sqrt(x * x + y * y);    
	    }    
	  
	    private void midPoint(PointF point, MotionEvent event) {    
	        float x = event.getX(0) + event.getX(1);    
	        float y = event.getY(0) + event.getY(1);    
	        point.set(x / 2, y / 2);    
	    }    
	}   
}
