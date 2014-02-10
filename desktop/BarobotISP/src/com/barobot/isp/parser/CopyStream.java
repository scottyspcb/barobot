package com.barobot.isp.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyStream extends Thread {
	  InputStream theInput;
	  OutputStream theOutput;

	  CopyStream(InputStream in) {
	    this(in, System.out);
	  }

	  CopyStream(OutputStream out) {
	    this(System.in, out);
	  }

	  public CopyStream(InputStream in, OutputStream out) {
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
