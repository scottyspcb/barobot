package com.barobot.activity;

import com.barobot.R;
import com.barobot.gui.MainActivity;
import com.barobot.gui.OutsideComponentActivity;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Arduino;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BarobotActivity extends Activity {
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.option_menu, menu);
	    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent serverIntent = null;
    	switch (item.getItemId()) {
    	case R.id.action_panic:
    		virtualComponents.cancel_all();
    		return false;
    	case R.id.main_feature:
    		serverIntent = new Intent(this, MainActivity.class);
    		break;
    	case R.id.action_bottles:
    		serverIntent = new Intent(this, BottleSetupActivity.class);
    		break;
    	case R.id.action_product:
    		serverIntent = new Intent(this, ProductActivity.class);
    		break;
    	case R.id.action_outside_components:
    		serverIntent = new Intent(this, OutsideComponentActivity.class);
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

       		virtualComponents.kalibrcja();
    		break;	

    	case R.id.update_drinks:
    		serverIntent = new Intent(this, UpdateActivity.class);
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
    		Arduino.getInstance().unlock();
    		break;    		
    		
    	case R.id.menu_debug_window:
    		serverIntent = new Intent(this, DebugActivity.class);
    		break;
    	case R.id.menu_settings:
    		serverIntent = new Intent(this, MainSettingsActivity.class);
    		break;
    	}
    	if(serverIntent!=null){
    		serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		startActivity(serverIntent);
    	}
    	return false;
    }
    
    protected void setTextViewText(String text, int id)
	{
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
