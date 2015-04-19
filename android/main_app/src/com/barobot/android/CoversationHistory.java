package com.barobot.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.widget.ArrayAdapter;
import com.barobot.parser.message.History_item;

public class CoversationHistory<T> {
	public List<T> mConversationHistory = new ArrayList<T>();
    public boolean log_active	= true;
	private ArrayAdapter<T> mConversation;
	private Activity mainView;

    CoversationHistory(Activity mainView){
		this.mainView = mainView;
    }
	public void addToList(final T m) {
		if(log_active){
			synchronized (mConversationHistory) {
				mConversationHistory.add( m );
			}
			if(this.mConversation !=null){
				this.mainView.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mConversation !=null){
				 			mConversation.add(m);
				 			mConversation.notifyDataSetChanged();
						}
					}
				});
			}
		}
	}
/*
	public void addToList(final String string, final boolean direction ) {
		if(log_active){
			this.mainView.runOnUiThread(new Runnable() {
			     public void run() {
			//    	Log.i(Constant.TAG, "addtohist:[" + string +"]"); 
			    	T hi = new T( string.trim(), direction);
			    	
					synchronized (mConversationHistory) {
						mConversationHistory.add( hi );
					}
			 		if(mConversation !=null){
			 			mConversation.add(hi);
			 			mConversation.notifyDataSetChanged();
					} 
			    }
			});
		}
	}*/
	public void clearHistory() {
		if(log_active){
			synchronized (mConversationHistory) {
				mConversationHistory.clear();
			}
			if(this.mConversation !=null){
				this.mConversation.clear();
			}
		}
	}
	//public List<History_item> getHistory(){
	//	return mConversationHistory;
	//}
	public void getHistory(ArrayAdapter<T> mConversation) {
		this.mConversation = mConversation;
		this.mConversation.addAll(mConversationHistory);
	}
}

