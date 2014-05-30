package com.barobot.isp;

import java.io.IOException;




import com.barobot.common.Initiator;
import com.barobot.common.IspSettings;
import com.barobot.common.constant.Methods;
import com.barobot.hardware.devices.BarobotConnector;
import com.barobot.hardware.devices.i2c.BarobotTester;
import com.barobot.hardware.devices.i2c.Carret;
import com.barobot.hardware.devices.i2c.I2C_Device;
import com.barobot.hardware.devices.i2c.MainboardI2c;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.isp.enums.Board;
import com.barobot.isp.enums.UploadErrors;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;
import com.barobot.common.IspOverSerial;

public class UploadCode {
	public void prepareUpanels(Hardware hw ) {
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
	//	prepareUpanel2( Upanel.BACK, q, hw.barobot, hw, false );
		prepareUpanel2( Upanel.FRONT, q, hw.barobot, hw, false );
	}
	private void prepareUpanel2(final int row, Queue q, final BarobotConnector barobot, final Hardware hw, final boolean firstOnly ) {
		hw.barobot.i2c.clear();

		final int current_index		= 0;
		final Upanel firstInRow	= new Upanel();
		firstInRow.setRow(row);
		firstInRow.setIndex(row);
		firstInRow.setNumInRow(current_index);

		String command = "N" + row;
		q.add( new AsyncMessage( command, true ){			// has first upanel?
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT  ){
						if(bytes[3] == 1 ){							// has next
							System.out.println("has next ROW "+row+"- OK");
							q.show("run");
							hw.barobot.i2c.add( firstInRow );
							q.addWait(100);
							Queue qq2	= UploadFirst( firstInRow, barobot, hw, firstOnly );
							q.addFirst(qq2);
						}else{
							System.out.println("ERROR: No device on ROW "+ row );
						}
						return true;
					}
				}
				return false;
			}
		});
	}

	private Queue UploadFirst(final Upanel current_dev, final BarobotConnector barobot, final Hardware hw, boolean firstOnly) {
		final String hex_code = current_dev.getHexFile();
		Queue nq = new Queue();
		if( IspSettings.setFuseBits ){
			nq.add( new AsyncMessage( true ){
				@Override	
				public String getName() {
					return "isp upanel setFuseBits start" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					Queue doAfter	= new Queue();
					current_dev.isp( doAfter );
					doAfter.add( new AsyncMessage( true ){
						@Override	
						public String getName() {
							return "isp upanel setFuseBits" ;
						}
						@Override
						public Queue run(Mainboard dev, Queue queue) {
							command = current_dev.setFuseBits( hw.comPort );
							Main.main.runCommand(command, hw);
							return null;
						}
					});
					return doAfter;
				}
			});
			nq.addWait(100);
		}
		if(IspSettings.setHex){
			nq.add( new AsyncMessage( true ){
				@Override	
				public String getName() {
					return "isp upanel setHex start" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					Queue doAfter	= new Queue();
					current_dev.isp( doAfter );
					doAfter.add( new AsyncMessage( true ){
						@Override	
						public String getName() {
							return "isp upanel setHex" ;
						}
						@Override
						public Queue run(Mainboard dev, Queue queue) {
							command = current_dev.uploadCode( hex_code, hw.comPort );
							Main.main.runCommand(command, hw);
							return null;
						}
					});
					return doAfter;
				}
			});
			nq.addWait(100);
		}
		if( !firstOnly ){
			final String resetCmd	= current_dev.getReset();
			nq.add( new AsyncMessage( resetCmd, true ){		// read address of the first upanel
				@Override
				public boolean onInput(String input, Mainboard dev, Queue mainQueue) {
					if( input.equals("R"+resetCmd) ){
						return true;		// its me, ignore message
					}
					return false;
				}
				@Override
				public boolean isRet(String result, Queue q) {
					if(result.startsWith(""+ Methods.METHOD_DEVICE_FOUND +",")){		//	112,18,19,1
						int[] bytes = Decoder.decodeBytes(result);	// HELLO, ADDRESS, TYPE, VERSION
						current_dev.setAddress(bytes[1]);
						System.out.println("+Upanel " + current_dev.getNumInRow() + " ma adres " + current_dev.getAddress());
						Queue qq2	= checkHasNext( hw, barobot, current_dev, hex_code );	
						current_dev.addLed( qq2, "22", 100 );	// na pocz¹tek daæ
						q.addFirst(qq2);
						return true;
					}
					return false;
				}
			});
			nq.addWait(100);
		}
		return nq;
	}

	private Queue checkHasNext( final Hardware hw, final BarobotConnector barobot, final Upanel current_dev, String upanel_code ) {
		Queue nq = new Queue();	
		final String nextCommand = "n" + current_dev.getAddress();
		nq.addWait(100);
		nq.add( new AsyncMessage( nextCommand, true ){			// has first upanel?
			@Override
			public boolean onInput(String input, Mainboard dev, Queue mainQueue) {
				if( input.equals("n"+nextCommand) ){
					return true;		// its me, ignore message
				}
				return false;
			}
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT  ){
						if(bytes[3] == 1 ){							// has next
							System.out.println("has next ROW "+current_dev.getAddress()+"- OK");
							Upanel next_device	= new Upanel();
							next_device.setRow( current_dev.getRow() );
							next_device.isResetedBy( current_dev );
							if( current_dev.getBottleNum() != -1 ){
								next_device.setNumInRow( current_dev.getBottleNum()+1 );
							}
							hw.barobot.i2c.add( next_device );
							Queue qq2	= UploadFirst( next_device, barobot, hw, false );
							q.addFirst(qq2);
						}else if( current_dev.getNumInRow() >= 5 ){	// all found
						}else{
							System.out.println("ERROR: No device after "+ current_dev.getAddress() );
						}
						return true;
					}
				}
				return false;
			}
		});
		return nq;
	}
	public void prepareCarret(final Hardware hw) {
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		hw.synchro();
		final Carret current_dev	= hw.barobot.i2c.carret;
		final String carret_code = current_dev.getHexFile();
		if( IspSettings.setFuseBits){
			q.addWait(100);
			q.add( new AsyncMessage( true ){
				@Override
				public String getName() {
					return "isp carret setFuseBits" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					Queue doAfter	= new Queue();
					current_dev.isp( doAfter );
					doAfter.add( new AsyncMessage( true ){
						@Override	
						public String getName() {
							return "isp carret setFuseBits" ;
						}
						@Override
						public Queue run(Mainboard dev, Queue queue) {
							command = current_dev.setFuseBits( hw.comPort );
							Main.main.runCommand(command, hw);
							return null;
						}
					});
					return doAfter;
				}
			});
		}
		if(IspSettings.setHex){
			q.addWait(100);
			q.add( new AsyncMessage( true ){
				@Override	
				public String getName() {
					return "isp carret setHex" ;
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					Queue doAfter	= new Queue();
					current_dev.isp( doAfter );

					doAfter.add( new AsyncMessage( true ){
						@Override	
						public String getName() {
							return "isp carret setHex" ;
						}
						@Override
						public Queue run(Mainboard dev, Queue queue) {
							command = current_dev.uploadCode( carret_code, hw.comPort );
							Main.main.runCommand(command, hw);
							return null;
						}
					});
					return doAfter;
				}
			});
		}
	}
	public void clearUpanel(Hardware hw) {
		Upanel[] list = hw.barobot.i2c.getUpanels();
		while(list.length > 0 ){
			boolean found = false;
			/*
			for (Upanel u : hw.barobot.i2c.list){
				if(u.have_reset_to == null ){
					 System.out.println("Rozpoczynam id " + u.getAddress() );
					 hw.barobot.i2c.list.remove(u);
					 u.can_reset_me_dev.have_reset_to = null;
					 found = true;
					 break;
				}
			}*/
			if(!found){
				System.out.println("Brak wêz³ów koñcowych" );
				break;
			}
		}
		System.out.println("Lista pusta" );
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	public void prepareMB(final Hardware hw ) {
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		hw.synchro();
		final I2C_Device current_dev	= new MainboardI2c();
		final String upanel_code = current_dev.getHexFile();
		q.add("", false);		
		q.add("PING", "PONG");
		q.addWaitThread(Main.mt);
		if(IspSettings.setHex){	
			current_dev.isp( q );	// mam 2 sek na wystartwanie
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public String getName() {
					return "prepareMB";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					command = current_dev.uploadCode( upanel_code, hw.comPort);
					Main.main.runCommand(command, hw);
					return null;
				}
			});
		}
		q.addWaitThread(Main.mt);
		hw.synchro();
	}

	
	
	private Uploader IspUploader;
    private Board mSelectedBoard;
    private IspOverSerial mSerial;
	
	public void prepareMB2(final Hardware hw) {	
		final Queue q = hw.getQueue();

		hw.connectIfDisconnected();
		hw.synchro();
		final I2C_Device current_dev	= new MainboardI2c();
		final String upanel_code		= current_dev.getHexFile();
		hw.synchro();
		q.addWaitThread(Main.mt);	

		mSerial			= new IspOverSerial(hw.getConnection());
		mSelectedBoard	= Board.BAROBOT_MAINBOARD;
        IspUploader		= new Uploader();
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
	        }
	        @Override
	        public void onCancel() {
	            Initiator.logger.i(" prepareMB2.upload","Cancel uploading");
	        }
	        @Override
	        public void onError(UploadErrors err) {
	            Initiator.logger.i(" prepareMB2.upload","Error  : "+err.toString());
	        }
	        @Override
	        public void resetDevice(boolean reset ){
	        	current_dev.isp( q );	// mam 2 sek na wystartwanie
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
		q.addWaitThread(Main.mt);
	}

	
	public void checkCarret(Hardware hw) {
		String command = "";
		Queue q = hw.getQueue();
		hw.connectIfDisconnected();
		//I2C_Device current_dev	= new Upanel( 3, 0 );
		I2C_Device current_dev	= hw.barobot.i2c.carret;
		current_dev.isp( q );
		command = current_dev.checkFuseBits( hw.comPort );
		Main.main.runCommand(command, hw);
	}

	public void prepareMBManualReset(final Hardware hw) {
		String command					= "";
		final I2C_Device current_dev	= new MainboardI2c();
		final String upanel_code		= current_dev.getHexFile();
		if(IspSettings.setHex){	
			command = current_dev.uploadCode(upanel_code, hw.comPort);
			Main.main.runCommand(command, hw);
		}
	}

	public void prepareSlaveMB(final Hardware hw) {
		final I2C_Device current_dev	= new BarobotTester();
		Queue q = hw.getQueue();

		if(IspSettings.setFuseBits){
			hw.connectIfDisconnected();
			hw.synchro();
			q.addWaitThread(Main.mt);
			q.addWait(100);
			current_dev.isp( q );
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public String getName() {
					return "prepareSlaveMB2";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					String command = current_dev.setFuseBits( hw.comPort );
					Main.main.runCommand(command, hw);
					return null;
				}
			});
			hw.closeOnReady();
			q.addWaitThread(Main.main);
		}
		if(IspSettings.setHex){	
			hw.connectIfDisconnected();
			hw.synchro();
			current_dev.isp( q );	// mam 2 sek na wystartwanie
			q.add( new AsyncMessage( true ){		// na koncu zamknij
				@Override
				public String getName() {
					return "prepareSlaveMB";
				}
				@Override
				public Queue run(Mainboard dev, Queue queue) {
					command = current_dev.uploadCode( current_dev.getHexFile(), hw.comPort);
					Main.main.runCommand(command, hw);
					return null;
				}
			});
			hw.closeOnReady();
			q.addWaitThread(Main.main);
		}
	}
	public void prepare1Upanel(final Hardware hw, final BarobotConnector barobot, final int row ) {
		hw.connectIfDisconnected();
		Queue q = hw.getQueue();
		prepareUpanel2( row, q, hw.barobot, hw, true );	
	}
	

	public void prepareUpanelNextTo(final Hardware hw, final int nexto ) {
		final Upanel prev			= new Upanel();
		prev.setAddress(nexto);
		final String hex_code		= prev.getHexFile();

		hw.connectIfDisconnected();
		Queue q = hw.getQueue();
		String command = "n" + nexto;
		q.addWait(100);
		q.add( new AsyncMessage( command, true ){			// has first upanel?
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT  ){
						if(bytes[3] == 1 ){							// has next
							q.show("run");
							Queue qq2	= checkHasNext( hw, hw.barobot, prev, hex_code );
							qq2.addWait(100);
							q.addFirst(qq2);
						}else{
							System.out.println("ERROR: No device next to "+nexto );
						}
						return true;
					}
				}
				return false;
			}
		});
	}
}
