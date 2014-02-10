package com.barobot.isp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.barobot.isp.parser.SerialInputBuffer;

public class InputListener  extends Thread {
	BufferedReader theInput;
	Hardware owner;
	boolean running = true;

	public InputListener(InputStream inputStream, Hardware hardware) {  
//		this.theInput = inputStream;
//		this.theInput = new InputStreamReader(inputStream);
		this.theInput = new BufferedReader(new InputStreamReader(inputStream));
		this.owner = hardware;
	}

	public void run() {
	    try {
	      while (running) {   
	    	while(!theInput.ready() && running){;
	    	}
//	    	 String ln = theInput.readLine();
	    	 //this.owner.onInput(ln);
	 //   	System.out.println("read[");
	    	char[] buffer = new char[256];
	        int bytesRead = theInput.read(buffer, 0, 256);

	 //       System.out.println("read]" + bytesRead);
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
	    	SerialInputBuffer.clear();
	    }
	  }
	public void close() {
		running =false;
	}
}
