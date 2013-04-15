package com.example.photoer;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BrowserView extends ImageView {

    private final static String ALBUM_PATH  
            = Environment.getExternalStorageDirectory() + "/download/";
    
	public BrowserView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setOnTouchListener(new MultiPointTouchListener());
	}
	public BrowserView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public BrowserView(Context context) {
		super(context);
	}
	public void setOwner(Browser owner) {
		this.owner = owner;
	}
	private Browser owner;
	public void setImage(String imagename) {
	    //File dirFile = new File(ALBUM_PATH);
		try{
		File file = new File(ALBUM_PATH+imagename);
		if (!file.exists()) {
			imagename = imagename.substring(0,imagename.indexOf(".jpg"))+".print.jpg";
		}
		Bitmap bmp = BitmapFactory.decodeFile(ALBUM_PATH+imagename);
		owner.setcurrentFilename(imagename);
		owner.setswitchButton();
		this.setImageBitmap(bmp);
		}
		catch (Exception e){
			this.setImageResource(R.drawable.empty);
		}
		System.gc();
		this.invalidate();
	}
}
