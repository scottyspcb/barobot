package com.barobot.parser;

import com.barobot.common.Initiator;
import com.barobot.parser.message.AsyncMessage;

public class QueueLock{

	//public boolean wait_for_device = false;
	public AsyncMessage wait_for = null;

//	protected static final Object lock_wait = new Object();		// protect lock.wait_for_device var
	public static final Object lock_wait_for = new Object();	// protect wait_for var
	public static final Object lock_exec = new Object();		// protect exec method

	public void unlock(String withCommand, Queue mainQueue ) {
		boolean allowUnlock = false;
		synchronized (QueueLock.lock_wait_for) {
			if(wait_for!=null){
				Initiator.logger.i(">>>QueueLock.unlockRet", "["+wait_for.toString() +"] with: ["+ withCommand.trim()+"]");
				wait_for.unlockWith(withCommand);
				wait_for.setWaiting( false );
				wait_for = null;
				allowUnlock = true;
			}
		}
		if(allowUnlock){
			mainQueue.unlock();
		}
	}

	public void unlock(AsyncMessage asyncMessage, String withCommand,Queue mainQueue) {
		boolean allowUnlock = false;
		synchronized (QueueLock.lock_wait_for) {
			if(wait_for == asyncMessage ){
				Initiator.logger.i(">>>QueueLock.unlockRet", "["+wait_for.toString() +"] with: ["+ withCommand.trim()+"]");
				wait_for.unlockWith(withCommand);
				wait_for.setWaiting( false );
				wait_for = null;
				allowUnlock = true;
			}
		}
		if(allowUnlock){
			mainQueue.unlock();
		}
	}

	public void unlock() {
		synchronized (lock_wait_for) {
			if( wait_for!=null ){
				Initiator.logger.i("QueueLock.unlock", "unlock id:" + wait_for.toString() );
				wait_for.unlockWith("force unlock");
				wait_for.setWaiting( false );
				wait_for = null;
			}
		}
	}
}
