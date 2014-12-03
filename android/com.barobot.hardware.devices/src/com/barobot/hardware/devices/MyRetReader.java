package com.barobot.hardware.devices;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.common.constant.Methods;
import com.barobot.common.interfaces.HardwareState;
import com.barobot.parser.Queue;
import com.barobot.parser.interfaces.RetReader;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;
import com.barobot.parser.utils.GlobalMatch;

public class MyRetReader implements RetReader {
	private BarobotConnector barobot;
	private HardwareState state;
//	private BarobotEventListener bel;
	private int robot_id_high = 0;
	boolean checkInput	= false;		// pokazuj logi z analoga

	public MyRetReader(  BarobotConnector brb ){
		this.barobot	= brb;
		this.state		= brb.state;

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
				int hpos = Decoder.toInt(fromArduino, 0);
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
			public String getMatchRet() {
				return "^POKE \\d+$";		// i.e. "POKE 820166"
			}
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {
				fromArduino = fromArduino.replace("POKE ", "");
				int millis = Decoder.toInt(fromArduino, 0);
				barobot.setLastSeen(millis);
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
					barobot.driver_x.setMargin(cx);	// ostatnia znana pozycja jest marginesem
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

		barobot.mb.addGlobalRegex( new GlobalMatch(){		// METHOD_DEVICE_FOUND
			@Override
			public boolean run(Mainboard asyncDevice, String fromArduino, String wait4Command, AsyncMessage wait_for) {				
				String decoded	= "Arduino.GlobalMatch.METHOD_DEVICE_FOUND/MAINBOARD_DEVICE_TYPE";
				int cx			= barobot.driver_x.getSPos();
				barobot.driver_x.setMargin(-cx);	// ostatnia znana pozycja jest marginesem
				Queue mq		= barobot.main_queue;
				mq.clear();
				Initiator.logger.i("MyRetReader.decoded", decoded);
				barobot.onConnected(barobot.main_queue, true );
				return true;
			}
			@Override
			public String getMatchRet() {
				return "^BSTART$";
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

		//	RESET	RRESET			RESET MAINBOARD
		//	RESET3	RRESET3		OLD RESET NODE 3
		//	RB		RRB			OLD	RESET I2C BUS
		//	RB2		RRB2		OLD	RESET I2C BUS
		//	TEST	RTEST		OLD	I2C TEST
		//	T		RT94			TEMPERATURE
		//	EX		REX				ENABLE X
		//	EY		REY			OLD	ENABLE Y
		//	EZ		REZ			OLD	ENABLE Z
		//	DX		RDX				DISABLE X
		//	DY		RDY				DISABLE Y
		//	DZ		RDZ				DISABLE Z
		//	x		Rx				GET X POS
		//	y		Ry				GET Y POS
		//	z		Rz				GET Z POS
		//	Q		RQ				SET ALL LEDS
		//	l		Rl				SET 1 LED
		//	L		RL			OLD LEDS
		//	B		RB			OLD LEDS
		//	C		RC			OLD LEDS
		//	AX		RAX				ACCELERATION X
		//	G		RG
		//	WR		RWR
		//	M		RM				SET EEPROM
		//	m		Rm				READ EEPROM
		//	A		RA				READ ANALOG
		//	a		Ra				READ ANALOG DIRECTLY
		//	K		RK				SET SERVO Z
		//	PING	RPONG		OLD SYNCHRO
		//	AA		-				ANDORID ACTIVE
		//	V		RV				PCB VERSION
		//	P		RP			OLD	PROGRAMER
		//	N		RN			OLD HAS NEXT ON I2C
		//	n		Rn			OLD HAS NEXT ON I2C
		//	I		RI			OLD TEST I2C
		//	IH		RIH				IS HOME
		//  S		RS				stats

		String command = "";
		if(wait_for2!= null && wait_for2.command != null && wait_for2.command != "" ){
			command = wait_for2.command;
	//		Initiator.logger.w("MyRetReader.isRetOf", fromArduino+ " for "+ command );
			if( fromArduino.startsWith( "R" + command )){
				if( fromArduino.startsWith( "Rm" ) || command.equals( "S" ) || command.equals("IH") || command.startsWith("K") || command.equals("A2") ){
					// analyse below
				}else{
					Initiator.logger.i("Arduino.GlobalMatch", "auto_unlock");
					return true;
				}
			}
			if( command.equals("PING") && fromArduino.equals("PONG") ){		// hack for older version, now PING returns RPONG	
				return true;
			}
			if( fromArduino.startsWith( "E" + command)){		// error tez odblokowuje
				return true;
			}
		//	if( fromArduino.startsWith("RX") && wait_for2.command.startsWith("x") ){
		//		return true;
		//	}
			if( fromArduino.equals( "NOCMD ["+ command +"]")){
				return true;	// no command = unlock and log
			}
		}

		//Initiator.logger.i("MyRetReader.decoded.44444", fromArduino);
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
		//		Initiator.logger.i("new pos y:", ""+pos );
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
					Initiator.logger.e("MyRetReader.decoded.wrong1", "[" + command + "], expected output:" + retLike+ " because of " + decoded );
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
					Initiator.logger.e("MyRetReader.decoded.wrong3", command + ", expected output:" + "E..." + " fromArduino:'"+ fromArduino+"'" );
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
	//					Initiator.logger.i("MyRetReader.decoded OK", decoded);
						return true;
					}else{
						Initiator.logger.e("MyRetReader.decoded.wrong5", command + ", expected output:" + "X..." + " because of " + decoded + " fromArduino:'"+ fromArduino+"'" );
					}
				}else if( parts[3] == Methods.DRIVER_Y){
					int pos = parts[4] + (parts[5] << 8);
					decoded += "/DRIVER_Y";
					state.set( "POSY", pos );
					if(command.startsWith("Y")){
	//					Initiator.logger.i("MyRetReader.decoded OK", decoded);
						return true;
					}else{
						Initiator.logger.e("MyRetReader.decoded.wrong7", command + ", expected output:" + "Y..." + " because of " + decoded + " fromArduino:'"+ fromArduino+"'" );
					}
				}else if( parts[3] == Methods.DRIVER_Z){
					int pos = parts[4] + (parts[5] << 8);
					decoded += "/DRIVER_Z";
					state.set( "POSZ",pos);
					if(command.startsWith("Z")){
		//				Initiator.logger.i("MyRetReader.decoded OK", decoded);
						return true;
					}else if(command.startsWith("K")){
						Initiator.logger.i("MyRetReader.decoded OK", decoded);
						return true;
					}else{
						Initiator.logger.e("MyRetReader.decoded.wrong9", command + ", expected output:" + "Z..." + " because of " + decoded + " fromArduino:'"+ fromArduino+"'" );
					}
				}else{
					decoded += "/???";
					Initiator.logger.i("MyRetReader.decoded", decoded);
					return false;
				}
			}else{
				Initiator.logger.e("MyRetReader", "unknown METHOD_I2C_SLAVEMSG => false(fromArduino: '"+ fromArduino +"')");
				return false;
			}
			//Initiator.logger.i("MyRetReader.retLike", retLike);
			//Initiator.logger.i("MyRetReader.decoded", decoded);
			return false;

		}else if(fromArduino.startsWith(""+Methods.METHOD_CHECK_NEXT) ){
			decoded += "/METHOD_CHECK_NEXT";

		}else if(fromArduino.startsWith(""+Constant.RET) ){		// na końcu bo to może odblokować wysyłanie i spowodować zapętlenie
			decoded += "/RET";
			String fromArduino2 = fromArduino.substring(1);		// without R
			if(fromArduino2.startsWith(Constant.GETXPOS)  ){
				decoded += "/GETXPOS";
				String fromArduino3 = fromArduino2.replace(Constant.GETXPOS, "");	
				int hpos = Decoder.toInt(fromArduino3, 0);	// hardware pos
				barobot.driver_x.setHPos( hpos );
				if( command.startsWith(Constant.GETXPOS)){
					return true;
				}else{
					Initiator.logger.e("MyRetReader.decoded.wrong12", "command:" +command+ ", decoded: " + decoded + " fromArduino:'"+ fromArduino+"'" );
				}
			}else if(fromArduino2.startsWith(Constant.GETYPOS)){
				decoded += "/GETYPOS";
				String fromArduino3 = fromArduino2.replace(Constant.GETYPOS, "");
				state.set( "POSY",fromArduino3);
				if( command.startsWith(Constant.GETYPOS)){
					return true;
				}else{
					Initiator.logger.e("MyRetReader.decoded.wrong13", "command:" +command+ ", decoded: " + decoded + " fromArduino:'"+ fromArduino+"'" );
				}
			}else if(fromArduino2.startsWith(Constant.GETZPOS)){
				decoded += "/GETZPOS";
				String fromArduino3 = fromArduino2.replace(Constant.GETZPOS, "");
				state.set( "POSZ",fromArduino3);
				if( command.startsWith(Constant.GETZPOS)){
					return true;
				}else{
					Initiator.logger.e("MyRetReader.decoded.wrong13", "command:" +command+ ", decoded: " + decoded + " fromArduino:'"+ fromArduino+"'" );
				}
			}else if(fromArduino2.equals( "IH" ) && command.equals("IH")){				// is home
				barobot.driver_x.setMargin(0);
				barobot.driver_x.setHPos( 0 );
				Initiator.logger.e("MyRetReader.decoded", "this is home");
				return true;

			}else if(fromArduino2.startsWith( "S" ) ){				// stats
				String fromArduino3 = fromArduino2.substring(2);
				int[] parts = Decoder.decodeBytes( fromArduino3 );
				if(parts.length >= 5 ){
					// RRS,VERSION,TEMP,STARTS,ROBOT_ID_LOW, ROBOT_ID_HIGH
					if( parts[0] > 0 ){
						state.set( "ARDUINO_VERSION", parts[0]);
				//		Initiator.logger.i("MyRetReader.decoded.version", "is " + parts[0]);
					}
					if( parts[1] > 0 ){
						state.set("TEMPERATURE", parts[1] );
				//		Initiator.logger.i("MyRetReader.decoded.TEMPERATURE", "s " + parts[1]);
					}
					if( parts[2] > 0 ){
						state.set("ARDUINO_STARTS", parts[2] );
				//		Initiator.logger.i("MyRetReader.decoded.ARDUINO_STARTS", "is " + parts[2]);
					}
					if( parts[3] > 0 || parts[4] > 0 ){			// OR is ok
						int id = (parts[4] << 8) + parts[3];
						if( id > 0 && id < 65535 ){				// 65535 = 1111111111111111b (empty eeprom)
							barobot.changeRobotId( id, true );
						}else{
							this.robot_id_error = true;
						}
					}
				}
				if( command.equals("S")){
					return true;
				}
			}else if(fromArduino2.startsWith( "A2" ) && command.equals("A2")){			// load cell (weigh sensor)
				String fromArduino3 = fromArduino2.substring(3);		// 	RA2,598		=> A2,598		=> 598
				int weight			= Decoder.toInt(fromArduino3, 0);
				if( weight > 0 ){			// 0 is imposible
					barobot.weight.newValue(weight);
				}
				if( command.equals("A2") ){
					return true;
				}
		//	}else if(fromArduino2.startsWith( "M" ) && command.startsWith("M") ){	// save in eeprom memory

			}else if(fromArduino2.startsWith( "K" ) ){								// move Z
				String fromArduino3 = fromArduino2.substring(1);		// 	RK1500		=> K1500		=> 1500
				int pos				= Decoder.toInt(fromArduino3, 0);
				decoded				+= "/DRIVER_Z";
				if( pos > 0 ){
					state.set( "POSZ",pos );
				}
				if(command.startsWith("K")){
					return true;
				}else{
					Initiator.logger.e("MyRetReader.decoded.wrong76", command + ", expected output:" + "Z..." + " because of " + decoded + " fromArduino:'"+ fromArduino+"'" );
				}

			}else if(fromArduino2.startsWith( "m" ) && command.startsWith("m")){	// eeprom memory
				decoded += "/EEPROM";
				String fromArduino3 = fromArduino2.substring(1);
				String[] parts		= fromArduino3.split(",");
				if(parts.length == 3){
					String addressHex	= parts[0];
					int addressDec	= Decoder.fromHex(addressHex, -1);
				//	int address 	= Decoder.fromHex(parts[0], -1);
					int value1 = Decoder.toInt(parts[1], 0);
					int value2 = Decoder.toInt(parts[2], 0);
					decoded += "/"+addressDec+"/"+value1;
					if(value1 != value2 ){
						Initiator.logger.e("MyRetReader.decoded.fatal", "memory error: "+ addressDec + ", value1 "+ value1+ ", value2 "+ value2 );
					}
					from_eeprom_memory( addressDec, value1 );
					String expected = "m"+ addressHex;
					if( command.equals(expected) ){
		//				Initiator.logger.e("MyRetReader.decoded", "[" + command + "], decoded: " + decoded + " fromArduino:"+ fromArduino );
						return true;
					}else{
		//				Initiator.logger.e("MyRetReader.decoded.wrong8", "[" + command + "], decoded: " + decoded + " fromArduino:"+ fromArduino );
						return false;
					}
				}else{
					Initiator.logger.e("MyRetReader.decoded.wrong8", "[" + command + "], decoded: " + decoded + " fromArduino:"+ fromArduino );
				}
			}else{
				decoded += "/????" + "fromArduino2: [" + fromArduino2+"], command: [" + command + "]";
			}
		}else if( fromArduino.startsWith( "" + Methods.METHOD_IMPORTANT_ANALOG) ){		// msg od slave		
	//		Initiator.logger.i("MyRetReader.decoded checkInput", decoded);
			if( command.startsWith("A")){
				return importantAnalog(asyncDevice, wait_for2, fromArduino, checkInput );
			}else{
				return false;
			}
		}else if( fromArduino.startsWith( "" + Methods.RETURN_I2C_ERROR) ){
			decoded += "/RETURN_I2C_ERROR";
			// short ttt[4] = {RETURN_I2C_ERROR,my_address, deviceAddress,length, command }
			// Urządzenie 'my_address' wysyłało do 'deviceAddress' bajtów length
			//barobot.main_queue.unlock();
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

	private void from_eeprom_memory(int address, int value ) {
		if( address == Methods.EEPROM_ROBOT_ID_HIGH ){
			this.robot_id_high  = value;
		}
		if( address == Methods.EEPROM_ROBOT_ID_LOW ){		// always check high and then low, so here set robot_id
			int value2 = (this.robot_id_high << 8) + value;
			if( value2 > 0 && value2 < 65535 ){			// 65535 = 1111111111111111b (empty eeprom)
			//	Initiator.logger.i("MyRetReader.from_eeprom_memory", "value4: "+ value2 );
				this.barobot.changeRobotId( value2, true );
			}else{
				this.robot_id_error = true;
			}
		}
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
		/*
		byte ttt[10] = {
				METHOD_IMPORTANT_ANALOG, 	// 0
				INNER_HALL_X, 		// 1	
				state_name,  		// 2	// STATE
				dir,				// 3	// dir
				bytepos.bytes[3], 	// 4	// bits 0-7
				bytepos.bytes[2],  	// 5	// bits 8-15
				bytepos.bytes[1], 	// 6	// bits 16-23
				bytepos.bytes[0],  	// 7	// bits 24-32
				(value & 0xFF),
				(value >>8),
			};
		*/
		String decoded = "/METHOD_IMPORTANT_ANALOG";
		int[] parts = Decoder.decodeBytes( fromArduino );
		if( parts.length >= 10 && parts[1] == Methods.INNER_HALL_X ){
			decoded += "/HALL_X";
		//	Initiator.logger.i("input_parser", "hardware pos: " + hpos );
		//	Initiator.logger.i("input_parser", "software pos: " + spos );
			int state_name	= parts[2];
			int dir			= parts[3];
		//	int hpos		= parts[7] + (parts[6] << 8) + (parts[5] << 16 + (parts[4] << 24));
			short hpos		= (short) (parts[6] << 8);
			hpos			+= (short)parts[7];
			int value		= parts[8] + (parts[9] << 8);
			int spos		= barobot.driver_x.hard2soft(hpos);
			decoded += "/@s:" + spos;
			decoded += "/@h:" + hpos;
			decoded += "/#" + value;

			barobot.driver_x.setHPos( hpos );
			barobot.state.set("HALLX", value);
			barobot.state.set("HX_STATE", state_name);

			boolean isCalibrating	= state.getInt("scann_bottles", 0 ) > 0 && !checkInput && dir == Methods.DRIVER_DIR_FORWARD;
			if( isCalibrating ){
				state_num++;
				if(state_name == Methods.HX_STATE_0 ){				// ERROR
					decoded += "/HX_STATE_0";
					state_num = 0;
				}else if(state_name == Methods.HX_STATE_1 ){
					decoded += "/HX_STATE_1/"+ spos;
				//	spos += 300;				// hall stops 400 points before magnet
					state.set( "LENGTHX", spos);
					Initiator.logger.i("input_parser.11", decoded );
				//	barobot.hereIsBottle(11, spos, SERVOY_FRONT_POS );
					state_num = 0;
				}else if(state_name == Methods.HX_STATE_2 ){
					decoded += "/HX_STATE_2";
					hereIsMagnet( 11, hpos, hpos, Constant.BOTTLE_IS_FRONT );
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
					last_3	= 0;
					last_7	= 0;
					if (hpos != barobot.driver_x.getHardwarePos()){
						barobot.driver_x.setMargin(hpos);						// new software pos
						spos = barobot.driver_x.hard2soft(hpos);
						int SERVOY_FRONT_POS = state.getInt("SERVOY_FRONT_POS", 1000 );
						barobot.hereIsStart(spos, SERVOY_FRONT_POS );
						Initiator.logger.i("input_parser", "jestem w: " + spos );
						barobot.driver_x.setSPos( spos );
					}

				}else if(state_name == Methods.HX_STATE_10 ){		// ERROR not connected
					decoded += "/HX_STATE_10";
				}
				String line = value + "\t" + state_name + "\t" + hpos + "\t" + state_num + "\t" + fromstart+ "\t" + "\t" + decoded;
				Initiator.logger.saveLog(line);
			}else{
				if(state_name == Methods.HX_STATE_0 ){				// ERROR
					barobot.state.set("HALLX_UNDER", "-2");
				}else if(state_name == Methods.HX_STATE_1 ){
					decoded += "/HX_STATE_1";
					state.set( "LENGTHX", spos);
					int SERVOY_FRONT_POS = state.getInt("SERVOY_FRONT_POS", 1000 );
					Initiator.logger.i("input_parser", "koniec skali: " + spos );
					barobot.hereIsBottle(11, spos+100, SERVOY_FRONT_POS );
					state_num = 0;
					barobot.state.set("HALLX_UNDER", "1");
				}else if(state_name == Methods.HX_STATE_2 ){
				}else if(state_name == Methods.HX_STATE_3 ){	// back pos
					barobot.state.set("HALLX_UNDER", "2");
				}else if(state_name == Methods.HX_STATE_4 ){	// before back
				}else if(state_name == Methods.HX_STATE_5 ){	// no magnet
					barobot.state.set("HALLX_UNDER", "0");
				}else if(state_name == Methods.HX_STATE_6 ){	// before front pos
				}else if(state_name == Methods.HX_STATE_7 ){	// under front pos
					barobot.state.set("HALLX_UNDER", "1");
				}else if(state_name == Methods.HX_STATE_8 ){	
				}else if(state_name == Methods.HX_STATE_9 ){
					barobot.driver_x.setMargin(hpos);
					spos = barobot.driver_x.hard2soft(hpos);// new software pos (equal 0)
					barobot.driver_x.setSPos( spos );
					int SERVOY_FRONT_POS = state.getInt("SERVOY_FRONT_POS", 1000 );
					barobot.hereIsStart(spos, SERVOY_FRONT_POS );
		//			Initiator.logger.i("input_parser", "jestem2 w: " + spos );
					barobot.state.set("HALLX_UNDER", "0");

				}else if(state_name == Methods.HX_STATE_10 ){		// ERROR not connected
					barobot.state.set("HALLX_UNDER", "-1");
				}
			}
		//	Initiator.logger.i("INNER_HALL_X", "" + state_name + " / "+ hpos+ " / "+ dir);
		}else if( parts[1] == Methods.INNER_HALL_Y ){
			decoded += "/INNER_HALL_Y";
			/*
			byte ttt[10] = {
					METHOD_IMPORTANT_ANALOG, 
					INNER_HALL_Y, 
					state_name, 
					0,						// last dir
					0,						// pos
					0,						// pos
					(pos & 0xFF),			// position
					(pos >>8),				// position
					(value & 0xFF),
					(value >>8),
				};
				// 125,1,66,0,0,0,228,2,177,1
			*/

		//	int state_name	= parts[2];
			short hpos		= (short) (parts[6] << 8);
			hpos			+= (short)parts[7];
			int value		= parts[8] + (parts[9] << 8);
			decoded += "/@pos:" + hpos;
			decoded += "/value:" + value;
			if( value > 0 ){
				barobot.state.set("HALLY", value);
			}
	//		Initiator.logger.i("input_parser", decoded );
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
			Initiator.logger.saveLog("bottle "+ num +" BACK"+ "frontNum: "+ frontNum+" BackNum: "+ BackNum+ "from " +fromHPos + " to " + toHPos );
		}else{
			Initiator.logger.saveLog("bottle "+ num +" FRONT"+ "frontNum: "+ frontNum+" BackNum: "+ BackNum+ "from " +fromHPos + " to " + toHPos );
		}
		if( bottleIsBack == row ){
			int hposx = (fromHPos + toHPos) / 2;
			if( bottleIsBack == Constant.BOTTLE_IS_BACK){
		//		hposx -= 20;
			}
			if(num <= 11){
				Initiator.logger.i("dodaje do bottle "+ num +"", "bylo:"+hposx+ ", bedzie:"+(hposx+50));
				//int hposx	= fromPos;
				int spos2	= barobot.driver_x.hard2soft(hposx);
				barobot.hereIsBottle(num, spos2, ypos );
				barobot.lightManager.color_by_bottle_now( num, "02", 200, 0, 200, 0);
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
