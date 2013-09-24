package com.barobot.debug;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import com.barobot.R;
import com.barobot.hardware.Device;
import com.barobot.hardware.DeviceSet;
import com.barobot.utils.Constant;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.Context;

public class DeviceView extends BaseAdapter {
    private Context ctx;
    private DeviceSet data;
    private ViewHolderPattern[] list;
 
    public DeviceView(Context ctx, DeviceSet ss) {
	    this.data	= ss;
    	this.ctx	= ctx;
    	list		= new ViewHolderPattern[ this.data.getCount()];
    }
 
    public int getCount() {
    	return this.data.getCount();
    }
 
    public Object getItem(int position) {
    	Constant.log("DeviceView", "getItem " +position);
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
    	View convertView		= inflater.inflate(R.layout.device, parent, false);
    	ViewHolderPattern item	= new ViewHolderPattern();
    	item.row				= convertView;
    	item.tv					= (TextView) convertView.findViewById(R.id.textView_item_custom);
    	item.progress			= (SeekBar) convertView.findViewById(R.id.pos_bar);
    	item.device				= dd;
    	item.value				= position;
        Constant.log("DeviceView", "create " + position);
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
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
		}});
        
        
        
    	return item;
    } 
 
    public View getView(final int position, View convertView, ViewGroup parent) {
    	if(list[position] == null){
    		list[position] = createRow( position, parent);
	    } else {
	    	Constant.log("DeviceView", "no create " +"/"+position+ " value:" + list[ position ].value);
	    }
    	list[ position ].tv.setText(list[ position ].device.typeName +" / "+ list[ position ].device.name);
        list[ position ].progress.setProgress( list[ position ].value);		// odpala tez onProgressChanged

	    return list[position].row;
    }
}
