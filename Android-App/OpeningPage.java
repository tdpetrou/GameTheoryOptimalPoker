package com.tdp.coolp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class OpeningPage extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_opening_page);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.opening_page, menu);
		return true;
	}
	
	public void BeginPlay(View v){
		Intent intent = new Intent();
		intent.setClass(this, MainActivity.class);		
		this.startActivity(intent);
	}

}
