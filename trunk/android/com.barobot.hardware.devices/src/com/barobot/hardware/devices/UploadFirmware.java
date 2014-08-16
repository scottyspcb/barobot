package com.barobot.hardware.devices;

import java.io.IOException;

import com.barobot.common.Initiator;
import com.barobot.common.IspOverSerial;
import com.barobot.common.IspSettings;
import com.barobot.common.constant.LowHardware;
import com.barobot.common.interfaces.onReadyListener;
import com.barobot.common.interfaces.serial.IspCommunicator;
import com.barobot.common.interfaces.serial.Wire;
import com.barobot.hardware.devices.i2c.I2C_Device;
import com.barobot.hardware.devices.i2c.MainboardI2c;
import com.barobot.isp.UploadCallBack;
import com.barobot.isp.Uploader;
import com.barobot.isp.enums.Board;
import com.barobot.isp.enums.UploadErrors;
import com.barobot.parser.Queue;

public class UploadFirmware {
	public Uploader IspUploader;
	public Board mSelectedBoard;
	public IspOverSerial mSerial;

	public void prepareMB2( Queue q, Wire oldConnection, final onReadyListener rl ) {
		q.add( "\n", false );
		q.add( "\n", false );
		q.add( "PING", "PONG" );
		q.add( "PING", "PONG" );

		final I2C_Device current_dev	= new MainboardI2c();
		final String upanel_code		= current_dev.getHexFile();
		mSelectedBoard					= Board.ARDUINO_PRO_5V_328;
		IspUploader						= new Uploader();
		final Wire newConnection		= oldConnection.newInstance();
		final Wire resetingConnection	= oldConnection.newInstance();

		newConnection.setBaud(mSelectedBoard.uploadBaudrate);
		resetingConnection.setBaud(LowHardware.MAINBOARD_SERIAL0_BOUND);

		oldConnection.close();
		Initiator.logger.i(" prepareMB2.wait","please reset");
		mSerial			= new IspOverSerial(newConnection);
		IspUploader.setSerial(mSerial);
		IspUploader.setBoard( mSelectedBoard );
		IspUploader.setCallBack( new UploadCallBack() {
	        @Override
	        public void onUploading(int value) {
	            Initiator.logger.i(" prepareMB2.setProgress",""+value+"%");
	        }
	        @Override
	        public void onPreUpload() {
	            Initiator.logger.i(" prepareMB2.upload","Upload : Start");
	        }
	        @Override
	        public void onPostUpload(boolean success) {
	            if(success) {
	                Initiator.logger.i(" prepareMB2.upload","Upload : Successful");
	            } else {
	                Initiator.logger.i(" prepareMB2.upload", "Upload fail");
	            }
	            rl.onReady();
	        }
	        @Override
	        public void onCancel() {
	            Initiator.logger.i(" prepareMB2.upload","Cancel uploading");
	            rl.onReady();
	        }
	        @Override
	        public void onError(UploadErrors err) {
	            Initiator.logger.i(" prepareMB2.upload","Error  : "+err.toString());
	            rl.onReady();
	        }
	        @Override
	        public void resetDevice(boolean reset, IspCommunicator mComm ){
	        	String r = current_dev.getIsp()+"\n";	// mam 2 sek na wystartwanie
	        //	mComm.write(r);	
	        	try {
	        		newConnection.close();
	        		resetingConnection.open();
					resetingConnection.send(r);
					resetingConnection.close();
					newConnection.open();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    } );
		if(IspSettings.setHex){	
	        IspUploader.setHex( upanel_code );
	        try {
	            IspUploader.upload();
	        } catch (RuntimeException e) {
	            Initiator.logger.i(" prepareMB2.upload", e.toString());
	        }
		}
	}
}