package com.barobot;

import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.message.History_item;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DebugInputActivity extends Activity {

    private ListView mConversationView;
    private ArrayAdapter<History_item> mConversation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_input);
		
	//	mConversation						= new ArrayAdapter<History_item>(this, R.layout.message);
		mConversation						= new ArrayAdapter<History_item>(this, android.R.layout.simple_list_item_1);
		

		Button debug_input_clear_history	= (Button) findViewById(R.id.debug_input_clear_history);
		Button debug_input_unlock_log		= (Button) findViewById(R.id.debug_input_unlock_log);
		ToggleButton debug_input_enable		= (ToggleButton) findViewById(R.id.debug_input_enable);
		Button debug_input_close			= (Button) findViewById(R.id.debug_input_close);
		ListView debug_input_list			= (ListView) findViewById(R.id.debug_input_list);


	//	Arduino.getInstance().getHistory( mConversation );
		
		debug_input_clear_history.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				clearHistory();
			};			
		});
	
		debug_input_enable.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				ToggleButton tb			= (ToggleButton) v;
		  	  	boolean isChecked		= tb.isChecked();
		  	  	enableLogs( isChecked );
			};			
		});	
		debug_input_unlock_log.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				BarobotConnector barobot = Arduino.getInstance().barobot;
				barobot.main_queue.unlock();
			};
		});

		debug_input_close.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				enableLogs( false );
				finish();
			};
		});
	    mConversationView = (ListView) debug_input_list;
	    mConversationView.setAdapter(mConversation);
		debug_input_enable.setChecked(true);
	}


	protected void enableLogs(boolean b) {
		// TODO Auto-generated method stub
		
		
		
	}

	protected void clearHistory() {
		mConversation.clear();
	}
}
