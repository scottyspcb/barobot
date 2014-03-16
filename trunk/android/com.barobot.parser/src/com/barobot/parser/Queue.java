package com.barobot.parser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import com.barobot.common.Initiator;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.output.AsyncDevice;


/*
 * LinkedList<E>
    get(int index) is O(n)
    add(E element) is O(1)
    add(int index, E element) is O(n)
    remove(int index) is O(n)
    Iterator.remove() is O(1) <--- main benefit of LinkedList<E>
    ListIterator.add(E element) is O(1) <--- main benefit of LinkedList<E>

ArrayList<E>
    get(int index) is O(1) <--- main benefit of ArrayList<E>
    add(E element) is O(1) amortized, but O(n) worst-case since the array must be resized and copied
    add(int index, E element) is O(n - index) amortized, but O(n) worst-case (as above)
    remove(int index) is O(n - index) (i.e. removing last is O(1))
    Iterator.remove() is O(n - index)
    ListIterator.add(E element) is O(n - index)
 */

public class Queue {
	private static LinkedList<AsyncDevice> devs = new LinkedList<AsyncDevice>();
	
	public static final int DFAULT_DEVICE = 0;
	protected final Object lock = new Object();
	protected LinkedList<AsyncMessage> output = new LinkedList<AsyncMessage>();
	protected int wait_for_device_id = -1;
	private boolean isMainQueue = false;
	//private Thread t;

	public Queue( boolean isMainQueue ){
		this.isMainQueue = isMainQueue;
	}
	public Queue(){
		//	t = new Thread(this);
		}
	
	public static int registerSource( AsyncDevice dev ) {
	//	synchronized(devs){
			devs.add(dev);
			return devs.size() - 1;
	//	}
	}
	public static AsyncDevice getDevice(int devindex) {
		return devs.get(devindex);
	}
	public void clear(int devindex ) {
		synchronized (this.lock) {
			Initiator.logger.i("Queue.clear", ""+devindex );
			AsyncDevice dev = devs.get(devindex);
			dev.unlockRet("<close>");
			wait_for_device_id = -1;
			this.output.clear();
		}
	}
	public void clearAll() {
		Initiator.logger.i("Queue","clearAll");
		synchronized (this.lock) {
			wait_for_device_id = -1;
			for (AsyncDevice dev : devs){
				dev.clear();
			}
		}
	}

	public void read(int sourceid, String in) {
		synchronized (this.lock) {
			devs.get(sourceid).read( in );
		}
	}
	public void add(final int sourceid, final String command, boolean blocking) {
		synchronized (this.lock) {
			if(blocking){
				final String retcmd = "R" + command;
				output.add(new AsyncMessage( command, blocking ){
					public boolean isRet(String result) {
						if( retcmd.equals(result)){
						//	Initiator.logger.i("Queue", "isret: " + result+" for "  + command);
							return true;
						}
						return false;
					}
					public int getDeviceId() {
						return sourceid;
					}
				});
			}else{
				output.add(new AsyncMessage( command, blocking ));
			}
		}
		exec();
	}
	public void add( String command, boolean blocking) {
		synchronized (this.lock) {
			if(blocking){
				final String retcmd = "R" + command;
				output.add(new AsyncMessage( command, true ){
					public boolean isRet(String result) {
				//		Initiator.logger.i("Queue.add.isRet?:", result  + " of " + command );
						if( retcmd.equals( result )){
							return true;
						}
						return false;
					}
				});
			}else{
				output.add(new AsyncMessage( command, false ));
			}
		}
		exec();
	}
	public void add(final int sourceid, String command, final String retcmd) {
		synchronized (this.lock) {
			output.add(new AsyncMessage( command, true ){
				public boolean isRet(String result) {
			//		Initiator.logger.i("Queue.isRet?:", + result );
					if( retcmd.equals( result )){
						return true;
					}
					return false;
				}
				public int getDeviceId() {
					return sourceid;
				}
			});
		}
		exec();
	}

	public void add(AsyncMessage asyncMessage) {
		synchronized (this.lock) {
			output.add(asyncMessage);
		}
		exec();
	}

	public void add(Queue q2) {
	//	synchronized (this.lock) {
			output.addAll(q2.output);
	//	}
		exec();
	}	
	/*
	private void exec() {
		synchronized(this.t){
			t.
			if(!t.isAlive() && !running){
				Initiator.logger.i("Queue.exec.start", "start"+ (t.isAlive() ? "A" :"nA")  );
				running = true;
				t.start();
			}else{
				Initiator.logger.i("Queue.exec.start", "no start" );
			}
		}
	}
	private void endRun() {
		synchronized(this.t){
			running = false;
		}
	}
	boolean running = false;*/

