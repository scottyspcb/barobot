package com.barobot.hardware;
import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.barobot.R;
import com.barobot.constant.Constant;

import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.Context;

public class DeviceView extends BaseAdapter {
    private Context ctx;
    private DeviceSet data;
    private ViewHolderPattern[] list;
	private int layoutId;
 
    public DeviceView(Context ctx, DeviceSet deviceSet, int layoutId) {
	    this.data		= deviceSet;
    	this.ctx		= ctx;
    	this.layoutId	= layoutId;
    	list			= new ViewHolderPattern[ this.data.getCount()];
    }
	public void changeSet(DeviceSet deviceSet) {
	    this.data	= deviceSet;
	    this.notifyDataSetChanged();
	    this.notifyDataSetInvalidated();
    	list		= new ViewHolderPattern[ this.data.getCount()];	
	}
    public int getCount() {
    	return this.data.getCount();
    }
 
    public Object getItem(int position) {
    	return list[position];
    }
 
    public long getItemId(int position) {
    	Constant.log("DeviceView", "getItemId " +position);
    	return 0;
    }
 
    private class ViewHolderPattern {
    	public TextView tv;
    	public SeekBar progress;
    	public View row;
    	public int value = 0;
		public Device device;
    }

    public ViewHolderPattern createRow(final int position, ViewGroup parent) {
    	Device dd				= this.data.getItem(position);
    	LayoutInflater inflater	= (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View convertView		= inflater.inflate( this.layoutId, parent, false);
    	ViewHolderPattern item	= new ViewHolderPattern();
    	item.row				= convertView;
    	item.tv					= (TextView) convertView.findViewById(R.id.textView_item_custom);
    	item.device				= dd;
    	item.value				= position;
    	//
    	if(layoutId ==  R.layout.device){
    		ToggleButton b = (ToggleButton) convertView.findViewById(R.id.device_active);
    		b.setChecked(dd.is_active);
    		b.setOnCheckedChangeListener( new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
 					ViewHolderPattern view_holder = (ViewHolderPattern) getItem(position);
 					Constant.log("change at", ""+position+ " to: "+isChecked); 					
 					view_holder.device.setActive( isChecked );
				}
     		});
    	}else if(layoutId==R.layout.device_led){
        	item.progress			= (SeekBar) convertView.findViewById(R.id.pos_bar);
        	item.progress.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener(){
     			@Override
     			public void onProgressChanged(SeekBar seekBar, int newProgress,	boolean fromUser) {
     				if(fromUser){
     					ViewHolderPattern view_holder = (ViewHolderPattern) getItem(position);
     					Constant.log("change at", ""+position+ " to: "+newProgress);
     					view_holder.value = newProgress;
     				}
     			}
     			@Override
     			public void onStartTrackingTouch(SeekBar seekBar) {}
     			@Override
     			public void onStopTrackingTouch(SeekBar seekBar) {}
     		});
    	}
    	return item;
    } 
 
    public View getView(final int position, View convertView, ViewGroup parent) {
    	if(list[position] == null){
    		list[position] = createRow( position, parent);
    //		Constant.log("rysuje nowe", ""+position);
	    }else{
	 //   	Constant.log("rysuje stare", ""+position);   	
	    }
    	String text = ""+  (position+1) +". "+ list[ position ].device.getName();
    	
    	list[ position ].tv.setText(text);
    	if(layoutId ==  R.layout.device){
    	}else if(layoutId==R.layout.device_led){
    		list[ position ].progress.setProgress( list[ position ].value);		// odpala tez onProgressChanged
    	}
	    return list[position].row;
    }

}
