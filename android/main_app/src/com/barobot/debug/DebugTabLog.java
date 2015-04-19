package com.barobot.debug;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.parser.message.History_item;

public class DebugTabLog extends Fragment {
	public int tab_id	= -1 ;
	private Activity cc;
    private ListView mConversationView;
    private ArrayAdapter<History_item> mConversation;

    public DebugTabLog(Activity debugActivity, int tabCommandsId) {
   // 	Initiator.logger.i("DebugTabLog", "init");
    	this.tab_id = tabCommandsId;
    	this.cc		= debugActivity;
    	mConversation = new ArrayAdapter<History_item>(cc, R.layout.message);
    	//Arduino.getInstance().getHistory( mConversation );
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		int lay = DebugActivity.layouts[tab_id];
		View rootView = inflater.inflate( lay, container, false);
		

		return rootView;
	}

}