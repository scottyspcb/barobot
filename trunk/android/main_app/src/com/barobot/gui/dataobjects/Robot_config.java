package com.barobot.gui.dataobjects;


import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.PrimaryKey;

import com.eclipsesource.json.JsonObject;

@Entity
public class Robot_config extends Model<Robot_config> implements JsonSerializable{
	
	@PrimaryKey(autoIncrement=true)
	public int id;
	public String config_name;
	public String value;

	@ManyToOne
	public Robot robot;

	@Override
	public JsonObject getJson() {
		JsonObject jsonObject = new JsonObject()
			.add( "id", this.id )
			.add( "config_name", this.config_name )
			.add( "value", this.value )
			.add( "robot", this.robot.serial );
		return jsonObject;
	}	

	@Override
	public String toString() {
		return this.config_name+""+this.value;
	}
}
