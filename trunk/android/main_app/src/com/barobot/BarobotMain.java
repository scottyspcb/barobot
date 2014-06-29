package com.barobot;


import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;
import org.orman.sql.Query;

import com.barobot.gui.dataobjects.Language;
import com.barobot.gui.dataobjects.Recipe_t;
import com.barobot.gui.dataobjects.Slot;
import com.barobot.gui.dataobjects.Translated_name;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class BarobotMain extends Activity {
	private static BarobotMain instance;

	public static BarobotMain getInstance() {
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(instance == null){
			instance = this; // Set up the window layout
		}
		AppInvoker.createInstance(this);
		AppInvoker.getInstance().onCreate();
	}

    protected void setTextViewText(String text, int id){
		TextView tView = (TextView) findViewById(id);
		tView.setText(text);
	}

    protected void ButtonEnabled(boolean enabled, int id){
		Button okButton = (Button) findViewById(id);
		okButton.setEnabled(enabled);
	}
    
	@Override
	protected void onResume() {			// resume this activity
		super.onResume();
		AppInvoker.getInstance().onResume();
	}

	@Override
	protected void onStart() {			// start this activity
		super.onStart();
		AppInvoker.getInstance().onStart();
	}
}
/*
case R.id.menu_favorite:
	serverIntent = new Intent(this, BarobotMain.class);
	serverIntent.putExtra(BarobotMain.MODE_NAME, BarobotMain.Mode.Favorite.ordinal());
	serverIntent.putExtra("Test", "Test2");
	break;
	
case R.id.menu_choose:
	serverIntent = new Intent(this, RecipeListActivity.class);
	serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Normal.ordinal());
	break;

case R.id.menu_lucky:
	serverIntent = new Intent(this, RecipeListActivity.class);
	serverIntent.putExtra(RecipeListActivity.MODE_NAME, RecipeListActivity.Mode.Random.ordinal());
	break;	


if(serverIntent!=null){
	serverIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	startActivity(serverIntent);
}*/


/*
List<Translated_name> l = Model.fetchQuery(ModelQuery.select().from(Translated_name.class).where(
		C.and(
				C.eq("element_id", id),
				C.eq("table_name", table_name)
			)).getQuery(),Translated_name.class);

for( Translated_name tn : l){
	Log.e("translateName1", "translated " + tn.translated );
	Log.e("translateName1", "translated " + tn.language_id );
}
Log.e("translateName1", "------------------------");


Query query = new Query("SELECT * FROM Translated_name WHERE `element_id` ='"+id+"' and `table_name`='"+table_name+"' " + order);

Query query = ModelQuery.select().from(Translated_name.class).where(
		C.and(
				C.eq("element_id", id),
				C.eq("table_name", table_name)
			)).orderBy(order).getQuery();

List<Translated_name> l2 = Model.fetchQuery(query,Translated_name.class);

for( Translated_name tn : l2){
	Log.e("translateName2", "translated " + tn.translated );
	Log.e("translateName2", "translated " + tn.language_id );
}		
Log.e("translateName2", "------------------------");
*/

