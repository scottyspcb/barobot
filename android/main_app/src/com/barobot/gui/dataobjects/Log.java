package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Log extends Model<Log>{
	public static String STATUS_EMPTY = "Empty"; 

	@PrimaryKey(autoIncrement=true)
	public long id;
	public int level;
	public String tag;
	public String content;
	public int time;
}
