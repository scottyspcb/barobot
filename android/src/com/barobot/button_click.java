package com.barobot;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
public class button_click implements OnClickListener{

	@Override
	public void onClick(View v) {
		queue q = queue.getInstance();
		switch (v.getId()) {
		case R.id.set_x_1000:
			q.send("SET Z MIN");
			q.send("SET X " + ((-1000) * virtualComponents.mnoznikx));
			break;
		case R.id.set_x_100:
			q.send("SET Z MIN");
			q.send("SET X " + ((-100) * virtualComponents.mnoznikx));
			break;
		case R.id.set_x_10:
			q.send("SET Z MIN");
			q.send("SET X " + ((-10) * virtualComponents.mnoznikx));
			break;
		case R.id.set_x_1:
			q.send("SET Z MIN");
			q.send("SET X 0");
			break;
		case R.id.set_x10:
			q.send("SET Z MIN");
			q.send("SET X +" + ((10) * virtualComponents.mnoznikx));
			break;
		case R.id.set_x100:
			q.send("SET Z MIN");			
			q.send("SET X +" + ((100) * virtualComponents.mnoznikx));
			break;
		case R.id.set_x1000:
			q.send("SET Z MIN");
			q.send("SET X +" + ((1000) * virtualComponents.mnoznikx));
			break;
  
		case R.id.set_y_600:
			q.send("SET Z MIN");
			q.send("SET Y " + ((-1000) * virtualComponents.mnoznikx));
			break;
		case R.id.set_y_100:
			q.send("SET Z MIN");
			q.send("SET Y " + ((-100) * virtualComponents.mnoznikx));
			break;
		case R.id.set_y_10:
			q.send("SET Z MIN");
			q.send("SET Y " + ((-10) * virtualComponents.mnozniky));
			break;
		case R.id.set_y_0:
			q.send("SET Z MIN");
			q.send("SET Y 0");
			break;
		case R.id.set_y10:
			q.send("SET Z MIN");
			q.send("SET Y +" + ((10) * virtualComponents.mnozniky));
			break;
		case R.id.set_y100:
			q.send("SET Z MIN");
			q.send("SET Y +" + ((100) * virtualComponents.mnozniky));
			break;
		case R.id.set_y600:
			q.send("SET Z MIN");
			q.send("SET Y +" + ((1000) * virtualComponents.mnozniky));
			break;          
		case R.id.kalibrujx:
			//q.send("SET Z MIN");
			//q.send("SET Y " + virtualComponents.getInt("NEUTRAL_POS_Y", virtualComponents.neutral_pos_y ) );
			//q.send("KALIBRUJX");

			q.send("SET Z MIN");
			q.send("SET Y " + virtualComponents.getInt("NEUTRAL_POS_Y", virtualComponents.neutral_pos_y ) );
			long lengthx1	=  virtualComponents.getInt("LENGTHX", 600 ) * 10;
			q.send("SET X -" + lengthx1 );
			q.send("SET X +" + lengthx1 );
			q.send("SET X 0");
			break;
		case R.id.set_neutral_y:
			String posy2		=  virtualComponents.get("POSY", "0" );
			virtualComponents.set("NEUTRAL_POS_Y", posy2 );
			DebugWindow bb2 = DebugWindow.getInstance();
			if(bb2!=null){
				Toast.makeText(bb2, "To jest pozycja bezpieczna ("+posy2+")...", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.kalibrujy:
			q.send("SET Z MIN");
			long lengthy1	=  virtualComponents.getInt("LENGTHY", 600 )  * 10;
			q.send("SET Y -" + lengthy1 );
			q.send("SET Y +" + lengthy1 );
			q.send("SET Y 0");
			break;
		case R.id.kalibrujz:
			q.send("KALIBRUJZ");
			break;
		case R.id.machajx:
			q.add("SET Z MIN", true );
			q.add("SET Y " + virtualComponents.getInt("NEUTRAL_POS_Y", virtualComponents.neutral_pos_y ), false );
			long lengthx4	=  virtualComponents.getInt("LENGTHX", 600 );
			for( int i =0; i<10;i++){
				q.add("SET X " + (lengthx4/4), false );
				q.add("SET X " + (lengthx4/4 * 3) , false );
				q.add("WAIT READY" , true );
			}
			//q.add("MACHAJX", false );
			q.send();
			break;
		case R.id.machajy:
			q.send("SET Z MIN");
			q.send("MACHAJY");
			break;
		case R.id.machajz:
			q.send("MACHAJZ");
			break;
		case R.id.losujx:
			q.send("SET Z MIN");
			q.send("SET Y " + virtualComponents.getInt("NEUTRAL_POS_Y", virtualComponents.neutral_pos_y ) );
			q.send("LOSUJX");
			break;
		case R.id.losujy:
			q.send("SET Z MIN");
			q.send("LOSUJY");
			break;
		case R.id.losujz:
			q.send("LOSUJZ");
			break;
		case R.id.glweight:
			q.send("GET GLASS");
			break;
		case R.id.bottweight:
			q.send("GET WEIGHT");
			break;
		case R.id.fill5000:
			//virtualComponents.nalej( 5000 );

			q.add("ENABLEX", true);
			q.add("WAIT TIME 5005", true);
			q.add("ENABLEZ", true);
			q.add("WAIT TIME 5004", true);
			q.add("ENABLEZ", true);
			q.add("WAIT TIME 5003", true);
			q.add("DISABLEX", true);
			q.add("WAIT TIME 5002", true);
		    q.add("DISABLEY", true);
		    q.add("WAIT TIME 5001", true);
		    q.add("DISABLEZ", true);
		    q.add("WAIT TIME 4006", true);
		    q.add("GET CARRET", false);
		    q.send();

			break;
		case R.id.set_bottle:
			// przełącz okno na listę butelek, 
			// zablokuj przyciski i po naciśnięciu ustaw w tym miejscu butelkę
			Constant.log(Constant.TAG,"wybierz butelkę...");
			
			DebugWindow bb = DebugWindow.getInstance();
			if(bb!=null){
				String posx		=  virtualComponents.get("POSX", "0" );	
				String posy		=  virtualComponents.get("POSY", "0" );
				Constant.log(Constant.TAG,"wybierz butelkę3...");
				Toast.makeText(bb, "Wybierz butelkę do zapisania pozycji " + posx + "/" + posy, Toast.LENGTH_LONG).show();
				bb.tabHost.setCurrentTabByTag("tab0");
				bb.tabHost.bringToFront();
				bb.tabHost.setEnabled(true);
			}else{
				Constant.log(Constant.TAG,"BRAK DebugWindow 345");
			}
			break;
		case R.id.max_z:
			if(virtualComponents.need_glass_up){
				q.add("WAIT GLASS " + virtualComponents.weigh_min_diff, true);
			}
			q.add("ENABLEX", true);
			q.add("ENABLEY", true);
			q.add("ENABLEZ", true);		
			q.add("SET Z MAX", true);
			q.add("DISABLEX", true);
			q.add("DISABLEY", true);
			q.add("DISABLEZ", true);
			q.add("GET CARRET", true);
			q.send();
			break;
		case R.id.min_z:
			q.add("ENABLEX", true);
			q.add("ENABLEY", true);
			q.add("ENABLEZ", true);	
			q.add("SET Z MIN", true);
			q.add("DISABLEX", true);
			q.add("DISABLEY", true);
			q.add("DISABLEZ", true);
			q.add("GET CARRET", true);
			q.send();
			break;

		case R.id.max_x:
			q.send("SET Z MIN");
			long lengthx5	=  virtualComponents.getInt("LENGTHX", 1600 );
			q.send("SET X +" + lengthx5);
			break;
		case R.id.max_y:
			q.send("SET Z MIN");
			long lengthy2	=  virtualComponents.getInt("LENGTHY", 600 );
			q.send("SET Y +" + lengthy2 );			
			break;
		case R.id.min_x:
			q.send("SET Z MIN");
			long lengthx3	=  virtualComponents.getInt("LENGTHY", 600 );
			q.send("SET X -" + lengthx3 );
			break;
		case R.id.min_y:
			q.send("SET Z MIN");
			long lengthy3	=  virtualComponents.getInt("LENGTHY", 600 );
			q.send("SET Y -" + lengthy3 );
			break;	

		case R.id.length_x:
			q.send("SET Z MIN");
			// do obecnej pozycji dodaj różnicę do końca
			long lengthx	=  virtualComponents.getInt("LENGTHX", 1600);
			long posx		=  virtualComponents.getInt("POSX", 55);	
			if( lengthx > 0 ){
				long target = lengthx - posx;
				q.send("SET X +" + target);
			}
			break;	
		case R.id.length_x2:
			q.send("SET Z MIN");
			// od obecnej pozycji odejmij tą pozycję (powinno zajechac do 0)
			q.send("SET X -" + virtualComponents.getInt("POSY", 0 ));
			break;		

		case R.id.length_y:
			q.send("SET Z MIN");
			// do obecnej pozycji dodaj różnicę do konkońca
			long lengthy	=  virtualComponents.getInt("LENGTHY", 600 );
			long posy		=  virtualComponents.getInt("POSY", 0 );	
			if( lengthy > 0 ){
				long target = lengthy - posy;
				q.send("SET Y +" + target);
			}
			break;	
		case R.id.length_y2:
			q.send("SET Z MIN");
			// od obecnej pozycji odejmij tą pozycję (powinno zajechac do 0)
			q.send("SET Y -" + virtualComponents.getInt("POSY", 0 ));
			break;					
	   }
	}
}
