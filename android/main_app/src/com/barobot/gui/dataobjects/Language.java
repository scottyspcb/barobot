package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

import com.barobot.gui.utils.LangTool;

@Entity
public class Language extends Model<Language>{
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
}
