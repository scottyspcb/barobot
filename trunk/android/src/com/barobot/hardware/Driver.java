package com.barobot.hardware;

import android.util.Log;

public class Driver {
	long hardware_pos = 0;
	long software_pos = 0;

	long m1 = 0;
	long m2 = 0;
	//	todo s = (( (h * p1 + m1) / d ) + m2) * p2 

	public void setM( long margin1 ){
		m1 = margin1;
		Log.w("set MARGIN X", "" + m1);
	}
	public long getSPos(){
		return software_pos;
	}
	public long getHPos(){
		return hardware_pos;
	}
	void setSPos( long pos2 ){
		software_pos = pos2;
		hardware_pos = software_pos + m1;	
	}
	void setHPos( long pos2 ){
		hardware_pos = pos2;
		software_pos = hardware_pos - m1;
	}
	/*
	h		s	m
	-4000 = 0	-4000
	*/
	public long hard2soft( long pos2 ){
		Log.w("MARGIN X1", "Margin: " + m1 + "  hard: " + pos2 + "=> soft " + (pos2 -m1) );
		return pos2 - m1;
	}
	public long soft2hard( long pos3 ){
		Log.w("MARGIN X2", "Margin: " + m1 + "  soft: " + pos3 + " => hard " + (pos3 -( -m1)));
		return pos3 - (- m1);
	}
}
