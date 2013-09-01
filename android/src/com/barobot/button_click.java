package com.barobot;

import java.util.Random;

import com.barobot.hardware.ArduinoQueue;
import com.barobot.hardware.virtualComponents;
import com.barobot.utils.Constant;
import com.barobot.utils.Arduino;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
public class button_click implements OnClickListener{
	private DebugWindow dbw;
	
	public button_click(DebugWindow debugWindow){
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
		case R.id.set_x_1:
			virtualComponents.moveZDown( q );
			q.add("SET X 0", true );
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
		case R.id.set_y_0:
			virtualComponents.moveZDown( q );
			q.add("SET Y 0", true );
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
				q.add("SET X " + (lengthx4/4), true );
				q.add("SET X " + (lengthx4/4 * 3) , true );
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
				q.add("SET Z MAX", true );
				q.add("SET Z MIN", true );
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
			ar.send("GET GLASS");
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
			dbw.tabHost.setCurrentTabByTag("tab0");
			dbw.tabHost.bringToFront();
			dbw.tabHost.setEnabled(true);

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

		case R.id.length_x:
			virtualComponents.moveZDown( q );
			// do obecnej pozycji dodaj różnicę do końca
			long lengthx	=  virtualComponents.getInt("LENGTHX", 1600);
			long posx3		=  virtualComponents.getInt("POSX", 55);	
			if( lengthx > 0 ){
				long target = lengthx - posx3;
				q.add("SET X +" + target, true);
			}
			ar.send(q);
			break;	
		case R.id.length_x2:
			virtualComponents.moveZDown( q );
			// od obecnej pozycji odejmij tą pozycję (powinno zajechac do 0)
			q.add("SET X -" + virtualComponents.getInt("POSY", 0 ), true);
			ar.send(q);
			break;		

		case R.id.length_y:
			virtualComponents.moveZDown( q );
			// do obecnej pozycji dodaj różnicę do konkońca
			long lengthy	=  virtualComponents.getInt("LENGTHY", 600 );
			long posy3		=  virtualComponents.getInt("POSY", 0 );	
			if( lengthy > 0 ){
				long target = lengthy - posy3;
				q.add("SET Y +" + target, true);
			}
			ar.send(q);
			break;	
		case R.id.length_y2:
			virtualComponents.moveZDown( q );
			// od obecnej pozycji odejmij tą pozycję (powinno zajechac do 0)
			q.add("SET Y -" + virtualComponents.getInt("POSY", 0 ), true);
			ar.send(q);
			break;	
		case R.id.unlock:
			ar.unlock();
			break;
		case R.id.clear_history:
			dbw.clearList();
			break;
		case R.id.pacpac:	
			virtualComponents.pacpac();
			break;
		case R.id.smile:
			BarobotMain.getInstance().cm.doPhoto();
			break;	
	   }
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
