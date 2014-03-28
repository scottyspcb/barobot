package com.barobot.hardware.devices;

import com.barobot.common.constant.Methods;
import com.barobot.hardware.devices.i2c.Upanel;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class LedOrder {
	private Queue mq;
	private OnReadyListener<LedOrder> onReadyListener;
	private int num = 0;
	private BarobotConnector barobot;

	public LedOrder() {
	}
	// row		- rz¹d Back(3) lub Front(4)
	// rowIndex	- kod rzêdu 0 - Back, 1 Front
	// index	- numer upanelu kolejno liczony po butelkach od 0 czyli 0-11
	// num		- numer upanelu liczony od pocz¹tku do konca rzedu i nastepnie przez drugi rzad
	// inrow	- numer butelki kolejno w rzêdzie 0-5
	// address	- adres i2c

	public void asyncStart(BarobotConnector barobot, Queue q) {
		this.mq = q;
		this.barobot = barobot;	

		Thread t = new Thread( new Runnable(){
			public synchronized void run() {
				synchronized(LedOrder.this){
					LedOrder.this.findOrder( Upanel.BACK  );	
					LedOrder.this.findOrder( Upanel.FRONT  );
					LedOrder.this.mq.add( new AsyncMessage( true ){
						@Override	
						public String getName() {
							return "onReady LedOrder" ;
						}
						@Override
						public Queue run(Mainboard dev, Queue queue) {
							onReadyListener.onReady(LedOrder.this);
							return null;
						}
					});
					LedOrder.this.mq.show("LedOrder");
				}
			}
		});
		t.start();
	}
	private void findOrder(final int row ) {
		final Queue nq3 = new Queue();
		nq3.add( new AsyncMessage( "RESET"+ row, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith(""+ Methods.METHOD_DEVICE_FOUND +",")){		//	112,18,19,1
					int[] bytes = Decoder.decodeBytes(result);	// HELLO, ADDRESS, TYPE, VERSION

					Upanel u = new Upanel();
					u.setAddress( bytes[1] );
					u.setIndex( row );	// first in row
					u.setRow( row );
					u.setInRow( num );

					barobot.i2c.add(u);

					System.out.println("UPANEL dla butelki "+ u.getBottleNum() 
							+" o numerze: " + u.getInRow() 
							+" w rzedzie: " + u.getRow()
							+ " ma adres:" + u.getAddress() );
					num++;

					Queue q2 = resetNextTo( u );
					q.addFirst(q2);
					return true;
				}
				return false;
			}
		});
		String command = "N" + row;
		this.mq.add( new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT  ){
						if(bytes[3] == 1 ){							// has next
							System.out.println("has next ROW "+row+"- OK");
							q.show("run");
							num		= 0;
							q.addFirst(nq3);
						}
						return true;
					}
				}
				return false;
			}
		});
	}
	public Queue resetNextTo( final Upanel u) {
		final Queue nq = new Queue();
		String command2 = "RESET_NEXT"+ u.getAddress();
		nq.add( new AsyncMessage( command2, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith(""+ Methods.METHOD_DEVICE_FOUND +",")){		//	112,18,19,1
					int[] bytes = Decoder.decodeBytes(result);	// HELLO, ADDRESS, TYPE, VERSION

					Upanel u2 = new Upanel();
					u2.setAddress( bytes[1] );
					u2.setIndex( 0 );
					u2.setRow( u.getRow() );
					u2.setInRow( num );
					u2.canResetMe(u);
					barobot.i2c.list.add(u2);

					System.out.println("UPANEL dla butelki "+ u2.getBottleNum() 
							+" o numerze: " + u2.getInRow() 
							+" w rzedzie: " + u2.getRow()
							+ " ma adres:" + u2.getAddress() );
			
					num++;
					Queue q2 = resetNextTo( u2 );
					q.addFirst(q2);
					return true;
				}
				return false;
			}
		});
		Queue nq2 = new Queue();
		nq2.add( new AsyncMessage( "n" + u.getAddress(), true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT  ){
						if(bytes[3] == 1 ){							// has next
							q.addFirst(nq);
						}else{
							System.out.println("koniec upaneli na" + u.getAddress() );
						}
						return true;
					}
				}
				return false;
			}
		});
		return nq2;
	}
	public void addOnReady(OnReadyListener<LedOrder> onReadyListener) {
		this.onReadyListener = onReadyListener;
	}
}


