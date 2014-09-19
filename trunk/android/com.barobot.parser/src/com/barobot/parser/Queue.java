package com.barobot.parser;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import com.barobot.common.Initiator;
import com.barobot.parser.message.AsyncMessage;
import com.barobot.parser.message.LimitedBuffer;
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

/*
 * Queue
 * 
 * 0		- next to send, addFirst()
 * 1
 * 2
 * 3
 * 4
 * 5
 * 6		- last to send, add()
 * 
 * */

public class Queue {
	protected static final Object lock_output			= new Object();	// protect output var
	protected LinkedList<AsyncMessage> output		= new LinkedList<AsyncMessage>();
	protected boolean isMainQueue = false;
	protected Mainboard mb;
	private LimitedBuffer<AsyncMessage> history		= new LimitedBuffer<AsyncMessage>(500);

	public Queue(){
	}

	public Queue(Mainboard mb2){
		this.isMainQueue	= true;
		this.mb				= mb2;
		mb.setMainQueue( this );
		this.worker.start();
	}

	private int timeout = 5000;
	private int try_count = 0;
	private int try_max = 5;
	private AsyncMessage last_msg = null;
	private boolean canExec = false;
	private boolean writing = true;
	private Object exec_lock = new Object();
	private int ticks = 0;

	private Thread worker = new Thread( new Runnable() {
		@Override
		public void run() {
			while(writing){
				if( !canExec ){
					synchronized (exec_lock) {
						try {
							Initiator.logger.i("Queue.worker.wait", "start + " + ( output.isEmpty() ? "empty" : "not empty" ));	
							exec_lock.wait(timeout);
							Initiator.logger.i("Queue.worker.wait", "end + " + ( output.isEmpty() ? "empty" : "not empty" ));	
			//				show("exec");
							
						} catch (InterruptedException e) {
							Initiator.logger.e("Queue.worker.exec_lock.wait", "InterruptedException", e);	
						} catch (Exception e) {
							Initiator.logger.e("Queue.worker.exec_lock.wait", "Exception", e);	
						}
					}
				}
				/*
				Initiator.logger.e("Queue.worker.tick", ""+ticks);
				if( last_msg != null && last_msg.equals(obj)){
					try_count++;
				}else{
				}
				ticks++;
				
				if(!lock.wait_for_device){
					Initiator.logger.i("Queue.run.wait_for1 stop", ""+ lock.wait_for_device);
					show("exec");
					continue;
				}*/

				boolean isEmpty;
				AsyncMessage m = null;
				synchronized (QueueLock.lock_wait_for) {
					if(Mainboard.lock.wait_for != null){
						isEmpty = true;
					}else{
						synchronized (lock_output) {
							isEmpty = output.isEmpty();
							if(!isEmpty){
								m = output.pop();
								last_msg = m;
							}
						}
					}
				}

				while(!isEmpty){
					this.exec2(m);
					synchronized (QueueLock.lock_wait_for) {
						if(Mainboard.lock.wait_for!= null){
							isEmpty = true;
						}else{
							synchronized (lock_output) {
								isEmpty = output.isEmpty();
								if(!isEmpty){
									m = output.pop();
									last_msg = m;
								}
							}
						}
					}
					if(isEmpty){
						canExec = false;
					}
				}
			}
			Initiator.logger.e("Queue.worker.finish", ""+ticks);	
		}

		private void exec2(AsyncMessage m) {
		//	Initiator.logger.i("Queue.run.start",  m.toString() );
			synchronized (QueueLock.lock_wait_for) {
					Queue nextq = m.start( mb, Queue.this );
					if( nextq != null ){
						addFirst(nextq);	// add on front
					}
					if(m.wait4Finish()){
		            	//	Initiator.logger.i( "waitFor:", "["+m.toString()+"]" );
		        		Mainboard.lock.wait_for		= m;
		        		m.setWaiting( true );
		        		canExec = false;
	
		        //    	endRun();
		           // 	Initiator.logger.i("Queue.isBlocing true & return", m.toString() );
		           // 	Initiator.logger.i("Queue.isBlocing", "" + lock.wait_for_device_id );
		            }else{
		            //	Initiator.logger.i("Queue.no Blocing", m.toString() );
		            //	Initiator.logger.i("Queue.no Blocing", "" + lock.wait_for_device_id );
		            }
					//	if(output.isEmpty()){
					//		Initiator.logger.i("Queue.run", "empty");
					//	}
			}
		}
	});

