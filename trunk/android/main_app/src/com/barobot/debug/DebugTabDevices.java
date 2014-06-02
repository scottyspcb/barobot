package com.barobot.debug;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.barobot.AppInvoker;
import com.barobot.R;
import com.barobot.activity.DebugActivity;
import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.hardware.Arduino;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.I2C_Device_Imp;

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
		final BarobotConnector barobot = Arduino.getInstance().barobot;
		final View rootView = inflater.inflate( lay, container, false);
		final int[] buttons = {
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

		final int[] capacityBox = {
				R.id.capacity0,
				R.id.capacity1,
				R.id.capacity2,
				R.id.capacity3,
				R.id.capacity4,		
				R.id.capacity5,
				R.id.capacity6,
				R.id.capacity7,		
				R.id.capacity8,
				R.id.capacity9,
				R.id.capacity10,
				R.id.capacity11	
		};

		int ds = barobot.state.getInt("DRIVER_X_SPEED", Constant.DRIVER_X_SPEED );
		int dcs = barobot.state.getInt("DRIVER_CALIB_X_SPEED", Constant.DRIVER_CALIB_X_SPEED );

		int srt = barobot.state.getInt("SERVOY_REPEAT_TIME", Constant.SERVOY_REPEAT_TIME );	
		int sptw = barobot.state.getInt("SERVOZ_PAC_TIME_WAIT", Constant.SERVOZ_PAC_TIME_WAIT );
		
		
		Initiator.logger.i("DebugTabDevices.", "DRIVER_X_SPEED= " +ds );
	
		final EditText editSX = (EditText) rootView.findViewById( R.id.edit_speed_x );
		final EditText editbCSX = (EditText) rootView.findViewById( R.id.edit_cal_speed_x );
		final EditText editbSRT = (EditText) rootView.findViewById( R.id.edit_repeat_time );
		final EditText editbSPTW = (EditText) rootView.findViewById( R.id.edit_pac_time_wait );

		editSX.setText(""+ds );
		editbCSX.setText(""+dcs );
		editbSRT.setText(""+srt );
		editbSPTW.setText(""+sptw );
		
		
		Button xb1 = (Button) rootView.findViewById(R.id.save_poss);
		xb1.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				for(int i=0;i<12;i++){
					EditText editb0 = (EditText) rootView.findViewById( buttons[i] );
					Initiator.logger.i("DebugTabDevices.zapisuje", "BOTTLE_OFFSETX_" + i + "= " +editb0.getText().toString() );
					int a = Integer.parseInt(editb0.getText().toString());
					barobot.setSlotMarginX(i, a );

					EditText cap0 = (EditText) rootView.findViewById( capacityBox[i] );
					int cap = Integer.parseInt(cap0.getText().toString());
					barobot.setCapacity(i, cap);
				}

				int ds = Integer.parseInt(editSX.getText().toString());
				int dcs = Integer.parseInt(editbCSX.getText().toString());
				int srt = Integer.parseInt(editbSRT.getText().toString());
				int sptw = Integer.parseInt(editbSPTW.getText().toString());

				barobot.state.set("DRIVER_X_SPEED", ds);
				barobot.state.set("DRIVER_CALIB_X_SPEED", dcs);
	
				barobot.state.set("SERVOY_REPEAT_TIME", srt);
				barobot.state.set("SERVOZ_PAC_TIME_WAIT", sptw);
			}
		});

		/*
		Button xb2 = (Button) rootView.findViewById(R.id.reset_margins);
		xb2.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				for(int i=0;i<12;i++){
					int xpos = BarobotConnector.margin_x[i];
					final EditText editb0 = (EditText) rootView.findViewById( buttons[i] );
					barobot.setSlotMarginX( i, xpos );
					editb0.setText(""+xpos );

					int cap = BarobotConnector.capacity[i];
					final EditText cap0 = (EditText) rootView.findViewById( capacityBox[i] );
					barobot.setCapacity( i, cap );
					cap0.setText(""+cap );	
				}	
			}
		});*/
/*
		Button xb20 = (Button) rootView.findViewById(R.id.load_poss);	
		xb20.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				for(int i=0;i<12;i++){
					int xpos = barobot.getSlotMarginX( i );
					final EditText editb0= (EditText) rootView.findViewById( buttons[i] );
					final int num			= i;
					Initiator.logger.i("DebugTabDevices", "set input BOTTLE_OFFSETX_" + num + "= " +xpos );
					editb0.setText(""+xpos );
					
					int cap =  barobot.getCapacity(num);
					final EditText cap0 = (EditText) rootView.findViewById( capacityBox[i] );
					cap0.setText(""+cap );

				}
			}
		});
*/
		for(int i=0;i<12;i++){
			int xpos = barobot.getSlotMarginX( i );
			EditText editb0 = (EditText) rootView.findViewById( buttons[i] );
			int num = i;
			Initiator.logger.i("DebugTabDevices", "set input BOTTLE_OFFSETX_" + num + "= " +xpos );
			editb0.setText(""+xpos );
			
			int cap =  barobot.getCapacity(num);
			final EditText cap0 = (EditText) rootView.findViewById( capacityBox[i] );
			cap0.setText(""+cap );
		}

		return rootView;
	}
}

/*
editb0.addTextChangedListener(new TextWatcher() {
       public void afterTextChanged(Editable s) {

       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
    });
*/