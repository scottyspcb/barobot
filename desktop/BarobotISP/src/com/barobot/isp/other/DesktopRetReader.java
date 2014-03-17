package com.barobot.isp.other;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareContext;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;
import com.barobot.parser.output.RetReader;
import com.barobot.parser.utils.Decoder;

public class DesktopRetReader implements RetReader {

	private HardwareContext hwc;

	DesktopRetReader( HardwareContext hwc ){
		this.hwc = hwc;
	}

	public boolean isRetOf(AsyncDevice asyncDevice, AsyncMessage wait_for2, String fromArduino ) {

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
				/*virtualComponents.driver_x.hard2soft(hpos);
				
				
				
				
				virtualComponents.saveXPos( posx);
				int lx	=  virtualComponents.getInt("LENGTHX", 600 );
				if( posx > lx){		// Pozycja wieksza niz dlugosc? Zwieksz długosc
					virtualComponents.set( "LENGTHX", "" + posx);
				}
				if(command.equals("x")){
					return true;
				}else{
					return false;
				}
				*/
				
			}else if( parts[2] == Methods.METHOD_GET_Y_POS ){
				decoded += "/METHOD_GET_Y_POS";
				int pos = parts[3] + (parts[4] << 8); 
	//			virtualComponents.set( "POSY",""+pos);
				if(command.equals("y")){
					return true;
				}else{
					return false;
				}
				
			}else if( parts[2] == Methods.METHOD_GET_Z_POS ){
				decoded += "/METHOD_GET_Z_POS";
				int pos = parts[3] + (parts[4] << 8); 
	//			virtualComponents.set( "POSZ",""+pos);
				if(command.equals("z")){
					return true;
				}else{
					return false;
				}
			}else if( parts[2] == Methods.METHOD_DRIVER_DISABLE ){
				decoded += "/METHOD_DRIVER_DISABLE";
				if( parts[3] == Constant.DRIVER_X){
					decoded += "/DRIVER_X";
					retLike = "DX";
				}else if( parts[3] == Constant.DRIVER_Y){
					decoded += "/DRIVER_Y";
					retLike = "DY";
				}else if( parts[3] == Constant.DRIVER_Z){
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
					Initiator.logger.e("MyRetReader.decoded.wrong", command);
					return false;
				}
				
			}else if( parts[2] == Methods.METHOD_DRIVER_ENABLE ){
				decoded += "/METHOD_DRIVER_ENABLE";
				if( parts[3] == Constant.DRIVER_X){
					decoded += "/DRIVER_X";
					retLike = "REX";
				}else if( parts[3] == Constant.DRIVER_Y){
					decoded += "/DRIVER_Y";
					retLike = "REY";
				}else if( parts[3] == Constant.DRIVER_Z){
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
					Initiator.logger.e("MyRetReader.decoded.wrong", command);
					return false;
				}
				
			}else if( parts[2] == Methods.RETURN_DRIVER_READY ){
				decoded += "/RETURN_DRIVER_READY";
				if( parts[3] == Constant.DRIVER_X){
					decoded += "/DRIVER_X";
					//int pos = parts[4] + (parts[5] << 8) + (parts[6] << 16 + (parts[7] << 24));
					int hpos = parts[7] + (parts[6] << 8) + (parts[5] << 16 + (parts[4] << 24));
			//		int spos = virtualComponents.driver_x.hard2soft(hpos);
			//		virtualComponents.saveXPos( spos );
					if(command.startsWith("X")){
						return true;
					}else{
						Initiator.logger.e("MyRetReader.decoded.wrong", command);
					}
				}else if( parts[3] == Constant.DRIVER_Y){
					int pos = parts[4] + (parts[5] << 8);
					decoded += "/DRIVER_Y";
		//			virtualComponents.set( "POSY",pos);
					if(command.startsWith("Y")){
						return true;
					}else{
						Initiator.logger.e("MyRetReader.decoded.wrong", command);
					}
				}else if( parts[3] == Constant.DRIVER_Z){
					int pos = parts[4] + (parts[5] << 8);
					decoded += "/DRIVER_Z";
		//			virtualComponents.set( "POSZ",pos);
					if(command.startsWith("Z")){
						return true;
					}else{
						Initiator.logger.e("MyRetReader.decoded.wrong", command);
					}
				}else{
					decoded += "/???";
					Initiator.logger.i("MyRetReader.decoded", decoded);
					return false;
				}

			}else{
				Initiator.logger.e("MyRetReader", "no METHOD_I2C_SLAVEMSG => false");
				return false;
			}
			//Initiator.logger.i("MyRetReader.retLike", retLike);
			Initiator.logger.i("MyRetReader.decoded", decoded);
			return false;
		}else if(fromArduino.startsWith(""+Methods.RETURN_PIN_VALUE) ){
			return false;
		}else if(fromArduino.startsWith(""+Methods.METHOD_CHECK_NEXT) ){
			decoded += "/METHOD_CHECK_NEXT";

		}else if( fromArduino.startsWith( "" + Methods.METHOD_IMPORTANT_ANALOG) ){		// msg od slave		
			Initiator.logger.i("MyRetReader.decoded checkInput", decoded);
			if( command.startsWith("A")){
				return importantAnalog(asyncDevice, wait_for2, fromArduino, true );
			}else{
				return false;
			}


		}else if( fromArduino.startsWith( "" + Methods.METHOD_EXEC_ERROR) ){		// msg od slave		
			decoded += "/METHOD_EXEC_ERROR";
			int[] parts = Decoder.decodeBytes( fromArduino );
			String retLike = fromArduino;
			if( parts[3] == Constant.DRIVER_X){
				decoded += "/Rx";
				retLike = "Rx";
			}else if( parts[3] == Constant.DRIVER_Y){
				decoded += "/Ry";
				retLike = "Ry";
			}else if( parts[3] == Constant.DRIVER_Z){
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

	private int states = 54;
	private int state_num = 0;
	private int fromstart = 0;
	
	private int last_3 = 0;
	private int last_7 = 0;
	private boolean was_empty6 = false;
	private boolean was_empty4 = false;
	
	public boolean importantAnalog(AsyncDevice asyncDevice, AsyncMessage wait_for2, String fromArduino, boolean checkInput ) {
		String command = "";
		if(wait_for2!= null && wait_for2.command != null && wait_for2.command != "" ){
			command = wait_for2.command;
		}
		/* byte ttt[8] = { METHOD_IMPORTANT_ANALOG, 
			INNER_HALL_X, 
			state_name, 
			0,					// dir is unknown on carret
			0, 					// position is unknown on carret
			0,  				// position is unknown on carret
			(value & 0xFF), (value >>8),};
		*/
		String decoded = "/METHOD_IMPORTANT_ANALOG";
		int[] parts = Decoder.decodeBytes( fromArduino );
		if( parts[1] == Methods.INNER_HALL_X ){
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

/*
				int state_name	= parts[2];
				int dir			= parts[3];
				int hpos		= parts[4] + (parts[5] << 8); 
				int spos		= virtualComponents.driver_x.hard2soft(hpos);
				
				virtualComponents.saveXPos( spos );
				
				state_num++;
				if(state_name == Methods.HX_STATE_0 ){				// ERROR
					decoded += "/HX_STATE_0";
					state_num = 0;
				}else if(state_name == Methods.HX_STATE_1 ){
					decoded += "/HX_STATE_1";
					if(virtualComponents.scann_bottles == true){
						virtualComponents.set( "LENGTHX", "" + spos);
						virtualComponents.set( "X_GLOBAL_MAX", "" + spos );
						virtualComponents.hereIsBottle(11, spos, virtualComponents.SERVOY_FRONT_POS );
						Initiator.logger.i("input_parser "+ virtualComponents.scann_num+" "+virtualComponents.SERVOY_FRONT_POS, "butelka 11: " + spos );
					}
					state_num = 0;
				}else if(state_name == Methods.HX_STATE_2 ){
					decoded += "/HX_STATE_2";
					was_empty6 = false;
					was_empty4 = false;

				}else if(state_name == Methods.HX_STATE_3 ){
					if(was_empty4){
						fromstart++;
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
						Initiator.logger.i("bottle "+ fromstart +" BACK", "from " +last_3 + " to " + hpos );
						int hposx = (hpos + last_3) / 2;
						int spos2	= virtualComponents.driver_x.hard2soft(hposx);
						int ypos	= virtualComponents.SERVOY_BACK_POS;
					//	int num		= virtualComponents.magnet_order[ind];
						virtualComponents.hereIsBottle(fromstart, spos2, ypos );
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
						Initiator.logger.i("bottle "+ fromstart +" FRONT", "from " +last_7 + " to " + hpos );

						int hposx = (hpos + last_7) / 2;
						int spos2	= virtualComponents.driver_x.hard2soft(hposx);
						int ypos	= virtualComponents.SERVOY_FRONT_POS;
					//	int num		= virtualComponents.magnet_order[ind];
						virtualComponents.hereIsBottle(fromstart, spos2, ypos );
						last_7 = 0;

					}else{
						decoded += "/HX_STATE_6";

						was_empty6 = true;
					}
				}else if(state_name == Methods.HX_STATE_7 ){
					if(was_empty6){
						last_7  = hpos;
						fromstart++;
						was_empty6 = false;
						decoded += "/7 BOTTLE START";
					}else{
						decoded += "/HX_STATE_7";
						last_7 = 0;
					}
				}else if(state_name == Methods.HX_STATE_8 ){
					was_empty6 = false;
					was_empty4 = false;
					decoded += "/HX_STATE_8";
				}else if(state_name == Methods.HX_STATE_9 ){
					decoded += "/HX_STATE_9";
					fromstart =0;
					virtualComponents.set( "X_GLOBAL_MIN", "" + hpos );
					virtualComponents.driver_x.setM(hpos);
					spos = virtualComponents.driver_x.hard2soft(hpos);		// new software pos (equal 0);
					virtualComponents.hereIsStart(spos, virtualComponents.SERVOY_FRONT_POS );
			//		Initiator.logger.i("input_parser", "jestem w: " + spos );

				}else if(state_name == Methods.HX_STATE_10 ){		// ERROR not connected
					decoded += "/HX_STATE_10";
				}
		
		*/
		//	Initiator.logger.i("INNER_HALL_X", "" + state_name + " / "+ hpos+ " / "+ dir);
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

}
