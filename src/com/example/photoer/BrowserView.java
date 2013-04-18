package com.example.photoer;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class BrowserView extends ImageView {

    private final static String ALBUM_PATH  
            = Environment.getExternalStorageDirectory() + "/download/";
    private MulitPointTouchListener listener;
	public BrowserView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		this.setOnTouchListener(listener = new MulitPointTouchListener(this));
	}
	public BrowserView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnTouchListener(listener = new MulitPointTouchListener(this));
	}
	public BrowserView(Context context) {
		super(context);
		this.setOnTouchListener(listener = new MulitPointTouchListener(this));
	}
	public void setOwner(Browser owner) {
		this.owner = owner;
	}
	private Browser owner;
	public void setImage(String imagename) {
		if (null==imagename){
			this.setImageResource(R.drawable.empty);
			return;
		}
	    //File dirFile = new File(ALBUM_PATH);
		Bitmap bmp = null;
		try{
		File file = new File(ALBUM_PATH+imagename);
		if (!file.exists()) {
			imagename = imagename.substring(0,imagename.indexOf(".jpg"))+".print.jpg";
		}
		System.out.println(ALBUM_PATH+imagename);
		System.gc();
		bmp = BitmapFactory.decodeFile(ALBUM_PATH+imagename);

		owner.setcurrentFilename(imagename);
		owner.setswitchButton();
		this.setImageBitmap(bmp);
		}
		catch (Exception e){
			this.setImageResource(R.drawable.empty);
		}
		//minScaleR;

		DisplayMetrics dm = new DisplayMetrics();
		try {
        ((Activity)this.getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        float minScaleR = Math.min(
                (float) dm.widthPixels / (float) bmp.getWidth(),
                (float) dm.heightPixels / (float) bmp.getHeight());
		listener.reset(minScaleR);
		} catch (Exception e){
			
		}
		System.gc();
		this.invalidate();
	}
}
