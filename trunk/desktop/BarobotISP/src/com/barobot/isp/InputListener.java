package com.barobot.isp;

import java.io.BufferedReader;
import java.io.IOException;

import com.barobot.isp.parser.SerialInputBuffer;

public class InputListener  extends Thread {
	BufferedReader theInput;
	Hardware owner;
	  boolean running = true;
	  InputListener(BufferedReader is, Hardware hardware ) {
		  this.theInput = is;
		  this.owner = hardware;
	  }
	  public void run() {
	    try {	
	      char[] buffer = new char[256];
	      while (running) {	    	  
	    	while(!theInput.ready() && running){;
	    	}
//	    	 String ln = theInput.readLine();
	    	 //this.owner.onInput(ln);
	 //   	System.out.println("read[");
	        int bytesRead = theInput.read(buffer);
	  //      System.out.println("read]" + bytesRead);
	        if (bytesRead > 0){
		        String in = new String( buffer, 0, bytesRead );
		  //    this.owner.onInput(in);
		    	SerialInputBuffer.readInput(in);	
	        }else if (bytesRead == -1){
	        	System.out.println("input -1");
	        	break;
	        }
	      }
	    }
	    catch (IOException e) {
	    //  e.printStackTrace();
	    }
	  }
	public void close() {
		running =false;
	}
}
