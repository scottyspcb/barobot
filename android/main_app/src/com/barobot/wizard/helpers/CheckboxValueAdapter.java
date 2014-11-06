package com.barobot.wizard.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.barobot.R;

public class CheckboxValueAdapter extends BaseAdapter {
	private SystemUnitTest mData = null;
	private Context context;
 
    public CheckboxValueAdapter(Context ctx, SystemUnitTest analog_list){
        mData  			= analog_list;
        analog_list.checkAllOk();
        this.context	= ctx;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        SystemTestItem<?> item	= (SystemTestItem<?>) getItem(pos);
        boolean result			= item.getResult();
        String value			= item.getValue();
        String title			= item.getTitle();

        LinearLayout lView		= (LinearLayout) convertView;
        if (lView == null) {
            lView =  (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.item_list_check, parent, false);
        }

        CheckBox lName	= (CheckBox) lView.findViewById(R.id.pair_key);
        TextView lValue	= (TextView) lView.findViewById(R.id.pair_value);
        lName.setText(title);

        if( result ){
        	lName.setChecked(true);
        //	lValue.setBackgroundColor( 0xff002200 );
        //	lName.setBackgroundColor( 0xff002200 );
        	lView.setBackgroundColor( 0xff006600 );
        }else{
        	lName.setChecked(false);
        //	lValue.setBackgroundColor( 0xff220000 );
        //	lName.setBackgroundColor( 0xffff0000 );
        	lView.setBackgroundColor( 0xff660000 );
        }
        lName.setClickable(false);
        lValue.setText(value);
        return lView;
    }

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		super.notifyDataSetInvalidated();
	}
}