	public void clear() {
		Initiator.logger.i("Queue","clearAll");
		if(isMainQueue){
			synchronized (lock_output) {
				this.output.clear();
			}
			Mainboard.lock.unlock("<clear>", this);
		}
	}

	public void destroy() {
		writing = false;
		output.clear();
		if(isMainQueue){
			canExec = true;
			synchronized (exec_lock) {
				exec_lock.notify();
			}
			mb.destroy();
		}
	}

	public void read( String in) {
		mb.read( in );
	}

	public void add( String command, boolean blocking) {
		AsyncMessage msg = null;
		final String retcmd = "R" + command;
		if(blocking){
			msg = new AsyncMessage( command, true ){
				@Override
				public boolean isRet(String result, Queue q) {
					Initiator.logger.i("Queue.add.isRet1?:", result  + " of " + command );
					if( retcmd.equals( result )){
						return true;
					}
					return false;
				}
			};
		}else{
			msg =new AsyncMessage( command, false );
		}
		synchronized (lock_output) {
			output.add(msg);
			if(isMainQueue){
				history.push(msg);
			}
		}
	//	Initiator.logger.i("Queue.add2 length", ""+output.size() );
	//	Initiator.logger.i("Queue.add2", "exec" );
		exec();
	}
	public void add(String command, final String retcmd) {
		AsyncMessage msg = new AsyncMessage( command, true ){
			@Override
			public boolean isRet(String result, Queue q) {
				Initiator.logger.i("Queue.isRet2?:", retcmd );
				if( retcmd.equals( result )){
					return true;
				}
				return false;
			}
		};
		synchronized (lock_output) {
			output.add(msg);
			if(isMainQueue){
				history.push(msg);
			}
		}
	//	Initiator.logger.i("Queue.add4 length", ""+output.size() );
		exec();
	}

	public void add(AsyncMessage asyncMessage) {
		synchronized (lock_output) {
			output.add(asyncMessage);
			if(isMainQueue){
				history.push(asyncMessage);
			}
		}
	//	Initiator.logger.i("Queue.add3 length", ""+output.size() );
		exec();
	}

	public void add(Queue q2) {
		synchronized (lock_output) {
			output.addAll(q2.output);
			if(isMainQueue){
				history.addAll(q2.output);
			}
		}
//		Initiator.logger.i("Queue.addAll length", ""+output.size() );
		exec();
	}
	public void addFirst(Queue q2) {
		synchronized (lock_output) {
			this.output.addAll( 0, q2.output);		// add on start
			if(isMainQueue){
				history.addBefore( this.output.peek(), q2.output);
			}
		}
		exec();
	}

