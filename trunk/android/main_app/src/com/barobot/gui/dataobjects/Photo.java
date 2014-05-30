package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Photo extends Model<Photo>{
	public static String STATUS_EMPTY = "Empty"; 

	@PrimaryKey(autoIncrement=true)
	public long id;
	public int width;
	public int height;
	public String mime_type;
	public int size;
	public String content;

}
