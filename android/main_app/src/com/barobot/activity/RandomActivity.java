package com.barobot.activity;

import java.io.IOException;
import java.io.InputStream;

import com.barobot.R;
import com.barobot.R.id;
import com.barobot.R.layout;
import com.barobot.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class RandomActivity extends BarobotActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_random);
		ImageView iw= (ImageView)findViewById(R.id.dice1);  
		//	InputStream ims = getAssets().open("dice.png");
			/* 
			int resID = getResources().getIdentifier("dice.png", "drawable",  getPackageName());
			iw.setImageResource(resID);
	*/
			try {
			    // get input stream
			    InputStream ims = getAssets().open("dice.png");
			    // load image as Drawable
			    Drawable d = Drawable.createFromStream(ims, null);
			    // set image to ImageView
			    iw.setImageDrawable(d);
			}
			catch(IOException ex) {
			}
			final Intent serverIntent = new Intent(this, BarobotMain.class);
			serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			iw.setOnClickListener( new OnClickListener() {		
				@Override
				public void onClick(View v) {
					startActivity(serverIntent);
				}
			});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}
