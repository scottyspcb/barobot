package com.barobot.debug;

import com.barobot.DebugActivity;
import com.barobot.R;
import com.barobot.R.id;
import com.barobot.hardware.ArduinoQueue;
import com.barobot.hardware.rpc_message;
import com.barobot.utils.Arduino;
import com.barobot.utils.Constant;
import com.barobot.utils.History_item;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class DebugTabLog extends Fragment {
	public int tab_id	= -1 ;
	private Activity cc;
    private ListView mConversationView;
    private ArrayAdapter<History_item> mConversation;

    public DebugTabLog(Activity debugActivity, int tabCommandsId) {
    	Constant.log("DebugTabLog", "init");
    	this.tab_id = tabCommandsId;
    	this.cc		= debugActivity;
    	mConversation = Arduino.getInstance().mConversationArrayAdapter;

	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
    	Constant.log("DebugTabLog", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		Constant.log("DebugTabLog", "onCreateView");

		int lay = DebugActivity.layouts[tab_id];
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		View rootView = inflater.inflate( lay, container, false);
		Button xb1 = (Button) rootView.findViewById(R.id.clear_history);
		final DebugTabLog dbw = this;
		xb1.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				Arduino.getInstance().clearHistory();
			};			
		});
	    mConversationView = (ListView) rootView.findViewById(R.id.led_list);
	    mConversationView.setAdapter(mConversation);
		return rootView;
	}
}