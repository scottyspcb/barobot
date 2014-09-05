package com.barobot.hardware.devices;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.hardware.devices.BarobotEventListener;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.interfaces.RetReader;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;
import com.barobot.parser.utils.GlobalMatch;

public class MyRetReader implements RetReader {
	private BarobotConnector barobot;
	private HardwareState state;
	private BarobotEventListener bel;

	public MyRetReader( BarobotEventListener bel, BarobotConnector brb ){
		this.barobot	= brb;
		this.state		= brb.state;
		this.bel		= bel;
		final MyRetReader mrr	= this;
		
		barobot.mb.addGlobalRegex( new GlobalMatch(){
			@Override
			public String getMatchRet() {
				return "^Rx\\d+$";
			}
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
				Initiator.logger.i("Arduino.GlobalMatch.RX", fromArduino);
				fromArduino = fromArduino.replace("Rx", "");
				int hpos = Decoder.toInt(fromArduino);
				barobot.driver_x.setHPos( hpos );	
				return true;
			}
			@Override
			public String getMatchCommand() {
				return null;		// all
			}
		} );
		barobot.mb.addGlobalRegex( new GlobalMatch(){
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
			//	Initiator.logger.i("Arduino.GlobalMatch.METHOD_IMPORTANT_ANALOG", fromArduino);
				mrr.importantAnalog(asyncDevice, wait_for, fromArduino, false );
				return true;
			}
			@Override
			public String getMatchRet() {
				return "^" +  Methods.METHOD_IMPORTANT_ANALOG + ",.*";
			}
			@Override
			public String getMatchCommand() {
				return null;		// all
			}
		} );


		barobot.mb.addGlobalRegex( new GlobalMatch(){
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
			//	Initiator.logger.i("Arduino.GlobalMatch.RETURN_PIN_VALUE", fromArduino);
				//{METHOD_I2C_SLAVEMSG,my_address, RETURN_PIN_VALUE, pin,value}
				int[] parts = Decoder.decodeBytes( fromArduino );
				short my_address	= (short) parts[1];
				short pin			= (short) parts[3];
				short value			= (short) parts[4];
				Initiator.logger.i("Arduino.POKE-BUTTON", "Address:" + my_address + ", pin: " + pin+ ", value: " + value );
				Queue q = barobot.main_queue;
				if(value > 0 ){
					q.add("L"+my_address+",ff,0", true);
				}else{
					q.add("L"+my_address+",ff,200", true);
				}
				return true;
			}
			@Override
			public String getMatchRet() {
				return "^" +  Methods.METHOD_I2C_SLAVEMSG + ",\\d+," + Methods.RETURN_PIN_VALUE + ",.*";
			}
			@Override
			public String getMatchCommand() {
				return null;		// all
			}
		} );
		barobot.mb.addGlobalRegex( new GlobalMatch(){		// METHOD_DEVICE_FOUND
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {				
				String decoded = "Arduino.GlobalMatch.METHOD_DEVICE_FOUND";
				int[] parts = Decoder.decodeBytes( fromArduino );
				// short ttt[4] = {METHOD_DEVICE_FOUND,addr,type,ver};
				// short ttt[4] = {METHOD_DEVICE_FOUND,I2C_ADR_MAINBOARD,MAINBOARD_DEVICE_TYPE,MAINBOARD_VERSION};
				boolean scanning = true;
				if( scanning ){
		/*
					short pos = getResetOrder(buffer[1]);
					i2c_reset_next( buffer[1], false );       // reset next (next to slave)
					i2c_reset_next( buffer[1], true );
					}else if( scann_order ){ 
						if( pos == 0xff ){        // nie ma na liscie?
							order[nextpos++]  = buffer[1];            // na tm miejscu slave o tym adresie
						}else{
							scann_order  =  false;
						}
					}
				*/
				}
				if(parts[2] == Constant.MAINBOARD_DEVICE_TYPE ){
					decoded += "/MAINBOARD_DEVICE_TYPE";
					int cx		= barobot.driver_x.getSPos();
					barobot.driver_x.setM(cx);	// ostatnia znana pozycja jest marginesem
					state.set("MARGINX", cx);
					Queue mq = barobot.main_queue;
					mq.clear();
				}else if(parts[2] == Constant.UPANEL_DEVICE_TYPE ){		// upaneld
					decoded += "/UPANEL_DEVICE_TYPE";
				}else if(parts[2] == Constant.IPANEL_DEVICE_TYPE ){		// wozek
					decoded += "/IPANEL_DEVICE_TYPE";
				}
				Initiator.logger.i("MyRetReader.decoded", decoded);
				return true;
			}
			@Override
			public String getMatchRet() {
				return "^" +  Methods.METHOD_DEVICE_FOUND + ",.*";
			}
			@Override
			public String getMatchCommand() {
				return null;
			}
		} );

		barobot.mb.addGlobalRegex(  new GlobalMatch(){		// METHOD_TEST_SLAVE
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
				Initiator.logger.i("Arduino.GlobalMatch.METHOD_TEST_SLAVE", fromArduino);
				return true;
			}
			@Override
			public String getMatchRet() {
				return "^" +  Methods.METHOD_TEST_SLAVE + ",.*";
			}
			@Override
			public String getMatchCommand() {
				return null;
			}
		} );

		
	}

	@Override
	public boolean isRetOf(Mainboard asyncDevice,
			AsyncMessage wait_for2, String fromArduino, Queue mainQueue ) {

		String command = "";
		if(wait_for2!= null && wait_for2.command != null && wait_for2.command != "" ){
			command = wait_for2.command;
		}
	//	Log.w("MyRetReader.isRetOf", fromArduino+ " for "+ command );

		if(wait_for2!= null && wait_for2.command != null && wait_for2.command != "" ){
			if( fromArduino.startsWith( "R" + wait_for2.command )){
				return true;
			}
			if( fromArduino.startsWith( "E" + wait_for2.command)){		// error tez odblokowuje
				return true;
			}
			if( fromArduino.startsWith("RX") && wait_for2.command.startsWith("x") ){
				return true;
			}
		}
	
	//	RESET3	RRESET3
	//	RB		RRB
	//	RB2		RRB2
	//	TEST	RTEST
	//	T		RT94

		String decoded = "";
		if( fromArduino.startsWith( "" + Methods.METHOD_I2C_SLAVEMSG) ){
			decoded += "METHOD_I2C_SLAVEMSG";
			int[] parts = Decoder.decodeBytes( fromArduino );
			String retLike = fromArduino;
			if( parts[2] == Methods.METHOD_GET_X_POS ){
				decoded += "/METHOD_GET_X_POS";
				int hpos = parts[3] + (parts[4] << 8);
				//BarobotCommandResult
				barobot.driver_x.setHPos( hpos );	
				if(command.equals("x")){
					return true;
				}else{
					return false;
				}
				
			}else if( parts[2] == Methods.METHOD_GET_Y_POS ){
				decoded += "/METHOD_GET_Y_POS";
				int pos = parts[3] + (parts[4] << 8); 
				Initiator.logger.i("new pos y:", ""+pos );
				state.set( "POSY",""+pos);
				if(command.equals("y")){
					return true;
				}else{
					return false;
				}
				
			}else if( parts[2] == Methods.METHOD_GET_Z_POS ){
				decoded += "/METHOD_GET_Z_POS";
				int pos = parts[3] + (parts[4] << 8); 
				state.set( "POSZ",""+pos);
				if(command.equals("z")){
					return true;
				}else{
					return false;
				}
			}else if( parts[2] == Methods.METHOD_DRIVER_DISABLE ){
				decoded += "/METHOD_DRIVER_DISABLE";
				if( parts[3] == Methods.DRIVER_X){
					decoded += "/DRIVER_X";
					retLike = "DX";
				}else if( parts[3] == Methods.DRIVER_Y){
					decoded += "/DRIVER_Y";
					retLike = "DY";
				}else if( parts[3] == Methods.DRIVER_Z){
					decoded += "/DRIVER_Z";
					retLike = "DZ";
				}else{
					decoded += "/???";
					Initiator.logger.i("MyRetReader.decoded", decoded);
					return false;
				}
				if(command.equals(retLike)){		// DX, DY, DZ
					return true;
				}else{
					Initiator.logger.e("MyRetReader.decoded.wrong", command + ", expected:" + retLike+ " because of " + decoded );
					return false;
				}
				
			}else if( parts[2] == Methods.METHOD_DRIVER_ENABLE ){
				decoded += "/METHOD_DRIVER_ENABLE";
				if( parts[3] == Methods.DRIVER_X){
					decoded += "/DRIVER_X";
					retLike = "REX";
				}else if( parts[3] == Methods.DRIVER_Y){
					decoded += "/DRIVER_Y";
					retLike = "REY";
				}else if( parts[3] == Methods.DRIVER_Z){
					decoded += "/DRIVER_Z";
					retLike = "REZ";
				}else{
					decoded += "/???";
					Initiator.logger.i("MyRetReader.decoded", decoded);
					return false;
				}
				if(command.startsWith("E")){		// EX, EY, EZ
					return true;
				}else{
					Initiator.logger.e("MyRetReader.decoded.wrong", command + ", expected output:" + "E..." + " fromArduino:'"+ fromArduino+"'" );
					return false;
				}
			}else if( parts[2] == Methods.RETURN_DRIVER_READY || parts[2] == Methods.RETURN_DRIVER_READY_REPEAT){
				if(parts[2] == Methods.RETURN_DRIVER_READY){
					decoded += "/RETURN_DRIVER_READY";
				}else{
					decoded += "/RETURN_DRIVER_READY_REPEAT";
				}
				if( parts[3] == Methods.DRIVER_X){
					decoded += "/DRIVER_X";
					//int pos = parts[4] + (parts[5] << 8) + (parts[6] << 16 + (parts[7] << 24));
				//	short hpos = (short)parts[7] + (short)(parts[6] << 8);
					short hpos = (short) (parts[6] << 8);
					hpos += (short)parts[7];
					barobot.driver_x.setHPos( hpos );
					if(command.startsWith("X")){
						Initiator.logger.i("MyRetReader.decoded OK", decoded);
						return true;
					}else{
						Initiator.logger.e("MyRetReader.decoded.wrong", command + ", expected output:" + "X..." + " because of " + decoded + " fromArduino:'"+ fromArduino+"'" );
					}
				}else if( parts[3] == Methods.DRIVER_Y){
					int pos = parts[4] + (parts[5] << 8);
					decoded += "/DRIVER_Y";
					state.set( "POSY", pos );
					if(command.startsWith("Y")){
						Initiator.logger.i("MyRetReader.decoded OK", decoded);
						return true;
					}else{
						
						Initiator.logger.e("MyRetReader.decoded.wrong", command + ", expected output:" + "Y..." + " because of " + decoded + " fromArduino:'"+ fromArduino+"'" );
					}
				}else if( parts[3] == Methods.DRIVER_Z){
					int pos = parts[4] + (parts[5] << 8);
					decoded += "/DRIVER_Z";
					state.set( "POSZ",pos);
					if(command.startsWith("Z")){
						Initiator.logger.i("MyRetReader.decoded OK", decoded);
						return true;
					}else{
						Initiator.logger.e("MyRetReader.decoded.wrong", command + ", expected output:" + "Z..." + " because of " + decoded + " fromArduino:'"+ fromArduino+"'" );
					}
				}else{
					decoded += "/???";
					Initiator.logger.i("MyRetReader.decoded", decoded);
					return false;
				}

			}else{
				Initiator.logger.e("MyRetReader", "no METHOD_I2C_SLAVEMSG => false(fromArduino: '"+ fromArduino +"')");
				return false;
			}
			//Initiator.logger.i("MyRetReader.retLike", retLike);
			Initiator.logger.i("MyRetReader.decoded", decoded);
			return false;
		}else if(fromArduino.startsWith(""+Methods.RETURN_PIN_VALUE) ){
			return false;
		}else if(fromArduino.startsWith(""+Methods.METHOD_CHECK_NEXT) ){
			decoded += "/METHOD_CHECK_NEXT";

		}else if(fromArduino.startsWith(""+Constant.RET) ){		// na końcu bo to może odblokować wysyłanie i spowodować zapętlenie
			decoded += "/RET";
			String fromArduino2 = fromArduino.substring(1);
			if(fromArduino2.startsWith(Constant.GETXPOS)){
				decoded += "/GETXPOS";
				String fromArduino3 = fromArduino2.replace(Constant.GETXPOS, "");	
				int hpos = Decoder.toInt(fromArduino3);	// hardware pos
				barobot.driver_x.setHPos( hpos );
			}else if(fromArduino2.startsWith(Constant.GETYPOS)){
				decoded += "/GETYPOS";
				
				String fromArduino3 = fromArduino2.replace(Constant.GETYPOS, "");
				state.set( "POSY",fromArduino3);

			}else if(fromArduino2.startsWith(Constant.GETZPOS)){
				decoded += "/GETZPOS";
				String fromArduino3 = fromArduino2.replace(Constant.GETZPOS, "");
				state.set( "POSZ",fromArduino3);
			}else{
				decoded += "/????";
			}

		}else if( fromArduino.startsWith( "" + Methods.METHOD_IMPORTANT_ANALOG) ){		// msg od slave		
			Initiator.logger.i("MyRetReader.decoded checkInput", decoded);
			if( command.startsWith("A")){
				return importantAnalog(asyncDevice, wait_for2, fromArduino, true );
			}else{
				return false;
			}

		}else if( fromArduino.startsWith( "" + Methods.RETURN_I2C_ERROR) ){
			decoded += "/RETURN_I2C_ERROR";
			// short ttt[4] = {RETURN_I2C_ERROR,my_address, deviceAddress,length, command }
			// Urządzenie 'my_address' wysyłało do 'deviceAddress' bajtów length
			barobot.main_queue.unlock();
			// todo, obsłużyc to lepiej

		}else if( fromArduino.startsWith( "" + Methods.METHOD_EXEC_ERROR) ){		// msg od slave		
			decoded += "/METHOD_EXEC_ERROR";
			int[] parts = Decoder.decodeBytes( fromArduino );
			String retLike = fromArduino;
			if( parts[3] == Methods.DRIVER_X){
				decoded += "/Rx";
				retLike = "Rx";
			}else if( parts[3] == Methods.DRIVER_Y){
				decoded += "/Ry";
				retLike = "Ry";
			}else if( parts[3] == Methods.DRIVER_Z){
				decoded += "/Rz";
				retLike = "Rz";
			}else{
				decoded += "/???";
				return false;
			}
		}
		if(!decoded.equals("")){
			Initiator.logger.i("MyRetReader.decoded", decoded);
		}
		return false;
	}

	private int state_num = 0;
	private int fromstart = 0;
	
	private int frontNum = 0;
	private int BackNum = 0;
	private int last_3 = 0;
	private int last_7 = 0;
	
	private boolean was_empty6 = false;
	private boolean was_empty4 = false;
	public boolean importantAnalog(Mainboard asyncDevice, AsyncMessage wait_for2, String fromArduino, boolean checkInput ) {
		String command = "";
		if(wait_for2!= null && wait_for2.command != null && wait_for2.command != "" ){
			command = wait_for2.command;
		}
		/* short ttt[8] = { METHOD_IMPORTANT_ANALOG, 
			INNER_HALL_X, 
			state_name, 
			0,					// dir is unknown on carret
			0, 					// position is unknown on carret
			0,  				// position is unknown on carret
			(value & 0xFF), (value >>8),};
		*/
		String decoded = "/METHOD_IMPORTANT_ANALOG";
		int[] parts = Decoder.decodeBytes( fromArduino );
		if( parts.length >= 10 && parts[1] == Methods.INNER_HALL_X ){
			decoded += "/INNER_HALL_X";
			/*
			if(dir == Methods.DRIVER_DIR_BACKWARD){
			}
			if(dir == Methods.DRIVER_DIR_FORWARD) {
				int ind	= virtualComponents.scann_num
				if(direction == Methods.DRIVER_DIR_BACKWARD){
					ind	= 11-virtualComponents.scann_num;
				}
				int num		= virtualComponents.magnet_order[ind];
				int ypos	= virtualComponents.b_pos_y[ num ];
				posx		= posx + virtualComponents.margin_x[num];
				if(direction == Methods.DRIVER_DIR_BACKWARD){
					virtualComponents.hereIsBottle(num, posx, ypos );	
				}
				Initiator.logger.i("input_parser "+ virtualComponents.scann_num+" "+ypos, "butelka "+num+": " + posx+ " / " + virtualComponents.margin_x[num] );
				virtualComponents.scann_num++;
			}	*/
		//	Initiator.logger.i("input_parser", "hardware pos: " + hpos );
		//	Initiator.logger.i("input_parser", "software pos: " + spos );
			int state_name	= parts[2];
			int dir			= parts[3];
		//	int hpos		= parts[7] + (parts[6] << 8) + (parts[5] << 16 + (parts[4] << 24));
			short hpos		= (short) (parts[6] << 8);
			hpos += (short)parts[7];
			int value		= parts[8] + (parts[9] << 8);
			int spos		= barobot.driver_x.hard2soft(hpos);
			decoded += "/@s:" + spos;
			decoded += "/@h:" + hpos;
			decoded += "/#" + value;
			barobot.driver_x.setHPos( hpos );

			if( state.getInt("scann_bottles", 0 ) > 0 && !checkInput && dir == Methods.DRIVER_DIR_FORWARD ){
				state_num++;
				if(state_name == Methods.HX_STATE_0 ){				// ERROR
					decoded += "/HX_STATE_0";
					state_num = 0;
				}else if(state_name == Methods.HX_STATE_1 ){
					decoded += "/HX_STATE_1";
					state.set( "LENGTHX", spos);
					state.set( "X_GLOBAL_MAX", spos );
					int SERVOY_FRONT_POS = state.getInt("SERVOY_FRONT_POS", 1000 );
					barobot.hereIsBottle(11, spos, SERVOY_FRONT_POS );
					state_num = 0;
				}else if(state_name == Methods.HX_STATE_2 ){
					decoded += "/HX_STATE_2";
					hereIsMagnet( 11, hpos, hpos+500, Constant.BOTTLE_IS_FRONT );
					frontNum = 0;
					BackNum = 0;
					last_3 = 0;
					was_empty6 = false;
					was_empty4 = false;

				}else if(state_name == Methods.HX_STATE_3 ){
					if(was_empty4){
						last_3  = hpos;
						was_empty4 = false;
						decoded += "/3 BOTTLE START";
					}else{
						decoded += "/HX_STATE_3";
						last_3 = 0;
					}
				}else if(state_name == Methods.HX_STATE_4 ){
					if(last_3 != 0 ){
						decoded += "/4 BOTTLE END";
						hereIsMagnet(fromstart, last_3, hpos, Constant.BOTTLE_IS_BACK );
						BackNum++;
						fromstart++;
						last_3 = 0;
					}else{
						decoded += "/HX_STATE_4";
						was_empty4 = true;
					}
				}else if(state_name == Methods.HX_STATE_5 ){
					decoded += "/HX_STATE_5";

				}else if(state_name == Methods.HX_STATE_6 ){
					if(last_7 != 0 ){
						decoded += "/6 BOTTLE END";
						hereIsMagnet(fromstart, last_7, hpos, Constant.BOTTLE_IS_FRONT );
						frontNum++;
						last_7 = 0;
						fromstart++;
					}else{
						decoded += "/HX_STATE_6";
						was_empty6 = true;
					}
				}else if(state_name == Methods.HX_STATE_7 ){
					if(was_empty6){
						last_7  = hpos;
						was_empty6 = false;
						decoded += "/7 BOTTLE START";
					}else{
						decoded += "/HX_STATE_7";
						last_7 = 0;
					}
				}else if(state_name == Methods.HX_STATE_8 ){
					was_empty6	= false;
					was_empty4	= false;
					fromstart 	= 0;
					frontNum = 0;
					BackNum = 0;
					decoded += "/HX_STATE_8";
				}else if(state_name == Methods.HX_STATE_9 ){
					decoded += "/HX_STATE_9";
					last_3 = 0;
					last_7 = 0;
					state.set( "X_GLOBAL_MIN", hpos );
					barobot.driver_x.setM(hpos);
					state.set("MARGINX", hpos);
					// new software pos (equal 0);
					spos = barobot.driver_x.hard2soft(hpos);
					int SERVOY_FRONT_POS = state.getInt("SERVOY_FRONT_POS", 1000 );
					barobot.hereIsStart(spos, SERVOY_FRONT_POS );
					Initiator.logger.i("input_parser", "jestem w: " + spos );
					barobot.driver_x.setSPos( spos );

				}else if(state_name == Methods.HX_STATE_10 ){		// ERROR not connected
					decoded += "/HX_STATE_10";
				}
			}else{
				if(state_name == Methods.HX_STATE_0 ){				// ERROR
				}else if(state_name == Methods.HX_STATE_1 ){
					decoded += "/HX_STATE_1";
					state.set( "LENGTHX", spos);
					state.set( "X_GLOBAL_MAX", spos );
					int SERVOY_FRONT_POS = state.getInt("SERVOY_FRONT_POS", 1000 );
					barobot.hereIsBottle(11, spos, SERVOY_FRONT_POS );
					state_num = 0;
				}else if(state_name == Methods.HX_STATE_2 ){
				}else if(state_name == Methods.HX_STATE_3 ){
				}else if(state_name == Methods.HX_STATE_4 ){
				}else if(state_name == Methods.HX_STATE_5 ){
				}else if(state_name == Methods.HX_STATE_6 ){
				}else if(state_name == Methods.HX_STATE_7 ){
				}else if(state_name == Methods.HX_STATE_8 ){
				}else if(state_name == Methods.HX_STATE_9 ){
					state.set( "X_GLOBAL_MIN", hpos );
					barobot.driver_x.setM(hpos);
					state.set("MARGINX", hpos);
					// new software pos (equal 0)
					spos = barobot.driver_x.hard2soft(hpos);
					barobot.driver_x.setSPos( spos );
					int SERVOY_FRONT_POS = state.getInt("SERVOY_FRONT_POS", 1000 );
					barobot.hereIsStart(spos, SERVOY_FRONT_POS );
					Initiator.logger.i("input_parser", "jestem2 w: " + spos );

				}else if(state_name == Methods.HX_STATE_10 ){		// ERROR not connected
				}	
			}
		//	Initiator.logger.i("INNER_HALL_X", "" + state_name + " / "+ hpos+ " / "+ dir);
		}else if( parts[1] == Methods.INNER_HALL_Y ){
			decoded += "/INNER_HALL_Y";
		}else if( parts[1] == Methods.INNER_WEIGHT ){
			decoded += "/INNER_WEIGHT";
		}else{
			decoded += "/???";
			Initiator.logger.i("MyRetReader.decoded", decoded);
			return false;
		}
		if( checkInput ){
			Initiator.logger.i("MyRetReader.decoded checkInput", decoded);
			return true;
		}
		Initiator.logger.i("MyRetReader.decoded", decoded);
		return true;
	}
	private void hereIsMagnet(int magnetnum, int fromHPos, int toHPos, int bottleIsBack ) {
		int num		= Constant.magnet_order[magnetnum];
		int ypos	= 0;
		int row		= Constant.bottle_row[ num ];

		if( row == Constant.BOTTLE_IS_BACK ){
			ypos = barobot.state.getInt("SERVOY_BACK_POS", 0);

		}else if(row ==Constant.BOTTLE_IS_FRONT){
			ypos = barobot.state.getInt("SERVOY_FRONT_POS", 0);
		}else{
			Initiator.logger.e("bottle "+ num, "error" );
		}

		if(row == Constant.BOTTLE_IS_BACK){
			Initiator.logger.i("bottle "+ num +" BACK", "frontNum: "+ frontNum+" BackNum: "+ BackNum+ "from " +fromHPos + " to " + toHPos );
		}else{
			Initiator.logger.i("bottle "+ num +" FRONT", "frontNum: "+ frontNum+" BackNum: "+ BackNum+ "from " +fromHPos + " to " + toHPos );
		}
		if( bottleIsBack == row ){
			int hposx = (fromHPos + toHPos) / 2;
			//int hposx	= fromPos;
			int spos2	= barobot.driver_x.hard2soft(hposx);
			barobot.hereIsBottle(num, spos2, ypos );
			Upanel up	= barobot.i2c.getUpanelByBottle(num);
			Queue q		= barobot.main_queue;
		    if( up != null ){
		    	q.sendNow(  "L"+ up.getAddress() + ",02,200" );
				//up.setLed( q, "ff", 0 );
			}else{
				Initiator.logger.i("bottle "+ num +"","nie ma upanela dla id " + num );	
			}
		}else{
			Initiator.logger.i("bottle "+ num +"", "nie zgadza sie");
			if(row ==  Constant.BOTTLE_IS_BACK ){
				Initiator.logger.i("bottle "+ num +"", "nie zgadza sie. spodziewano sie back a jest front");	
			}else if(row ==  Constant.BOTTLE_IS_FRONT ){
				Initiator.logger.i("bottle "+ num +"", "nie zgadza sie. spodziewano sie front a jest back");
			}
		}
	}
}
