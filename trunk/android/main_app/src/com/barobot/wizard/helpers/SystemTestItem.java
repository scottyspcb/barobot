package com.barobot.wizard.helpers;

import android.content.Context;

public abstract class SystemTestItem<T>{
	protected T value		= null;
	public String title		= "1";
	boolean result			= false;
	private int solutionId	= 0;

	public SystemTestItem(String msg) {
		title = msg;
	}

	public abstract T read();

	public boolean check() {
		if(value == null){
			return false;
		}
		String val2	= ("" + value);
		if(value.getClass().getName().equals("Boolean")){
			return ((Boolean) value).booleanValue();
		}else{
		}
		if( value == null ){
			return false;
		}else if( val2.equals("") ||  val2.equals("0")){
			return false;
		}
		return true;
	}

	public String getTitle() {
		return this.title;
	}
	public String getValue() {
		return ("" + value);
	}

	public final boolean getResult() {
		value	= this.read();
		result	= check();
		return result;
	}

	public CharSequence getSolution( Context ctx ) {
		solutionId = getSolutionId();
		if( solutionId > 0 ){
			return ctx.getResources().getText( solutionId );
		}
		return "";
	}

	public int getSolutionId() {
		return 0;
	}
}
