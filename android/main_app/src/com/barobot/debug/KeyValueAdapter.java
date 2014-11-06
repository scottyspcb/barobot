package com.barobot.debug;
import java.util.LinkedHashMap;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.barobot.R;

public class KeyValueAdapter extends BaseAdapter {
	
	private LinkedHashMap<String, String> mData = null;
    private String[] mKeys;
	private Context context;
 
    public KeyValueAdapter(Context ctx, LinkedHashMap<String, String> data){
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

        View lView = convertView;
        if (lView == null) {
            lView =  LayoutInflater.from(this.context).inflate(R.layout.item_list_pair, parent, false);
        }
        TextView lName = (TextView) lView.findViewById(R.id.pair_key);
        TextView lCompany = (TextView) lView.findViewById(R.id.pair_value);

        lName.setText(key);
        lCompany.setText(value);
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

