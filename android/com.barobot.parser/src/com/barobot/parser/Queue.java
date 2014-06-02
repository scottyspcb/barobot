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
	protected final Object lock = new Object();				// protect wait_for_device var
	protected final Object lock_exec = new Object();		// protect exec method
	protected final Object lock_queue = new Object();		// protect output var
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
		if(isMainQueue){
			synchronized (this.lock_queue) {
				synchronized (this.lock) {
					this.output.clear();
					wait_for_device = false;
					mb.unlockRet("<clear>", true );
				}
			}
			
		}
	}

	public void destroy() {
		output.clear();
		if(isMainQueue){
			wait_for_device =  false;
			mb.destroy();
		}
		this.output		= null;
	}

	public void read( String in) {
		mb.read( in );
	}

	public void add( String command, boolean blocking) {
		AsyncMessage msg = null;
		synchronized (this.lock) {
			if(blocking){
				final String retcmd = "R" + command;
				msg = new AsyncMessage( command, true ){
					@Override
					public boolean isRet(String result, Queue q) {
				//		Initiator.logger.i("Queue.add.isRet?:", result  + " of " + command );
						if( retcmd.equals( result )){
							return true;
						}
						return false;
					}
				};
			}else{
				msg =new AsyncMessage( command, false );
			}
		}
		synchronized (this.lock_queue) {
			output.add(msg);
		}
	//	Initiator.logger.i("Queue.add2 length", ""+output.size() );
		exec();
	}
	public void add(String command, final String retcmd) {
		AsyncMessage msg = new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
		//		Initiator.logger.i("Queue.isRet?:", + result );
				if( retcmd.equals( result )){
					return true;
				}
				return false;
			}
		};

		synchronized (this.lock_queue) {
			output.add(msg);
		}
	//	Initiator.logger.i("Queue.add4 length", ""+output.size() );
		exec();
	}

	public void add(AsyncMessage asyncMessage) {
		synchronized (this.lock_queue) {
			output.add(asyncMessage);
		}
	//	Initiator.logger.i("Queue.add3 length", ""+output.size() );
		exec();
	}

	public void add(Queue q2) {
		synchronized (this.lock_queue) {
			output.addAll(q2.output);
		}
//		Initiator.logger.i("Queue.addAll length", ""+output.size() );
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
	//		Initiator.logger.i("Queue.run.wait_for1", ""+ wait_for_device_id);
			if( this.wait_for_device ){
			//	endRun();
		//		Initiator.logger.i("Queue.run.wait_for1 stop", ""+ wait_for_device_id);
				return;		// jestem w trakcie oczekiwania
			}else{
		//		Initiator.logger.w("Queue.run.wait_for1", "no blocked" + wait_for_device_id);	
			}
	//		show("exec");
	//		boolean wasEmpty = output.isEmpty();
			boolean isEmpty;
			synchronized (this.lock_queue) {
				isEmpty = !output.isEmpty();
			}
			while (isEmpty) {
				synchronized (this.lock) {
					if(this.wait_for_device ){		// is blocked
		//				Initiator.logger.i("Queue.wait_for2", "" + wait_for_device_id);
					//	endRun();
						return;
					}
				}
				AsyncMessage m = null;
				synchronized (this.lock_queue) {
					isEmpty = !output.isEmpty();
					if(isEmpty){
						break;
					}else{
						m	= output.pop();
					}
				}
//				Initiator.logger.i("Queue.run.start",  m.toString() );
				Queue nextq = m.start( mb, this );
			//	moveToHistory( m );
				if( nextq != null ){
					this.addFirst(nextq);	// add on front
				}
				if(m.wait4Finish()){
					synchronized (this.lock) {
						this.wait_for_device	= true;
					}
                	mb.waitFor( m );
            //    	endRun();
               // 	Initiator.logger.i("Queue.isBlocing true & return", m.toString() );
               // 	Initiator.logger.i("Queue.isBlocing", "" + wait_for_device_id );
                	return;
                }else{
                //	Initiator.logger.i("Queue.no Blocing", m.toString() );
                //	Initiator.logger.i("Queue.no Blocing", "" + wait_for_device_id );
                }
				synchronized (this.lock_queue) {
					isEmpty = !output.isEmpty();
				}
			}
		//	if(output.isEmpty()){
		//		Initiator.logger.i("Queue.run", "empty");
		//	}
		}
	}

	public void addFirst(Queue q2) {
		synchronized (this.lock_queue) {
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
		if(isMainQueue){
			synchronized (this.lock) {
				synchronized (this.lock_queue) {
					if(this.wait_for_device ){
			//			Initiator.logger.i("Queue", "unlock id:" + this.wait_for_device );
						mb.unlockRet("force unlock", false);
						this.wait_for_device =  false;
					}
				}
			}
			exec();
		}
	//	Initiator.logger.i("Queue", "unlock()");
	}
	public void addWaitThread(final Object thread) {
	//	Initiator.logger.i("Queue", "add thread wait");
		int size = 0;
		synchronized (this.lock_queue) {
			size = this.output.size();
		}
		synchronized(this.output){
			if( this.wait_for_device || size > 0 ){
				AsyncMessage msg = new AsyncMessage( true ){
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
				};
				synchronized (this.lock_queue) {
					output.add(msg);
				}
	//			Initiator.logger.i("Queue.addWaitThread length", ""+output.size() );

				exec();
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
			public String getName() {
				return "addWait2: " + time;
			}
			@Override
			public Queue run(Mainboard dev, Queue queue) {
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
		synchronized (this.lock_queue) {
			output.add( m2 );
		}
	//	Initiator.logger.i("Queue.addWait2 length", ""+output.size() );
	}
	public void addWait(final int time) {
		if(time <= 0 ){
			return;
		}
		AsyncMessage m2 = new AsyncMessage( true, true ) {
			@Override
			public String getName() {
				return "addWait" + time;
			}
			@Override
			public Queue run(final Mainboard dev, Queue queue) {
				Initiator.logger.w("Queue.addWait.run", "time: " +time);
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
	//			    	Initiator.logger.w("Queue.addWait.end", "time: " +time);
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
		synchronized (this.lock_queue) {
			output.add( m2 );
		}
//		Initiator.logger.i("Queue.addWait length", ""+output.size() );
	}
	public boolean isBusy() {
		synchronized (this.lock_queue) {
			synchronized (this.lock) {
				if( this.wait_for_device){
					return true;
				}
				if( this.output.size() > 0 ){
					return true;
				}
			}
		}
		return false;
	}

	public void show( String prefix ) {
		String res = "Queue (" + prefix + "):\n";
		synchronized (this.lock) {
			if(isMainQueue){
				if( this.wait_for_device){
					String s = mb.showWaiting();
					res += "\tWaiting for:"+  s + "\n";				
				}
			}
		}
		synchronized (this.lock_queue) {
			for (AsyncMessage msg : this.output){
				res += "\t" + msg.toString() + "\n";
			}
		}
		Initiator.logger.w("Queue.show", res);
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
