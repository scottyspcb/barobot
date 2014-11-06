package com.barobot.parser.message;

import java.util.LinkedList;

public class LimitedBuffer<T> {
	LinkedList<T> stack;
    private int size;

    public LimitedBuffer( int limit ) {
          this.stack	= new LinkedList<T>();
          this.size		= limit;
    }
    public void push(T obj) {
    	return;
    	/*
    	
		Initiator.logger.w("LimitedBuffer.push", obj.toString());	
    	this.stack.add(obj);
    	if(this.stack.size() > this.size ){
    		
    	}
    	//	this.stack.removeFirst();
    		
    		0
    		50
    		200
    		0...(200-50)
*/
    	//	this.stack.subList(0, (this.size() - this.size)).clear();
    	//}
    }

    public T pop() {
    	return this.stack.pop();
    }

    public void clear() {
    	this.stack.clear();
    }

    public int size() {
         return size;
    }
 
    public LinkedList<T> getAll() {
    	return this.stack;
    }

	public void addBefore(T before, LinkedList<T> addList) {
		int pos = this.stack.indexOf(before);
		if(pos==-1){
	//		Initiator.logger.w("LimitedBuffer.addBefore", " -1 before "+ before.toString());	
		}else{
			this.stack.addAll(pos, addList);
	//		Initiator.logger.w("LimitedBuffer.addBefore OK", "before "+ this.stack.size());	
		}
	}
	public void addAll(LinkedList<T> output) {
		this.stack.addAll(output);
	}
}
