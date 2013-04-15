package com.example.photoer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;



import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public class Browser extends Activity {
    String uname,ftpurl;

    private final static String ALBUM_PATH  
            = Environment.getExternalStorageDirectory() + "/download/";
    public int selectedid;
    private BrowserView browserView;
    private Gallery gallery;
    private Button mSwitchButton;
    ArrayList<String> flist;
	@SuppressWarnings("deprecation")
	Bitmap smallprint;
	ImageAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser);
		Bundle bundle = (Browser.this.getIntent().getExtras());
		uname = bundle.getString("uname");
		ftpurl =  bundle.getString("ftpurl");
		browserView = (BrowserView) findViewById(R.id.showImage); 
		browserView.setOwner(this);
		mSwitchButton = (Button) findViewById(R.id.buttonSwitchPrinter);

		smallprint = BitmapFactory.decodeResource(Browser.this.getResources(), R.drawable.smallprint);

        adapter = new ImageAdapter(this);

		getImgFileList(); 
		adapter.setFlist(flist);
		mSwitchButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				File file = new File(ALBUM_PATH+currentFilename);
				String newname;
				if (currentPrintable) {
				   file.renameTo(new File( newname = ALBUM_PATH+namewithoutprint(currentFilename)));
				   Toast.makeText(Browser.this, "这张照片将只保留电子版", Toast.LENGTH_SHORT).show();
				}
				else{ 
				   file.renameTo(new File(newname= ALBUM_PATH+currentFilename.substring(0,currentFilename.indexOf(".jpg"))+".print.jpg"));
				   Toast.makeText(Browser.this, "这张照片将被打印", Toast.LENGTH_SHORT).show();
				}
				currentFilename = newname;
				setswitchButton();
				getImgFileList();
			}});
		Button deleteButton = (Button) findViewById(R.id.buttonDelete);
		Button exitButton = (Button) findViewById(R.id.buttonExit);
		Button backButton = (Button) findViewById(R.id.buttonBack);
		backButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent();
				intent.setClass(Browser.this, Camera.class);
				intent.putExtras((Browser.this.getIntent().getExtras()));
				startActivity(intent);
				Browser.this.finish();
			}});
		exitButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {

				new AlertDialog.Builder(Browser.this).setTitle("确认退出").setMessage("请确认已经上传了您的照片之后再退出")
					     .setPositiveButton("退出",  new OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,	int which) {
										Intent intent = new Intent();
										intent.setClass(Browser.this, Welcome.class);
										startActivity(intent);
										Browser.this.finish();
									}} )
						.setNegativeButton("返回", new OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {}})
						.create().show();
				
			}});
		
		deleteButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(Browser.this).setTitle("确认删除").setMessage("请确认删除这张照片")
					     .setPositiveButton("删除",  new OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog,	int which) {
										File file = new File(ALBUM_PATH+currentFilename);
										file.delete();
										getImgFileList();
										while (selectedid>=flist.size()) selectedid --;
										if (flist.size() ==0) browserView.setImage(null);
										else browserView.setImage(flist.get(selectedid));
									}} )
						.setNegativeButton("返回", new OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {}})
						.create().show();
				
			}});
		
		System.gc();
		gallery = (Gallery)findViewById(R.id.gallery1);
        adapter.setActivity(Browser.this);
        TypedArray typedArray = obtainStyledAttributes(R.styleable.Gallery);
        adapter.setmGalleryItemBackground(typedArray.getResourceId(R.styleable.Gallery_android_galleryItemBackground, 0));
        gallery.setAdapter(adapter);
        gallery.setOnItemClickListener(new Gallery.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			// Toast.makeText(Browser.this,"你选择了"+String.valueOf(arg2)+"号图片",Toast.LENGTH_SHORT).show();   
			browserView.setImage(flist.get(selectedid = arg2));
		}});

		if (flist.size()>0) {
		   browserView.setImage(flist.get(flist.size()-1));
		   gallery.setSelected(true);
		   gallery.setSelection(flist.size()-1);
		}
		else {
			Intent intent = new Intent();
			intent.setClass(Browser.this, Welcome.class);
			startActivity(intent);
			Browser.this.finish();
		}
		
    }
	
	private void getImgFileList() {
		flist = new ArrayList<String>();
	    File dirFile = new File(ALBUM_PATH);  
        File[] files = dirFile.listFiles();
		for (File onefile:files) {
			String fname = onefile.getName();
			if (fname.startsWith(uname) && fname.indexOf(".jpg.jpg")<0) {
				flist.add(fname);

				System.out.println(fname);
			}
		}
		System.out.println(flist.size());
		Collections.sort(flist);
		System.gc();
		adapter.setFlist(flist);
		adapter.notifyDataSetChanged();
		
		
	}

    private class ImageAdapter extends BaseAdapter {
	int mGalleryItemBackground;
	private Context context;
	private Activity myActivity;
	private ArrayList<String> flist;
	public ImageAdapter(Context context) {
		this.context = context;
	}
	public void setFlist(ArrayList<String> flist){
		this.flist = flist;
	}
	
	public void setActivity(Activity owner) {
		myActivity = owner;
	}
	@Override
	public int getCount() {
		return flist.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView imageView = new ImageView(context);
		String fname = flist.get(position);
		
		String name_without_print = namewithoutprint(fname);
		System.out.println(name_without_print);
		Bitmap src = BitmapFactory.decodeFile(ALBUM_PATH+name_without_print+".jpg")	
				 .copy(Bitmap.Config.ARGB_8888, true);
		if (fname.contains(".print"))
		{
	    Canvas cv = new Canvas( src);
	    cv.drawBitmap(smallprint, null,new Rect(60,40,110,90), null);//在 0，0坐标开始画入src  
	    cv.save( Canvas.ALL_SAVE_FLAG );//保存  
	    //store  
	    cv.restore();//存储
		}
	       
		imageView.setImageBitmap(src);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imageView.setLayoutParams(new Gallery.LayoutParams(120, 80));
		imageView.setBackgroundResource(mGalleryItemBackground);
		System.gc();
		return imageView;
	}

	public int getmGalleryItemBackground() {
		return mGalleryItemBackground;
	}

	public void setmGalleryItemBackground(int mGalleryItemBackground) {
		this.mGalleryItemBackground = mGalleryItemBackground;
	}

}

    private String namewithoutprint(String fname) {
    	int index;
		if ((index = fname.indexOf(".print"))<0)
		return fname;
		else return fname.substring(0,index)+".jpg";
	}
	private boolean isprintable(String fname) {
		//TODO
		return (fname.indexOf(".print")>0);
		
	}
	private boolean currentPrintable;
	public String currentFilename;
	private void setViewImage(String fname) {
		currentFilename = fname;
		browserView.setImage(fname);
		setswitchButton();
		
	}
	public void setswitchButton() {
		currentPrintable=  (isprintable(currentFilename));
		if(currentPrintable) {
			mSwitchButton.setText("取消打印");
			mSwitchButton.setBackgroundResource(R.drawable.cancelprint);
		}
		else {
			mSwitchButton.setText("打印");
			mSwitchButton.setBackgroundResource(R.drawable.print);
		}
	}

	public void setcurrentFilename(String imagename) {
		this.currentFilename = imagename;
	}

}
