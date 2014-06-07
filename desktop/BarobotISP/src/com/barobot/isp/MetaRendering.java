package com.barobot.isp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import com.barobot.common.Initiator;

public class MetaRendering {
	public void createContstans() {
		String source = "C:\\PROG\\arduino\\libraries\\barobot_common\\constants.h";
		String detsination = "C:\\workspace\\Barobot\\android\\com.barobot.common\\src\\com\\barobot\\common\\constant\\Methods.java";	

		BufferedReader br = null;
		String data = "";
		try {
			br = new BufferedReader(new FileReader(source));
	        StringBuilder sb = new StringBuilder();
	        String line = translateLine(br.readLine());
	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = translateLine(br.readLine());
	        }
	        data = sb.toString();
		} catch (FileNotFoundException e) {
			Initiator.logger.appendError(e);
	    } catch (IOException e) {
			Initiator.logger.appendError(e);
		} finally {
			if(br!=null){
		        try {
					br.close();
				} catch (IOException e) {
					Initiator.logger.appendError(e);
				}
			}
	    }
		FileOutputStream fout;		
		try{
		    fout = new FileOutputStream (detsination);
		    PrintStream writer = new PrintStream(fout);
		    
		    writer.print("package com.barobot.common.constant;\n\n" +
		    		"public class Methods {\n\n");

		 	writer.println (data);
		   	writer.println ("}");
		    fout.close();		
		}
		catch (IOException e){
			System.err.println ("Unable to write to file");
			System.exit(-1);
		}	
	}
	private String translateLine(String readLine) {
		if(readLine!=null){
			if(readLine.contains("'")){		// CHAR
				readLine = readLine.replaceAll("'", "\"");
			}
			if(readLine.contains("\"")){	// STRING
				readLine = readLine.replaceAll("#define\\s", "\tpublic static final String ");
				readLine = readLine.replaceAll("\\s+(\".*\")$", "\t= $1;");
			}else{
				readLine = readLine.replaceAll("#define\\s", "\tpublic static final int ");
				readLine = readLine.replaceAll("\\s((0x)?\\d+)\\s*$", "\t= $1;");
			}
		//	System.out.println("onInput: " + readLine);
		}
		return readLine;
	}
}
