package com.barobot.isp;

import com.barobot.common.constant.Methods;
import com.barobot.parser.Queue;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;
import com.barobot.parser.utils.Decoder;

public class LedOrder {
	public static int[] upanelIndex2order = {0,2,4,6,8,10,1,3,5,7,9,11};	// numer butelki
	public static int[] order2upanelIndex = {0,6,1,7,2,8,3,9,4,10,5,11};	// numer butelki
	
	private Queue mq;
	private OnReadyListener<LedOrder> onReadyListener;
	public int[] upanels = {
			12,20,23,19,18,17,15,16,21,22,13,14,
		};
	public int[][] upanels_by_row = {
			{15,16,21,22,13,14},{12,20,23,19,18,17},
		};
	private boolean hasNext[] = {false, false};
	private int inrows[] = {0, 0};
	private int num = 0;
	public LedOrder(Queue q) {
		this.mq = q;
	}

	// row		- rz¹d Back(3) lub Front(4)
	// rowIndex	- kod rzêdu 0 - Back, 1 Front
	// index	- numer upanelu kolejno liczony po butelkach od 0 czyli 0-11
	// num		- numer upanelu liczony od pocz¹tku do konca rzedu i nastepnie przez drugi rzad
	// inrow	- numer butelki kolejno w rzêdzie 0-5
	// address	- adres i2c

	public void asyncStart() {
		Thread t = new Thread( new Runnable(){
			public synchronized void run() {
				synchronized(LedOrder.this){
					LedOrder.this.findOrder( 0, 3 );		// Back
				//	LedOrder.this.findOrder( 1, 4 );		// Front
					mq.add( new AsyncMessage( true ){
						@Override	
						public String getName() {
							return "onReady" ;
						}
						@Override
						public Queue run(Mainboard dev, Queue queue) {
							onReadyListener.onReady(LedOrder.this);
							return null;
						}
					});
					mq.show("LedOrder");

				}
			}
		});
		t.start();
	}

	public void addOnReady(OnReadyListener<LedOrder> onReadyListener) {
		this.onReadyListener = onReadyListener;
	}
	public int[] orderByNum() {
		return upanels;
	}

	private void findOrder(final int rowIndex, final int row ) {
		hasNext[rowIndex] = false;
		String command = "N" + row;
		mq.add( new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT  ){
						if(bytes[3] == 1 ){							// has next
							hasNext[rowIndex] = true;
						}
						return true;
					}
				}
				return false;
			}
		});
		mq.add( new AsyncMessage( true ){
			@Override
			public String getName() {
				return "hasNext"+ row ;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue ) {
				if( hasNext[rowIndex] ){
					System.out.println("has next ROW "+row+"?" + (hasNext[rowIndex] ? "1" : "0"));
					queue.show("run");
					return resetIndex(rowIndex, row);
				}
				return null;
			}
		});
	}
	private Queue resetIndex( final int rowIndex, final int row) {
		String command = "RESET"+ row;
		Queue nq = new Queue();
		nq.add( new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith(""+ Methods.METHOD_DEVICE_FOUND +",")){		//	112,18,19,1
					int[] bytes = Decoder.decodeBytes(result);	// HELLO, ADDRESS, TYPE, VERSION
					int inrow = inrows[rowIndex];
					System.out.println("UPANEL o numerze "+ inrows[rowIndex] +" w zedzie: " +row + " ma adres:" +  bytes[1] );
					upanels_by_row[rowIndex][inrow] = bytes[1];
					int index	= order2upanelIndex[num];
					upanels[index] =  bytes[1];
					inrows[rowIndex]++;
					num++;
					Queue q2 = resetNextToAddress( bytes[1], row, rowIndex );
					q.addFirst(q2);
					return true;
				}
				return false;
			}
		});
		return nq;
	}
	public Queue resetNextToAddress( final int address, final int row, final int rowIndex) {
		final Queue nq = new Queue();
		String command2 = "RESET_NEXT"+ address;
		nq.add( new AsyncMessage( command2, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith(""+ Methods.METHOD_DEVICE_FOUND +",")){		//	112,18,19,1
					int[] bytes = Decoder.decodeBytes(result);	// HELLO, ADDRESS, TYPE, VERSION
					int inrow = inrows[rowIndex];
					System.out.println("UPANEL o numerze "+ inrows[rowIndex] +" w zedzie: " +row + " ma adres:" +  bytes[1] );
					upanels_by_row[rowIndex][inrow] = bytes[1];
					int index	= order2upanelIndex[num];
					upanels[index] =  bytes[1];
					inrows[rowIndex]++;
					num++;
					Queue q2 = resetNextToAddress( bytes[1], row, rowIndex );
					q.addFirst(q2);
					return true;
				}
				return false;
			}
		});
		Queue nq2 = new Queue();
		String command = "n" + address;
		nq2.add( new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				if(result.startsWith("" + Methods.METHOD_I2C_SLAVEMSG + ",")){		//	122,1,188,1
					int[] bytes = Decoder.decodeBytes(result);
					if(bytes[2] == Methods.METHOD_CHECK_NEXT  ){
						if(bytes[3] == 1 ){							// has next
							hasNext[rowIndex] = true;
							q.addFirst(nq);
						}else{
							System.out.println("koniec upaneli na" + address );
							
						}
						return true;
					}
				}
				return false;
			}
		});
		return nq2;
	}
}