	protected void exec() {
		if(isMainQueue){
			if(Mainboard.lock.wait_for == null){
				synchronized (lock_output) {
					if(Mainboard.lock.wait_for == null){		// check twice
						if(!output.isEmpty()){
							canExec = true;
							synchronized (exec_lock) {
								exec_lock.notify();
							}
						}
					}
				}
			}
		}
	}
	/*
	private void exec2() {
		if(Thread.holdsLock(lock_exec)){	// don't do it if other is doing
			return;
		}
		synchronized (lock_exec) {
	//		Initiator.logger.i("Queue.run.wait_for1", "");
			if( lock.wait_for_device ){
			//	endRun();
		//		Initiator.logger.i("Queue.run.wait_for1 stop", ""+ lock.wait_for_device);
				return;		// jestem w trakcie oczekiwania
			}else{
		//		Initiator.logger.w("Queue.run.wait_for1", "no blocked " + lock.wait_for_device);	
			}
	//		show("exec");
	//		boolean wasEmpty = output.isEmpty();
			boolean isNotEmpty;
			synchronized (lock_queue) {
				isNotEmpty = !output.isEmpty();
			}
		//	Initiator.logger.i("Queue.run.wait_for isNotEmpty", ""+isNotEmpty);
			while (isNotEmpty) {
				synchronized (lock) {
					if(lock.wait_for_device ){		// is blocked
			//			Initiator.logger.i("Queue.wait_for2", "" + lock.wait_for_device);
					//	endRun();
						return;
					}
				}
				AsyncMessage m = null;
		//		Initiator.logger.i("Queue.run.wait_for isNotEmpty8", ""+isNotEmpty);
				synchronized (lock_queue) {
					isNotEmpty = !output.isEmpty();
					if(isNotEmpty){
						m	= output.pop();
					}else{
			//			Initiator.logger.i("Queue.run.wait_for ", "jednak pusto");
						break;
					}
				}

		//		Initiator.logger.i("Queue.run.start",  m.toString() );
				Queue nextq = m.start( mb, this );
			//	moveToHistory( m );
				if( nextq != null ){
					this.addFirst(nextq);	// add on front
				}
				if(m.wait4Finish()){
					synchronized (lock) {
						lock.wait_for_device	= true;
					}
                	mb.waitFor( m );
            //    	endRun();
               // 	Initiator.logger.i("Queue.isBlocing true & return", m.toString() );
               // 	Initiator.logger.i("Queue.isBlocing", "" + lock.wait_for_device_id );
                	return;
                }else{
                //	Initiator.logger.i("Queue.no Blocing", m.toString() );
                //	Initiator.logger.i("Queue.no Blocing", "" + lock.wait_for_device_id );
                }
				synchronized (lock_queue) {
					isNotEmpty = !output.isEmpty();
				}
			}
		}
		//	if(output.isEmpty()){
		//		Initiator.logger.i("Queue.run", "empty");
		//	}
	}*/
	
    public void sendNow( String command ) {	 // send without waiting
	//	synchronized (lock) {
			mb.send(command + "\n");
	//	}
    }
	public void unlock() {
		if(isMainQueue){
			Mainboard.lock.unlock();
			exec();
		}
	//	Initiator.logger.i("Queue", "unlock()");
	}
	public void addWaitThread(final Object thread) {
	//	Initiator.logger.i("Queue", "add thread wait");
		int size = 0;
		synchronized (lock_output) {
			size = this.output.size();
		}

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
		synchronized(this.output){
			if( Mainboard.lock.wait_for !=null || size > 0 ){	// if not empty
				synchronized (lock_output) {
					output.add(msg);
					if(isMainQueue){
						history.push(msg);
					}
				}
	//			Initiator.logger.i("Queue.addWaitThread length", ""+output.size() );

				exec();
			//	Initiator.logger.i("Queue", "START wait");
				synchronized(thread){
					try {
						thread.wait();
					} catch (InterruptedException e) {
						Initiator.logger.appendError(e);
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
				this.unlockingcommand = "end";
			}
		};
		synchronized (lock_output) {
			output.add( m2 );
			if(isMainQueue){
				history.push(m2);
			}
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
		///		Initiator.logger.w("Queue.addWait.run", "time: " +time);
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
				}, time);// odczekaj tyle czasu, odblokuj kolejk� i jedz dalej
				return null;
			}
			@Override
			public boolean wait4Finish() {
				return true;
			}
		};
		synchronized (lock_output) {
			output.add( m2 );
			if(isMainQueue){
				history.push(m2);
			}
		}
		exec();
//		Initiator.logger.i("Queue.addWait length", ""+output.size() );
	}
	public boolean isBusy() {
		synchronized (lock_output) {
			if( Mainboard.lock.wait_for != null){
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
		synchronized (QueueLock.lock_wait_for) {
			if(isMainQueue){
				if( Mainboard.lock.wait_for!=null){
					String s = Mainboard.lock.wait_for.toString();
					res += "\tWaiting for:"+  s + "\n";				
				}
			}
		}
		synchronized (lock_output) {
			for (AsyncMessage msg : this.output){
				res += "\t" + msg.toString() + "\n";
			}
		}
		Initiator.logger.w("Queue.show", res);
	}

	public LimitedBuffer<AsyncMessage> getHistory(){
		return this.history;
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
}, time);// odczekaj tyle czasu, odblokuj kolejk� i jedz dalej
return null;

*/
