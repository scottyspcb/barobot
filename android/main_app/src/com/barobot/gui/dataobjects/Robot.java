package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import com.eclipsesource.json.JsonObject;

@Entity
public class Robot extends Model<Robot> implements JsonSerializable{

	@PrimaryKey(autoIncrement=true)
	public int id;
	public int serial;
	public int sversion;
	public boolean is_current;

	@Override
	public JsonObject getJson() {
		JsonObject jsonObject = new JsonObject()
			.add( "id", this.id )
			.add( "serial", this.serial )
			.add( "sversion", this.sversion )
			.add( "is_current", this.is_current );
		return jsonObject;
	}	
}
