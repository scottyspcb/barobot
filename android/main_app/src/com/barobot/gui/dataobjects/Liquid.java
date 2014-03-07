/**
 * 
 */
package com.barobot.gui.dataobjects;

/**
 * @author Raven
 *
 */
public class Liquid {

	@Override
	public String toString() {
		return name + " (" + type + ")";
	}
	
	public Liquid (long id_, String type_, String name_, float voltage_)
	{
		id = id_;
		type = type_;
		name = name_;
		voltage = voltage_;
	}
	public Liquid (String type_, String name_, float voltage_)
	{
		this(0, type_, name_, voltage_);
	}
	
	public long id;
	public String type;
	public String name;
	public float voltage;
}
