package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import com.eclipsesource.json.JsonObject;

@Entity
public class Log_drink extends Model<Log_drink> implements JsonSerializable{

	@PrimaryKey(autoIncrement=true)
	public int id;
	public int robot_id;
	public long datetime;
	public long time;
	public int id_drink;
	public int error_code;
	public String order_source;
	public int size;
	public int ingredients;
	public int size_ml;
	public int size_real_ml;
	public int temp_before;
	public int temp_after;

	@Override
	public JsonObject getJson() {
		JsonObject jsonObject = new JsonObject()
			.add( "id", this.id )
			.add( "datetime", this.datetime )
			.add( "time", this.time )
			.add( "size_ml", this.size_ml )
			.add( "size_ml", this.id_drink )
			.add( "time", this.time )
			.add( "temp_after", this.temp_after );
		return jsonObject;
	}
}
