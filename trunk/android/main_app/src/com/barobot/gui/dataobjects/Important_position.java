package com.barobot.gui.dataobjects;

import org.orman.mapper.Model;
import org.orman.mapper.annotation.Entity;
import org.orman.mapper.annotation.ManyToOne;
import org.orman.mapper.annotation.PrimaryKey;

@Entity
public class Important_position extends Model<Important_position>{
	@PrimaryKey(autoIncrement=true)
	public long id;
	public int position;
	public int staxisatus;
	public int hall_value;
	public int margin;
	public boolean is_end_min;
	public boolean is_end_max;
	public boolean is_start;
	public boolean is_end;
	public int robot_id;
}
