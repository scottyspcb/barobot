package com.barobot.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.barobot.DebugActivity;
import com.barobot.R;
import com.barobot.hardware.DeviceSet;
import com.barobot.hardware.DeviceView;
import com.barobot.utils.Constant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;

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

		final ListView device_list_box = (ListView) rootView.findViewById(R.id.device_list);
		if( device_list_box == null){
			Constant.log("DebugTabDevices", "null2");
		}

		// show device list width filter (spinner)
		Spinner spinner		= (Spinner) rootView.findViewById(R.id.device_filter);
		Set<String> keys	= DeviceSet.getFeatures();
		List<String> lst	= new ArrayList<String>();
		lst.add("");
		lst.addAll(keys);
		java.util.Collections.sort(lst);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.cc, android.R.layout.simple_spinner_item, lst);
		//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.planets_array, android.R.layout.simple_spinner_item);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// Specify the layout to use when the list of choices appears
		spinner.setAdapter(adapter);		// Apply the adapter to the spinner
		DeviceSet deviceSet				= DeviceSet.getAll();
	    final DeviceView adapter_listy	= new DeviceView(this.cc, deviceSet,  R.layout.device );
	    device_list_box.setAdapter(adapter_listy);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		    	String selected		= parent.getItemAtPosition(pos).toString();
		    	DeviceView a		= adapter_listy;
		    	if("".equals(selected)){
		    		DeviceSet deviceSet		= DeviceSet.getAll();
		    		a.changeSet(deviceSet);
		    	}else{
		    		DeviceSet deviceSet		= DeviceSet.byFeature(selected);
		    		a.changeSet(deviceSet);
		    	}
		    	device_list_box.invalidate();
		    	device_list_box.refreshDrawableState();
		    }
		    public void onNothingSelected(AdapterView<?> parent) {}
		   });

	    device_list_box.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> arg0, View arg1, int pos,long arg3) {
	        	Constant.log("click1",""+ pos);
	        	Constant.log("click2", arg0.getClass().getName());
	        }
	    });
		return rootView;
	}
}
