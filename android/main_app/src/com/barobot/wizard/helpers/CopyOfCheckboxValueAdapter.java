package com.barobot.wizard.helpers;

import java.util.LinkedHashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.barobot.R;

public class CopyOfCheckboxValueAdapter extends BaseAdapter {
	private LinkedHashMap<String, String> mData = null;
    private String[] mKeys;
	private Context context;
 
    public CopyOfCheckboxValueAdapter(Context ctx, LinkedHashMap<String, String> data){
        mData  			= data;
        this.context	= ctx;
        mKeys 			= mData.keySet().toArray(new String[mData.size()]);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(mKeys[position]);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        String key = mKeys[pos];
        String value = getItem(pos).toString();
        LinearLayout lView = (LinearLayout) convertView;
        if (lView == null) {
            lView =  (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.item_list_check, parent, false);
        }
        CheckBox lName	= (CheckBox) lView.findViewById(R.id.pair_key);
        TextView lValue	= (TextView) lView.findViewById(R.id.pair_value);
        lName.setText(key);
        
        if( pos%2 == 0 ){
        	lName.setChecked(false);
        //	lValue.setBackgroundColor( 0xff220000 );
        //	lName.setBackgroundColor( 0xffff0000 );
        	lView.setBackgroundColor( 0xff660000 );
        }else{
        	lName.setChecked(true);
        //	lValue.setBackgroundColor( 0xff002200 );
        //	lName.setBackgroundColor( 0xff002200 );
        	lView.setBackgroundColor( 0xff006600 );
        }
        lName.setClickable(false);
        lValue.setText(value);
        return lView;
    }

	@Override
	public void notifyDataSetChanged() {
	    mKeys = mData.keySet().toArray(new String[mData.size()]);
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
        mKeys = mData.keySet().toArray(new String[mData.size()]);
		super.notifyDataSetInvalidated();
	}

}
