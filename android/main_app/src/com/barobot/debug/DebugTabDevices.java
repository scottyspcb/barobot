package com.barobot.debug;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.barobot.AppInvoker;
import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.hardware.virtualComponents;

public class DebugTabDevices extends Fragment {
	public int tab_id	= -1 ;
	private Activity cc;
	
    public DebugTabDevices(Activity debugActivity, int tabCommandsId) {
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
        super.onActivityCreated(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		int lay = DebugActivity.layouts[tab_id];
		View rootView = inflater.inflate( lay, container, false);
		

		int[] buttons = {
				R.id.editb0,
				R.id.editb1,
				R.id.editb2,
				R.id.editb3,
				R.id.editb4,		
				R.id.editb5,
				R.id.editb6,
				R.id.editb7,		
				R.id.editb8,
				R.id.editb9,
				R.id.editb10,		
				R.id.editb11		
		};

		for(int i=0;i<12;i++){
			int xpos = virtualComponents.getInt("BOTTLE_X_" + i, 0 );
			//int ypos = virtualComponents.getInt("BOTTLE_Y_" + i, 0 );
			EditText editb0 = (EditText) rootView.findViewById( buttons[i] );
			editb0.setText(""+xpos );
			final int num = i;
			editb0.addTextChangedListener(new TextWatcher() {
		           public void afterTextChanged(Editable s) {
		       // 	   Initiator.logger.i("DebugTabDevices.zapisuje", "BOTTLE_X_" + num + "= " +s.toString() );
		        	   virtualComponents.set("BOTTLE_X_" + num, s.toString() ); 
		           }
		           public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		           public void onTextChanged(CharSequence s, int start, int before, int count) {}
		        });
		}
		return rootView;
	}
}
