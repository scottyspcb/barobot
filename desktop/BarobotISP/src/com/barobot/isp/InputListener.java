package com.barobot.isp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.barobot.isp.parser.SerialInputBuffer;

public class InputListener  extends Thread {
	Hardware owner;
	boolean running = true;
	private InputStreamReader theInput2;
	private InputStream theInput1;
	private BufferedReader theInput3;
	
	public InputListener(InputStream inputStream, Hardware hardware) {  
		this.theInput1	= inputStream;
		this.theInput2	= new InputStreamReader(this.theInput1);
		this.theInput3	= new BufferedReader(this.theInput2);
		this.owner		= hardware;
	}
	boolean fast = true;
	
	public void run() {
	    try {
	      while (running) {
	    	  if(fast){
	   // 		  while(!theInput.ready() && running){  
	   // 		  }
	    //		  System.out.println("read{");
	    		  char[] buffer = new char[128];
		  	      int bytesRead = theInput3.read(buffer, 0, 128);
		  	      if (bytesRead > 0){
		//  		      String in = new String( buffer, 0, bytesRead );
		//  		      System.out.println("}readcount:" + bytesRead + ": "+ in);
		  		      
		  		  //    this.owner.onInput(in);
		//  		    	SerialInputBuffer.readInput(buffer, 0, bytesRead);	
		  	      }else if (bytesRead == -1){
		  	      	System.out.println("input -1");
		  	       	break;
		  	      }
	    	  }else{
	    	//	  String ln = theInput.readLine();
	 //   		  System.out.println("theInput" + ln);
	    	//	  SerialInputBuffer.readInput(ln + "\n");
	    	  }
	      }
	    }
	    catch (IOException e) {
	    	e.printStackTrace();
	    	SerialInputBuffer.clear();
	    }
	  }
	public void close() {
		try {
			theInput3.close();
			theInput2.close();
			theInput1.close();
			System.out.println("theInput close");
		} catch (IOException e) {
			e.printStackTrace();
		}
		running =false;
	}
}
