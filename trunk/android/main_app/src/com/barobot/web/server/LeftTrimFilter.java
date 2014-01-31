package com.barobot.web.server;

import com.x5.template.Chunk;
import com.x5.template.filters.BasicFilter;
import com.x5.template.filters.ChunkFilter;

public class LeftTrimFilter extends BasicFilter implements ChunkFilter {

	@Override
	public Object applyFilter(Chunk arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object applyFilter(Chunk arg0, Object arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getFilterAliases() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	* LeftTrimFilter is a simple, no-arg filter: {$tag|ltrim}.
	*
	* Visit the full documentation to learn how filter args get parsed
	* and passed into transformText(...)
	*
	*/
	public String transformText(Chunk chunk, String text, String[] args){
		if (text == null){ 
			return null;
		}
		int i=0;
		while (i < text.length() && Character.isWhitespace(text.charAt(i))) i++; 
		return (i == 0) ? text : text.substring(i);
	}

	public String getFilterName(){
		return "ltrim";
	}
}

