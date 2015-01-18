package com.barobot.gui.dataobjects;

import java.util.HashMap;
import java.util.Map;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.other.JsonSerializable;
import com.barobot.other.LangTool;
import com.eclipsesource.json.JsonObject;

@Entity
public class Translated_name extends Model<Translated_name> implements JsonSerializable{
	@PrimaryKey(autoIncrement=true)
	public int id;
	public int element_id;
	public String table_name;
	public int language_id;
	public String translated;

	public String getName() {
		return translated;
	}

	@Override
	public String toString() {
		return getName();
	}

	public Map<String, String> toHashMap() {
		String langCode = LangTool.getlangCode(this.language_id);
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", ""+this.id);
		map.put("element_id", ""+this.element_id);
		map.put("table_name", this.table_name);
		map.put("language_id", ""+this.language_id);
		map.put("language_code", ""+langCode);
		map.put("translated", this.translated);
		return map;
	}

	@Override
	public JsonObject getJson() {
		JsonObject jsonObject = new JsonObject()
			.add( "id", ""+this.id )
			.add( "element_id", this.element_id )
			.add( "table_name", this.table_name )
			.add( "language_id", this.language_id )
			.add( "translated", this.translated );
		
		return jsonObject;
	}
}
