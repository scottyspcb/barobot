package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import com.eclipsesource.json.JsonObject;

@Entity
public class Log_start extends Model<Log_start> implements JsonSerializable{

	@PrimaryKey(autoIncrement=true)
	public int id;
	public long datetime;
	public int app_starts;
	public String start_type;
	public int arduino_starts;
	public int app_version;
	public int arduino_version;
	public int database_version;
	public int serial_starts;
	public int temp_start;
	public String language;
	public int robot_id;

	@Override
	public JsonObject getJson() {
		JsonObject jsonObject = new JsonObject()
			.add( "id", this.id )
			.add( "datetime", this.datetime )
			.add( "app_starts", this.app_starts )
			.add( "arduino_starts", this.arduino_starts )
			.add( "app_version", this.app_version )
			.add( "arduino_version", this.arduino_version )
			.add( "serial_starts", this.serial_starts );
		return jsonObject;
	}
}
