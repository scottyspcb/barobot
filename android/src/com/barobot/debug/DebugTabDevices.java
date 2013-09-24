package com.barobot.debug;

import com.barobot.DebugActivity;
import com.barobot.R;
import com.barobot.hardware.DeviceSet;
import com.barobot.utils.Constant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class DebugTabDevices extends Fragment {
	public int tab_id	= -1 ;
	private Activity cc;
	
    public DebugTabDevices(Activity debugActivity, int tabCommandsId) {
    	Constant.log("DebugTabDevices", "init");
    	this.tab_id = tabCommandsId;
    	this.cc=debugActivity;
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		//Integer.toString(getArguments().getInt(DebugActivity.ARG_SECTION_NUMBER))
    	Constant.log("DebugTabDevices", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
 
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		Constant.log("DebugTabDevices", "onCreateView");

		int lay = DebugActivity.layouts[tab_id];
		//View rootView = inflater.inflate( R.layout.fragment_device_list_dummy, container, false);
		View rootView = inflater.inflate( lay, container, false);

		ListView device_list_box = (ListView) rootView.findViewById(R.id.device_list);
		if( device_list_box == null){
			Constant.log("DebugTabDevices", "null2");
		}

		//DeviceSet ss = DeviceSet.byFeature("IS_RGBW_LED").add("IS_LED");
		DeviceSet ss = DeviceSet.getAll();
	    DeviceView adapter_listy = new DeviceView(this.cc, ss);

	    device_list_box.setAdapter(adapter_listy);
	    device_list_box.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> arg0, View arg1, int pos,long arg3) {
	        	Constant.log("click1",""+ pos);
	        	Constant.log("click2", arg0.getClass().getName());
	        }
	    });
	    /*
	    device_list_box.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				Constant.log("scroll","first: "+ firstVisibleItem );
				Constant.log("scroll","totalItemCount"+ totalItemCount );
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				Constant.log("onScrollStateChanged",""+ scrollState );
			}
	    });
*/
		return rootView;
	}
}
