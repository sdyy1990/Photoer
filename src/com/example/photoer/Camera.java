package com.example.photoer;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Camera extends Activity {
	private ImageView buttonShot,buttonLeft,buttonRight,buttonUp,buttonDown;
	private static int MOTION_LEFT = 1;
	private static int MOTION_RIGHT = 2;
	private static int MOTION_UP = 3;
	private static int MOTION_DOWN = 4; 
	private String cameraurl,ftpurl,uname;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		buttonShot = (ImageView) findViewById(R.id.shotView);
		buttonLeft = (ImageView) findViewById(R.id.leftView);
		buttonRight = (ImageView) findViewById(R.id.rightView);
		buttonUp = (ImageView) findViewById(R.id.upView);
		buttonDown = (ImageView) findViewById(R.id.downView);
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
	private void TriggerCameraMove(int MODE){
		
	}
	private void TriggerShot() {
		
	}
}
