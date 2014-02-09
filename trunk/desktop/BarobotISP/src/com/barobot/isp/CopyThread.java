package com.barobot.isp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyThread extends Thread {
	  InputStream theInput;
	  OutputStream theOutput;

	  CopyThread(InputStream in) {
	    this(in, System.out);
	  }

	  CopyThread(OutputStream out) {
	    this(System.in, out);
	  }

	  CopyThread(InputStream in, OutputStream out) {
	    theInput = in;
	    theOutput = out;
	  }

	  public void run() {
	    try {
	      byte[] buffer = new byte[256];
	      while (true) {
	        int bytesRead = theInput.read(buffer);
	        if (bytesRead == -1) break;
	        theOutput.write(buffer, 0, bytesRead);
	      }
	    }
	    catch (IOException e) {
	  //    e.printStackTrace();
	    }

	  }
}
