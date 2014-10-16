package com.barobot.hardware.devices;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.barobot.common.Initiator;
import com.barobot.common.IspOverSerial;
import com.barobot.common.interfaces.serial.IspCommunicator;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.isp.UploadCallBack;
import com.barobot.isp.Uploader;
import com.barobot.isp.enums.Board;
import com.barobot.isp.enums.UploadErrors;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

public class UploadFirmware {
	private Uploader ispUploader;
	private IspOverSerial mSerial;

	public void prepareMB( Queue q, final Wire oldConnection, final String hex_firmware_path, final UploadCallBack rl ) {
		File hex_firmware_file = new File(hex_firmware_path);
        if(!hex_firmware_file.exists()){
        	q.add( new AsyncMessage( true ) {		// download new file
    			@Override
    			public String getName() {
    				return "Download firmware";
    			}
    			@Override
    			public Queue run(Mainboard dev, Queue queue) {
					return null;
    			}
        	});
        }
		q.add( "RESET", "RRESET" );					// ret command will come 1 sec before reset
		q.addWait(800);								// synchronize android and arduino
		q.add( new AsyncMessage( true, true ) {		// when version readed
			@Override
			public String getName() {
				return "Upload";
			}
			@Override
			public Queue run(final Mainboard dev, Queue queue) {
				File file = new File(hex_firmware_path);
				if(!file.exists()){
					rl.onError(UploadErrors.HEX_FILE_OPEN);
					return null;
				}
				final AsyncMessage msg	= this;
				ispUploader				= new Uploader();
				ispUploader.setCallBack( new UploadCallBack() {	// call back is very important for error handling so do it first
			        @Override
			        public void onUploading(int value) {
			        	rl.onUploading(value);
			        }
			        @Override
			        public void onPreUpload() {
			        	rl.onPreUpload();
			        }
			        @Override
			        public void onPostUpload(boolean success) {
			        	if(success){			// if success run after all
			        		rl.onUploading(105);
			        	}else{
			        		rl.onPostUpload(success);
			        	}
						new Timer().schedule(new TimerTask() {
						    public void run() {
						    	Initiator.logger.w("onPostUpload.schedule", "now!!!");
						    	mSerial.free();
						    	dev.unlockRet( msg, "isp burnt");			// unlock this AsyncMessage
						    }
						}, 7000 );// wait for first - run with new firmware
			        }
			        @Override
			        public void onError(UploadErrors err) {
			        	rl.onError(err);
			        }
			        @Override
			        public void resetDevice(boolean reset, IspCommunicator mComm ){
			    	}
			    });
				//	resetingConnection.setBaud(LowHardware.MAINBOARD_SERIAL0_BOUND);
				mSerial			= new IspOverSerial(oldConnection);
				ispUploader.setSerial(mSerial);
				ispUploader.setBoard( Board.ARDUINO_PRO_5V_328 );
				boolean isSet	= ispUploader.setHex( hex_firmware_path );
				if(!isSet){
					rl.onError(UploadErrors.HEX_STREAM_NUll);
				}
		        try {
		        	rl.onUploading(1);
			        ispUploader.upload();
		        } catch (RuntimeException e) {
		        	Initiator.logger.i("prepareMB2.upload", e.toString());
		        }
				return null;
			}
		});
		q.addWait( 1000 );
		q.add( "PING", "PONG" );
		q.add( "Q00ff0000", true );		// set red
		q.addWait( 1000 );
		q.add( "Q000000ff", true );		// set blue
		q.addWait( 1000 );
		q.add( "Q00ffffff", true );		// set white
		q.addWait( 1000 );
		q.add( "Q0000ff00", true );		// set green
		q.add( new AsyncMessage( true ) {		// when version readed
			@Override
			public String getName() {
				return "Check upload";
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
				rl.onUploading(120);
				Initiator.logger.i("Check upload", "jea!" );
	        	rl.onPostUpload( true );
				return null;
			}
		});
	}
}
