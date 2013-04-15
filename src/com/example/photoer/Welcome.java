package com.example.photoer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Welcome extends Activity {
	String ftpurl,cameraurl;
	@Override
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		final TextView nameText = (TextView) findViewById(R.id.nameText);
		Button enter = (Button) findViewById(R.id.buttonenter);
		FileInputStream is;
		try {
			is = Welcome.this.openFileInput("URLS.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			ftpurl = (br.readLine());
			cameraurl = (br.readLine());
			is.close();
		} catch (FileNotFoundException e) {
			ftpurl = "192.168.1.108";
			cameraurl = "http://192.168.1.106";
			e.printStackTrace();
		} catch (IOException e) {
			ftpurl = "192.168.1.108";
			cameraurl = "http://192.168.1.106";
			e.printStackTrace();
		}
		enter.setOnClickListener(new Button.OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				final String name = nameText.getText().toString();
				if (name.equals("admin")) {
					final EditText a1 = new EditText(Welcome.this);
					final EditText a2 = new EditText(Welcome.this);
					a1.setText(ftpurl);
					a2.setText(cameraurl);
					new AlertDialog.Builder(Welcome.this).setTitle("FTP IP").setIcon(
						     android.R.drawable.ic_dialog_info).setView(a1)
						     .setPositiveButton("OK",  new OnClickListener(){

										@Override
										public void onClick(DialogInterface dialog,	int which) {
											ftpurl = a1.getText().toString();
										}} ).create().show();
					new AlertDialog.Builder(Welcome.this).setTitle("camra IP").setIcon(
						     android.R.drawable.ic_dialog_info).setView(a2)
						     .setPositiveButton("OK",  new OnClickListener(){

										@Override
										public void onClick(DialogInterface dialog,	int which) {
											cameraurl = a2.getText().toString();
										}} ).create().show();
					try {
						FileOutputStream os = Welcome.this.openFileOutput("URLS.txt",MODE_PRIVATE);
						BufferedWriter br = new BufferedWriter(new OutputStreamWriter(os));
						br.write(ftpurl+"\n");
						br.write(cameraurl+"\n");
						br.close();
						os.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					return;
				}
				if (!checkleagal(name)) {
				   new AlertDialog.Builder(Welcome.this).setTitle("消息").setMessage("用户名只能包括英文字母和数字")
				  .setNegativeButton("返回重填", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
					}} ).create().show();
				    return;
				}
				new AlertDialog.Builder(Welcome.this).setTitle("确认").setMessage("请确认用户名   "+name)
				.setPositiveButton("OK", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//start activity
						int rand = (new Random()).nextInt(1024768);
						Intent intent = new Intent();
						intent.setClass(Welcome.this, Camera.class);
						Bundle bundle = new Bundle();
						bundle.putString("uname",name);//+"."+String.valueOf(rand));
						bundle.putString("ftpurl", ftpurl);
						bundle.putString("cameraurl", cameraurl);
						
						intent.putExtras(bundle);
						startActivity(intent);
						Welcome.this.finish();
					}})
				.setNegativeButton("返回重填", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}}).create().show();
						
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome, menu);
		return true;
	}
	
	public boolean checkleagal(String s){
		return s.matches("[a-zA-Z0-9]+");

		
	}

}