	public void exec() {
		if(!isMainQueue){
			return;
		}
		synchronized (this.lock) {
	//		Initiator.logger.i("Queue.run.wait_for1", ""+ wait_for_device_id);
			if(this.wait_for_device_id > -1 ){
			//	endRun();
	//			Initiator.logger.i("Queue.run.wait_for1 stop", ""+ wait_for_device_id);
				return;		// jestem w trakcie oczekiwania
			}else{
	//			Initiator.logger.w("Queue.run.wait_for1", "no blocked" + wait_for_device_id);	
			}
	//		boolean wasEmpty = output.isEmpty();
			while (!output.isEmpty()) {
				if(this.wait_for_device_id > -1 ){		// 0 or more is blocked
	//				Initiator.logger.i("Queue.wait_for2", "" + wait_for_device_id);
				//	endRun();
					return;
				}
				AsyncMessage m = output.pop();
				AsyncDevice dev = m.getDevice();
				if(dev!=null){
	//				Initiator.logger.i("Queue.run.start",  m.toString() );
					Queue nextq = m.start( dev );
					moveToHistory( m );
					if( nextq != null ){
						this.addFirst(nextq);	// add on front
					}
					if(m.wait4Finish()){
	                	this.wait_for_device_id	= m.getDeviceId();
	                	dev.waitFor(m, this );
	            //    	endRun();
	                //	Initiator.logger.i("Queue.isBlocing true & return", m.toString() );
	                //	Initiator.logger.i("Queue.isBlocing", "" + wait_for_device_id );
	                	return;
	                }else{
	                	this.wait_for_device_id	= -1;
	                //	Initiator.logger.i("Queue.no Blocing", m.toString() );
	                //	Initiator.logger.i("Queue.no Blocing", "" + wait_for_device_id );
	                }
				}
			}
			if(output.isEmpty()){
	//			Initiator.logger.i("Queue.run", "empty");
			}
		}
	//	endRun();
	}


	public void addFirst(Queue q2) {
		synchronized (this.lock) {
			this.output.addAll( 0, q2.output);		// add on start	
		}
		exec();
	}

    public void sendNow( int devindex, String command ) throws IOException {	 // send without waiting
		synchronized (this.lock) {
			devs.get(devindex).send(command + "\n");
		}
    }
	public void unlock() {
		synchronized (this.lock) {
			if(this.wait_for_device_id != -1 ){
				AsyncDevice dev = devs.get(this.wait_for_device_id);
				Initiator.logger.i("Queue", "unlock id:" + this.wait_for_device_id );
				dev.unlockRet("force unlock");
				this.wait_for_device_id = -1;
			}
		}
		Initiator.logger.i("Queue", "unlock()");
		exec();
	}

	private void moveToHistory(AsyncMessage m) {
		// TODO Auto-generated method stub
	}
	
	public void addWaitThread(final Object thread) {
	//	Initiator.logger.i("Queue", "add thread wait");
		synchronized(this.output){
			if( this.wait_for_device_id >= 0 || this.output.size() > 0 ){
				this.add( new AsyncMessage( true ){
					@Override
					public Queue run(AsyncDevice dev) {
			//			Initiator.logger.i("Queue","thread notify");
						synchronized(thread){
							thread.notify();
						}
						return null;
					}
				});
			//	Initiator.logger.i("Queue", "START wait");
				synchronized(thread){
					try {
						thread.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	//	Initiator.logger.i("Queue","end wait");
	}

	public void addWait2(final int time) {
		final AsyncMessage m2 = new AsyncMessage( true ) {
			public Queue run(AsyncDevice dev) {
				this.name				= "wait " + time;
				return null;
			}
			@Override
			public long getTimeout() {
				return time;
			}
			@Override
			public boolean wait4Finish() {
				return true;
			}
			@Override
			public boolean isRet(String result) {
				return false;
			}
			@Override
			public void afterTimeout() {
				this.unlocking_command = "end";
			}
		};
		output.add( m2 );		
	}
	public void addWait(final int time) {
		final AsyncMessage m2 = new AsyncMessage( true, true ) {
			@Override
			public Queue run(final AsyncDevice dev) {
				Initiator.logger.w("Queue.addWait.run", "time: " +time);
				this.name				= "wait " + time;
				/*
				final Handler handler	= new Handler();
				handler.postDelayed(new Runnable() {
				  @Override
				  public void run() {
					  ar.unlock( m3 );
				  }
				}, time);			
				*/
				new Timer().schedule(new TimerTask() {          
				    public void run() {
				    	Initiator.logger.w("Queue.addWait.schedule", "time: " +time);
				    	dev.unlockRet("wait " + time);
				    }
				}, time);// odczekaj tyle czasu, odblokuj kolejkê i jedz dalej
				return null;
			}
			@Override
			public boolean wait4Finish() {
				return true;
			}
		};
		output.add( m2 );		
	}

	public static void disableDevice(int mainboardSource) {
		getDevice(mainboardSource).disable();
	}
	public static void enableDevice(int mainboardSource) {
		getDevice(mainboardSource).enable();
	}
	public boolean isBusy() {
		synchronized (this.lock) {
			if( this.wait_for_device_id >= 0){
				return true;
			}
			if( this.output.size() > 0 ){
				return true;
			}
		}
		return false;
	}
	public void destroy() {
		synchronized (this.lock) {
			wait_for_device_id = -1;
			for (AsyncDevice dev : devs){
				dev.destroy();
			}
			this.output.clear();
			devs.clear();
		}
	}

}




/*
final Handler handler	= new Handler();
handler.postDelayed(new Runnable() {
  @Override
  public void run() {
	  ar.unlock( m3 );
  }
}, time);			

new Timer().schedule(new TimerTask() {          
    public void run() {
    	 ar.unlock( m3 );
    }
}, time);// odczekaj tyle czasu, odblokuj kolejkê i jedz dalej
return null;

*/
