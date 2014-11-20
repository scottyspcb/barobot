package com.barobot.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.other.ProgressTask;
import com.barobot.other.ProgressTask.UiTask;

public class BottleSetupActivity extends BarobotMain{
	private int[] ids;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bottle_setup);

		ids = new int[13];
		ids[1] = R.id.bottle1;
		ids[2] = R.id.bottle2;
		ids[3] = R.id.bottle3;
		ids[4] = R.id.bottle4;
		ids[5] = R.id.bottle5;
		ids[6] = R.id.bottle6;
		ids[7] = R.id.bottle7;
		ids[8] = R.id.bottle8;
		ids[9] = R.id.bottle9;
		ids[10] = R.id.bottle10;
		ids[11] = R.id.bottle11;
		ids[12] = R.id.bottle12;
		UpdateSlots();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    //Handle the back button
	    if(keyCode == KeyEvent.KEYCODE_BACK) {
	    	gotoMainMenu(null);
	        return true;
	    }
	    else {
	        return super.onKeyDown(keyCode, event);
	    }
	}

	public void gotoMainMenu(View view){
		UiTask tt = new UiTask() {
			@Override
			public void compute() {
				Engine engine = Engine.GetInstance();
			//	engine.invalidateData();
				engine.loadSlots();
				engine.getRecipes();
			}
			@Override
			public void close() {
				runOnUiThread(new Runnable() {
					  public void run() {
						finish();
						overridePendingTransition(R.anim.push_down_in,R.anim.push_down_out);
					  }
				});
			}
		};
		ProgressTask dd = new ProgressTask( this, tt );
		dd.execute();
	}

	private void UpdateSlots() {
		List<Slot> bottles = Engine.GetInstance().loadSlots();
		for(Slot bottle : bottles)
		{
			if (bottle.position > 0 && bottle.position <= ids.length )
			{
				TextView tview = (TextView) findViewById(ids[bottle.position]);
				if (bottle.status == Slot.STATUS_EMPTY) {
					tview.setText(R.string.empty_bottle_string);
				} else {
					tview.setText(bottle.getName());	
				}
			}
		}
	}

	public void onBottleClicked(View view)
	{
		int viewID = view.getId();
		int position = 0;
		for (int i = 1; i<=12 ; i++)
		{
			if (viewID == ids[i])
			{
				position = i;
				break;
			}
		}
		if (position != 0)
		{
			showProductSelectionActivity(position);
		}
		else
		{
			Log.w("BOTTLE_SETUP", "onBottleClicked called by an unknown view");
		}
	}
	
	void showProductSelectionActivity(int position)
	{
		Intent intent = new Intent(this, ProductActivity.class);
		intent.putExtra(ProductActivity.SLOT_NUMBER, position);
		startActivity(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		UpdateSlots();
	}
}
