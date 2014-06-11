package com.barobot.activity;

import com.barobot.R;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BarobotActivity extends Activity {
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	 //   MenuInflater inflater = getMenuInflater();
	 //   inflater.inflate(R.menu.option_menu, menu);
	    return true;
    }
	
	public void onMenuButtonClicked(View view)
	{
		Intent serverIntent = null;
		switch(view.getId())
		{
		/*
		case R.id.menu_favorite:
			serverIntent = new Intent(this, BarobotMain.class);
			serverIntent.putExtra(BarobotMain.MODE_NAME, BarobotMain.Mode.Favorite.ordinal());
			
			serverIntent.putExtra("Test", "Test2");
			break;
			*/
		case R.id.menu_choose:
			serverIntent = new Intent(this, BarobotMain.class);
			serverIntent.putExtra(BarobotMain.MODE_NAME, BarobotMain.Mode.Normal.ordinal());
			break;

		case R.id.menu_lucky:
			serverIntent = new Intent(this, BarobotMain.class);
			serverIntent.putExtra(BarobotMain.MODE_NAME, BarobotMain.Mode.Random.ordinal());
			break;	
		case R.id.menu_create:
			serverIntent = new Intent(this, CreatorActivity.class);
			break;
			
		case R.id.menu_options:
			serverIntent = new Intent(this, OptionsActivity.class);
			break;
		}
		
		if(serverIntent!=null){
    		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		startActivity(serverIntent);
    	}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent serverIntent = null;
    	BarobotConnector barobot = Arduino.getInstance().barobot;

    	switch (item.getItemId()) {
    	case R.id.action_panic:
    		barobot.cancel_all();
    		return false;
    	case R.id.action_bottles:
    		serverIntent = new Intent(this, BottleSetupActivity.class);
    		break;
    	case R.id.action_creator:
    		serverIntent = new Intent(this, CreatorActivity.class);
    		break;
    	case R.id.action_recipes:
    		serverIntent = new Intent(this, RecipeSetupActivity.class);
    		break;
    	case R.id.secure_connect_scan:
    		if( Arduino.getInstance().checkBT() == false ){
    			Toast.makeText(this, "Bluetooth jest niedostępny", Toast.LENGTH_LONG).show();
    			finish();
    		}
    		serverIntent = new Intent(this, BTListActivity.class);
    		break;
    		
       	case R.id.kalibracja:

       		barobot.kalibrcja();
    		break;	

    	case R.id.about_item:
    		serverIntent = new Intent(this, AboutActivity.class);
    		break;   
    		
    	case R.id.unlock_menu:
    		runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				Button makeButton = (Button) findViewById(R.id.make_button);
    				if(makeButton!=null){
    					makeButton.setEnabled(true);
    				}
    			}
    		});
    		Arduino.getMainQ().unlock();
    		break;    		
    		
    	case R.id.menu_debug_window:
    		serverIntent = new Intent(this, DebugActivity.class);
    		break;
    	case R.id.menu_settings:
    	//	serverIntent = new Intent(this, MainSettingsActivity.class);
    		break;
    	}
    	if(serverIntent!=null){
    		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		startActivity(serverIntent);
    	}
    	return false;
    }
    
    protected void setTextViewText(String text, int id){
		TextView tView = (TextView) findViewById(id);
		tView.setText(text);
	}
   
    protected String getTextViewText(int id)
    {
    	TextView tView = (TextView) findViewById(id);
    	return tView.getText().toString();
    }
    
    protected void ButtonEnabled(boolean enabled, int id)
	{
		Button okButton = (Button) findViewById(id);
		okButton.setEnabled(enabled);
	}
}
