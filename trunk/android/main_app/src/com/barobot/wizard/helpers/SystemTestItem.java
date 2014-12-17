package com.barobot.wizard.helpers;

import android.content.Context;

public abstract class SystemTestItem<T>{
	protected T value		= null;
	public String title		= "";
	private int solutionId	= 0;
	private int title_id	= 0;

	public SystemTestItem(String msg) {
		title		= msg;
	}
	public SystemTestItem(String msg, int wizardSolution) {
		title		= msg;
		solutionId	= wizardSolution;
	}
	public SystemTestItem(int title, int wizardSolution) {
		title_id	= title;
		solutionId	= wizardSolution;
	}

	public SystemTestItem(int title) {
		title_id	= title;
	}
	public abstract T read();

	public boolean check() {
		if(value == null){
			return false;
		}
		if(value.getClass().getCanonicalName().equals("java.lang.Boolean")){
			return ((Boolean) value).booleanValue();
		}else{
		}
		String val2	= ("" + value);
		if( value == null ){
			return false;
		}else if( val2.equals("") ||  val2.equals("0")){
			return false;
		}
		return true;
	}
	public String getTitle( Context ctx ) {
		if(title_id > 0){
			return ctx.getResources().getText( title_id ).toString();
		}
		return this.title;
	}
	public String getValue() {
		return ("" + value);
	}
	public final boolean getResult() {
		value	= this.read();
		boolean b =  check();
	//	Initiator.logger.i( "SystemTestItem", this.title + "/" + value.getClass().getSimpleName() +"/" +value +"/"+ b);
		return b;
	}
	public CharSequence getSolution( Context ctx ) {
		if( solutionId > 0 ){
			return ctx.getResources().getText( solutionId );
		}
		return "";
	}
	public int getSolutionId() {
		return solutionId;
	}
}
