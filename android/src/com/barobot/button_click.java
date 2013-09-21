package com.barobot;

import java.util.Random;

import com.barobot.drinks.RunnableWithData;
import com.barobot.hardware.ArduinoQueue;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Constant;
import com.barobot.utils.Arduino;
import com.barobot.webview.AJS;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
public class button_click implements OnClickListener{
	private Context dbw;
	
	public button_click(Context debugWindow){
		dbw = debugWindow;
	}

	@Override
	public void onClick(View v) {
		Arduino ar = Arduino.getInstance();
		ArduinoQueue q = new ArduinoQueue();

		switch (v.getId()) {
		case R.id.set_x_1000:
			virtualComponents.moveZDown( q );
			q.add("SET X " + ((-1000) * virtualComponents.mnoznikx), true);
			ar.send(q);
			break;
		case R.id.set_x_100:
			virtualComponents.moveZDown( q );
			q.add("SET X " + ((-100) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_x_10:
			virtualComponents.moveZDown( q );
			q.add("SET X " + ((-10) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_x10:
			virtualComponents.moveZDown( q );
			q.add("SET X +" + ((10) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_x100:
			virtualComponents.moveZDown( q );
			q.add("SET X +" + ((100) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_x1000:
			virtualComponents.moveZDown( q );
			q.add("SET X +" + ((1000) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;  
		case R.id.set_y_600:
			virtualComponents.moveZDown( q );
			q.add("SET Y " + ((-1000) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_y_100:
			virtualComponents.moveZDown( q );
			q.add("SET Y " + ((-100) * virtualComponents.mnoznikx), true );
			ar.send(q);
			break;
		case R.id.set_y_10:
			virtualComponents.moveZDown( q );
			q.add("SET Y " + ((-10) * virtualComponents.mnozniky), true );
			ar.send(q);
			break;
		case R.id.set_y10:
			virtualComponents.moveZDown( q );
			q.add("SET Y +" + ((10) * virtualComponents.mnozniky), true );
			ar.send(q);
			break;
		case R.id.set_y100:
			virtualComponents.moveZDown( q );
			q.add("SET Y +" + ((100) * virtualComponents.mnozniky), true );
			ar.send(q);
			break;
		case R.id.set_y600:
			virtualComponents.moveZDown( q );
			q.add("SET Y +" + ((1000) * virtualComponents.mnozniky), true );
			ar.send(q);
			break;
		case R.id.kalibrujx:
			virtualComponents.moveZDown( q );
			q.add("SET Y " + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			long lengthx1	=  virtualComponents.getInt("LENGTHX", 600 ) * 10;
			q.add("SET X -" + lengthx1, true );
			q.add("SET X +" + lengthx1, true );
			q.add("SET X 0", true);
			ar.send(q);
			break;
		case R.id.set_neutral_y:
			String posy2		=  virtualComponents.get("POSY", "0" );
			virtualComponents.set("NEUTRAL_POS_Y", ""+posy2 );
			String nn = virtualComponents.get("NEUTRAL_POS_Y", "0" );
			Toast.makeText(dbw, "To jest pozycja bezpieczna ("+nn+")...", Toast.LENGTH_LONG).show();
			break;
		case R.id.goToNeutralY:
			q.add("SET Y " + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			ar.send(q);
			break;
		case R.id.kalibrujy:
			this.setSpeed();		// jeśli mam szklankę to bardzo wolno
			virtualComponents.moveZDown( q );
			long lengthy1	=  virtualComponents.getInt("LENGTHY", 600 )  * 100;
			q.add("SET Y -" + lengthy1, true );
			q.add("SET Y +" + lengthy1, true );
			q.add("SET Y 0", true);
			ar.send(q);
			break;
		case R.id.kalibrujz:
			virtualComponents.moveZDown( q );
			ar.send(q);
			break;
		case R.id.machajx:
			virtualComponents.moveZDown( q );
			q.add("SET Y " + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			long lengthx4	=  virtualComponents.getInt("LENGTHX", 600 );
			for( int i =0; i<10;i++){
				//q.add("SET X " + (lengthx4/4), true );
				//q.add("SET X " + (lengthx4/4 * 3) , true );
				q.add("SET X 0", true );
				q.add("SET X " + (lengthx4) , true );
			}
			ar.send(q);
			break;
		case R.id.machajy:
			virtualComponents.moveZDown( q );
			long lengthy4	=  virtualComponents.getInt("LENGTHY", 600 );
			for( int i =0; i<10;i++){
				q.add("SET Y " + (lengthy4/4), true );
				q.add("SET Y " + (lengthy4/4 * 3) , true );
			}
			ar.send(q);
			break;
		case R.id.machajz:
			q.add("ENABLEZ", true);
			for( int i =0; i<10;i++){
				q.add("SET Z MAX", true);		// SET Z zwraca początek operacji a nie koniec
				q.addWait( virtualComponents.SERVOZ_UP_TIME );	// wiec trzeba poczekać
				q.add("SET Z MIN", true);		// SET Z zwraca początek operacji a nie koniec
				q.addWait( virtualComponents.SERVOZ_DOWN_TIME );	// wiec trzeba poczekać
			}
			q.add("DISABLEZ", true);
			ar.send(q);
			break;
		case R.id.losujx:
			Random generator2 = new Random( 19580427 );
			virtualComponents.moveZDown( q );
			q.add("SET Y " + virtualComponents.get("NEUTRAL_POS_Y", "0" ), true );
			long lengthx5	=  virtualComponents.getInt("LENGTHX", 600 );
		    for(int f = 0;f<20;){
		    	int left = generator2.nextInt((int)(lengthx5/100 / 2));
		    	int right =generator2.nextInt((int)(lengthx5/100 / 2));
		    	right+= lengthx5/100 / 2;
				q.add("SET X " + (left * 100), true );
				q.add("SET X " + (right * 100), true );
		        f=f+2;
		      }
		    ar.send(q);
			break;
		case R.id.losujy:
			Random generator3 = new Random( 19580427 );
			virtualComponents.moveZDown( q );
			long lengthy5	=  virtualComponents.getInt("LENGTHY", 600 );
		    for(int f = 0;f<20;){
		    	int left = generator3.nextInt((int)(lengthy5/100 / 2));
		    	int right =generator3.nextInt((int)(lengthy5/100));
		    	right+= lengthy5/100 / 2;
				q.add("SET Y " + (left * 100), true );
				q.add("SET Y " + (right * 100), true );
		        f=f+2;
		    }
		    ar.send(q);
			break;
		case R.id.glweight:
			ar.send("GET A " + virtualComponents.ANALOG_WAGA);
			break;
		case R.id.bottweight:
			ar.send("GET WEIGHT");
			break;
		case R.id.fill5000:
			virtualComponents.nalej( 3000 );
			break;
		case R.id.set_bottle:
			// przełącz okno na listę butelek, 
			// zablokuj przyciski i po naciśnięciu ustaw w tym miejscu butelkę
			Constant.log(Constant.TAG,"wybierz butelkę...");
			String posx		=  virtualComponents.get("POSX", "0" );	
			String posy		=  virtualComponents.get("POSY", "0" );
			Constant.log(Constant.TAG,"wybierz butelkę3...");
			Toast.makeText(dbw, "Wybierz butelkę do zapisania pozycji " + posx + "/" + posy, Toast.LENGTH_LONG).show();
//			dbw.tabHost.setCurrentTabByTag("tab0");
//			dbw.tabHost.bringToFront();
//			dbw.tabHost.setEnabled(true);

			break;
		case R.id.max_z:
			if(virtualComponents.need_glass_up){
				q.addWaitGlass();
			}
			q.add("ENABLEX", true);
			q.add("ENABLEY", true);
			q.add("ENABLEZ", true);		
			q.add("SET Z MAX", true);
			q.add("DISABLEX", true);
			q.add("DISABLEY", true);
			q.add("DISABLEZ", true);
			q.add("GET CARRET", true);
			ar.send(q);
			break;
		case R.id.min_z:
			q.add("ENABLEX", true);
			q.add("ENABLEY", true);
			q.add("ENABLEZ", true);	
			virtualComponents.moveZDown( q );
			q.add("DISABLEX", true);
			q.add("DISABLEY", true);
			q.add("DISABLEZ", true);
			q.add("GET CARRET", true);
			ar.send(q);
			break;

		case R.id.max_x:
			virtualComponents.moveZDown( q );
			long lengthx2	=  virtualComponents.getInt("LENGTHX", 600 );
			ar.send("SET X +" + lengthx2 );			
			break;

		case R.id.max_y:
			virtualComponents.moveZDown( q );
			long lengthy2	=  virtualComponents.getInt("LENGTHY", 600 );
			ar.send("SET Y +" + lengthy2 );			
			break;
		case R.id.min_x:
			virtualComponents.moveZDown( q );
			long lengthx3	=  virtualComponents.getInt("LENGTHY", 600 );
			ar.send("SET X -" + lengthx3 );
			break;
		case R.id.min_y:
			virtualComponents.moveZDown( q );
			long lengthy3	=  virtualComponents.getInt("LENGTHY", 600 );
			ar.send("SET Y -" + lengthy3 );
			break;	
		case R.id.unlock:
			ar.unlock();
			break;
		case R.id.pacpac:	
			virtualComponents.pacpac();
			break;
		case R.id.smile:
			BarobotMain.getInstance().cm.doPhoto();
			break;
		case R.id.graph_random:
	  	  	ToggleButton tb			= (ToggleButton) v;
	  	  	boolean isChecked		= tb.isChecked();
			if(isChecked){
				int graph_source5	= virtualComponents.graph_source;
				virtualComponents.disable_analog(ar, graph_source5 );
				int graph_speed2 = virtualComponents.graph_speed;
				AJS.getInstance().runJs("show_random", ""+graph_speed2);	
			}else{
				int graph_speed2 = virtualComponents.graph_speed;
				AJS.getInstance().runJs("show_random", "false");
			}

			break;

		case R.id.graph_source:
			int graph_source	= virtualComponents.graph_source;
			show_dialog( new RunnableWithData(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					virtualComponents.graph_source		= Integer.parseInt(this.data);	
					Arduino ar							= Arduino.getInstance();
					virtualComponents.enable_analog(ar, virtualComponents.graph_source, virtualComponents.graph_speed, virtualComponents.graph_repeat);	
				}
        	}, ""+graph_source);

			/*
        	deviceManager dm = deviceManager.getInstance();
        	dm.getDevices( devices.DEVICE_ANALOG );
        	dm.getDevices( devices.DEVICE_ULTRA );
           	live_analog_num		= 20;
        	.getDevices( devices.DEVICE_ANALOG );
        	virtualComponents.getDevices( ANALOG );
        	device.liveEnable();
*/
			break;
		case R.id.graph_speed:
			show_dialog( new RunnableWithData(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					virtualComponents.graph_speed		= Integer.parseInt(this.data);
					Arduino ar							= Arduino.getInstance();
					virtualComponents.enable_analog(ar, virtualComponents.graph_source, virtualComponents.graph_speed, virtualComponents.graph_repeat);
				}
        	}, ""+virtualComponents.graph_speed);

			break;
		case R.id.graph_repeat:	
			show_dialog( new RunnableWithData(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					virtualComponents.graph_repeat	= Integer.parseInt(this.data);
					Arduino ar						= Arduino.getInstance();
		        	virtualComponents.enable_analog(ar, virtualComponents.graph_source, virtualComponents.graph_speed, virtualComponents.graph_repeat);	
				}
        	}, ""+virtualComponents.graph_repeat);
			break;
		case R.id.graph_xsize:
			show_dialog( new RunnableWithData(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					virtualComponents.graph_xsize		= Integer.parseInt(this.data);
					AJS.getInstance().runJs("changex", ""+virtualComponents.graph_xsize);
				}
        	}, ""+virtualComponents.graph_speed);
			break;
		case R.id.graph_active:
	  	  	ToggleButton tb2		= (ToggleButton) v;
	  	  	boolean isChecked2		= tb2.isChecked();
			if(isChecked2){
				int graph_source9	= virtualComponents.graph_source;
				virtualComponents.disable_analog(ar, graph_source9 );
				AJS.getInstance().runJs("show_random", "false");
			}
			break;
		case R.id.graph_fps:
			show_dialog( new RunnableWithData(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					virtualComponents.graph_fps		= Integer.parseInt(this.data);
					AJS.getInstance().runJs("changefps", ""+virtualComponents.graph_fps);
				}
        	}, ""+virtualComponents.graph_fps);
			break;
		case R.id.graph_reverse:
			AJS.getInstance().runJs("reverseY");
			break;
		case R.id.graph_scale:
			AJS.getInstance().runJs("toggleLocalMin");
			break;
		case R.id.graph_points:
			AJS.getInstance().runJs("dots");
			break;
		case R.id.graph_lines:
			AJS.getInstance().runJs("lines");
			break;
		case R.id.graph_columns:
			AJS.getInstance().runJs("column");
			break;
		case R.id.graph_high_speed:
			AJS.getInstance().runJs("sethighspeed");
			break;
	   }
	}

	 public void show_dialog( final RunnableWithData onfinish, String defaultValue ){
		// custom dialog
		final Dialog dialog = new Dialog(dbw);
		dialog.setContentView(R.layout.dialog_int);
		dialog.setTitle("Podaj...");

		// set the custom dialog components - text, image and button
		final EditText text = (EditText) dialog.findViewById(R.id.dialog_int_input);
		text.setText(defaultValue);

		Button dialogButton = (Button) dialog.findViewById(R.id.dialog_int_dialogButtonOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String res =  text.getText().toString();
				onfinish.sendData(res);
				onfinish.run();
				dialog.dismiss();
			}
		});
		dialog.show();
	 }

	private void setSpeed() {
		if( virtualComponents.hasGlass() ){
			this.setSpeed( virtualComponents.WITHGLASS );
		}else{
			this.setSpeed( virtualComponents.WITHOUTGLASS );
		}
	}

	private void setSpeed(int withglass) {
//		Arduino ar = Arduino.getInstance();
/*
		q.add("SET SPPEDX 400", true);
		q.add("SET SPPEDY 400", true);	
		q.add("SET SPPEDX 400", true);
		q.add("SET SPPEDY 400", true);
	*/	
	}
}
