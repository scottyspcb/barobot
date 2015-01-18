package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.other.JsonSerializable;
import com.eclipsesource.json.JsonObject;

@Entity
public class Log extends Model<Log> implements JsonSerializable{
	@PrimaryKey(autoIncrement=true)
	public long id;
	public int level;
	public String tag;
	public String content;
	public int time;
	public int send_time;
	public int robot_id;

	@Override
	public JsonObject getJson() {
		JsonObject jsonObject = new JsonObject()
			.add( "id", this.id )
			.add( "level", this.level )
			.add( "tag", this.tag )
			.add( "content", this.content )
			.add( "time", this.time )
			.add( "send_time", this.send_time );
		return jsonObject;
	}
}
