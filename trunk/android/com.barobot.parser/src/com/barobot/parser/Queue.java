package com.barobot.parser;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import com.barobot.common.Initiator;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.Mainboard;

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
//	private static LinkedList<Mainboard> devs = new LinkedList<Mainboard>();
	protected final Object lock = new Object();
	protected final Object lock_exec = new Object();
	protected LinkedList<AsyncMessage> output = new LinkedList<AsyncMessage>();
	protected boolean wait_for_device = false;
	protected boolean isMainQueue = false;
	protected Mainboard mb;

	public Queue(){
	}

	public Queue(Mainboard mb2){
		this.isMainQueue	= true;
		this.mb				= mb2;
		mb.setMainQueue( this );
	}

	public void clear() {
		Initiator.logger.i("Queue","clearAll");
		synchronized (this.lock) {
			mb.unlockRet("<clear>", true );
			wait_for_device = false;
			this.output.clear();
		}
	}
	public void destroy() {
		wait_for_device =  false;
		mb.destroy();
		this.output		= null;
	}

	public void read( String in) {
		mb.read( in );
	}

	public void add( String command, boolean blocking) {
		synchronized (this.lock) {
			if(blocking){
				final String retcmd = "R" + command;
				output.add(new AsyncMessage( command, true ){
					@Override
					public boolean isRet(String result, Queue q) {
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
	public void add(String command, final String retcmd) {
		synchronized (this.lock) {
			output.add(new AsyncMessage( command, true ){
				@Override
				public boolean isRet(String result, Queue q) {
			//		Initiator.logger.i("Queue.isRet?:", + result );
					if( retcmd.equals( result )){
						return true;
					}
					return false;
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
		synchronized (this.lock) {
			output.addAll(q2.output);
		}
		exec();
	}
	public void exec() {
		if(!isMainQueue){
			return;
		}
		if(Thread.holdsLock(this.lock_exec)){	// don't do it if other is doing
			return;
		}
		synchronized (this.lock_exec) {
			synchronized (this.lock) {
		//		Initiator.logger.i("Queue.run.wait_for1", ""+ wait_for_device_id);
				if(this.wait_for_device ){
				//	endRun();
			//		Initiator.logger.i("Queue.run.wait_for1 stop", ""+ wait_for_device_id);
					return;		// jestem w trakcie oczekiwania
				}else{
			//		Initiator.logger.w("Queue.run.wait_for1", "no blocked" + wait_for_device_id);	
				}
		//		show("exec");
		//		boolean wasEmpty = output.isEmpty();
				while (!output.isEmpty()) {
					if(this.wait_for_device ){		// 0 or more is blocked
		//				Initiator.logger.i("Queue.wait_for2", "" + wait_for_device_id);
					//	endRun();
						return;
					}
					AsyncMessage m	= output.pop();
	//				Initiator.logger.i("Queue.run.start",  m.toString() );
					Queue nextq = m.start( mb, this );
				//	moveToHistory( m );
					if( nextq != null ){
						this.addFirst(nextq);	// add on front
					}
					if(m.wait4Finish()){
	                	this.wait_for_device	= true;
	                	mb.waitFor( m );
	            //    	endRun();
	                	Initiator.logger.i("Queue.isBlocing true & return", m.toString() );
	               // 	Initiator.logger.i("Queue.isBlocing", "" + wait_for_device_id );
	                	return;
	                }else{
	                	Initiator.logger.i("Queue.no Blocing", m.toString() );
	                //	Initiator.logger.i("Queue.no Blocing", "" + wait_for_device_id );
	                }
	
				}
			//	if(output.isEmpty()){
			//		Initiator.logger.i("Queue.run", "empty");
			//	}
			}
		}
	//	endRun();
	}
	public void addFirst(Queue q2) {
		synchronized (this.lock) {
			this.output.addAll( 0, q2.output);		// add on start
			/*
			if(isMainQueue ){
				Initiator.logger.i("Queue.addFirst", "newsize: "+ this.output.size() );
			}*/
	//		show("addFirst");
		}
		exec();
	}

    public void sendNow( String command ) {	 // send without waiting
		synchronized (this.lock) {
			mb.send(command + "\n");
		}
    }
	public void unlock() {
		synchronized (this.lock) {
			if(this.wait_for_device ){
			//	Initiator.logger.i("Queue", "unlock id:" + this.wait_for_device_id );
				mb.unlockRet("force unlock", false);
				this.wait_for_device =  false;
			}
		}
	//	Initiator.logger.i("Queue", "unlock()");
		exec();
	}
	public void addWaitThread(final Object thread) {
	//	Initiator.logger.i("Queue", "add thread wait");
		synchronized(this.output){
			if( this.wait_for_device || this.output.size() > 0 ){
				this.add( new AsyncMessage( true ){
					@Override
					public String getName() {
						return "WAIT4THREAD";
					}
					@Override
					public Queue run(Mainboard dev, Queue queue) {
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
			@Override
			public Queue run(Mainboard dev, Queue queue) {
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
			public boolean isRet(String result, Queue mainQueue) {
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
			public Queue run(final Mainboard dev, Queue queue) {
				Initiator.logger.w("Queue.addWait.run", "time: " +time);
				this.name				= "wait " + time;
				final AsyncMessage msg	= this;
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
				    	Initiator.logger.w("Queue.addWait.end", "time: " +time);
				    	dev.unlockRet( msg, "wait " + time);
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
	public boolean isBusy() {
		synchronized (this.lock) {
			if( this.wait_for_device){
				return true;
			}
			if( this.output.size() > 0 ){
				return true;
			}
		}
		return false;
	}

	public void show( String prefix ) {
		String res = "Queue (" + prefix + "):\n";
		synchronized (this.lock) {
			for (AsyncMessage msg : this.output){
				res += "\t" + msg.toString() + "\n";
			}	
			Initiator.logger.w("Queue.show", res);
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
