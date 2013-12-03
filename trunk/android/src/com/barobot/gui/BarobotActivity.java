package com.barobot.gui;

import com.barobot.AboutActivity;
import com.barobot.BTListActivity;
import com.barobot.DebugActivity;
import com.barobot.MainSettingsActivity;
import com.barobot.R;
import com.barobot.UpdateActivity;
import com.barobot.utils.Arduino;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    	case R.id.main_feature:
    		serverIntent = new Intent(this, MainActivity.class);
    		break;
    	case R.id.action_bottles:
    		serverIntent = new Intent(this, BottleSetupActivity.class);
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
    	case R.id.update_drinks:
    		serverIntent = new Intent(this, UpdateActivity.class);
    		break;
    	case R.id.about_item:
    		serverIntent = new Intent(this, AboutActivity.class);
    		break;   
    	case R.id.menu_debug_window:
    		serverIntent = new Intent(this, DebugActivity.class);
    		break;
    	case R.id.menu_settings:
    		serverIntent = new Intent(this, MainSettingsActivity.class);
    		break;
    	}
    	startActivity(serverIntent);
    	return false;
    }
}
