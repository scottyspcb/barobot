package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.other.JsonSerializable;
import com.barobot.other.LangTool;
import com.eclipsesource.json.JsonObject;

@Entity
public class Language extends Model<Language> implements JsonSerializable{
	@PrimaryKey(autoIncrement=true)

	public int id;
	public String name;
	public String lang_code;
	public String flag_code;

	public String getName() {
		return LangTool.translateName(id, "language", name );
	}
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public JsonObject getJson() {
		JsonObject jsonObject = new JsonObject()
			.add( "id", this.id )
			.add( "level", this.name )
			.add( "tag", this.lang_code )
			.add( "content", this.flag_code );
		return jsonObject;
	}
	
}
