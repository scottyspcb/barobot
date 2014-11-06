package com.barobot.sofa.route;

import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class EditDbRoute extends EmptyRoute {
	EditDbRoute(){
		this.regex = "^/setup/edit_db$";
	}
	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,
			IHTTPSession session) {

		if(session.getParms().containsKey("show")){
			Chunk action_chunk			= theme.makeChunk("setup#database_show");
	    	
			
			
			
	    	return action_chunk.toString();
		}else if(session.getParms().containsKey("edit")){
			Chunk action_chunk			= theme.makeChunk("setup#database_edit");
	    	
		//	SQLiteDatabase db = new SQLiteDatabase();
			
			
			/*
			
			ArrayList<String> arrTblNames = new ArrayList<String>();
			Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

			    if (c.moveToFirst()) {
			        while ( !c.isAfterLast() ) {
			            arrTblNames.add( c.getString( c.getColumnIndex("name")) );
			            c.moveToNext();
			        }
			    }
			
			
			
			Query query3 = new Query("SELECT `name` FROM `sqlite_master` WHERE type='table';");
			
			List<Object> a = Model.f.fetchQuery(query3, arg1);
*/
			
			
	    	return action_chunk.toString();
		}else{
			Chunk action_chunk			= theme.makeChunk("setup#database_start");
			
			
			

	    	action_chunk.set("tables", CommandRoute.geCommands() );
	    	return action_chunk.toString();
		}
	}
}
