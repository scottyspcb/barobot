package com.barobot.sofa.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;

import android.util.Log;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.dataobjects.Language;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Translated_name;
import com.barobot.gui.utils.LangTool;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class MainPage extends EmptyRoute{
	public MainPage() {
		use_raw_output = false;	
		this.regex = "^\\/$";
	}

	public String run(String url, SofaServer sofaServer, Theme theme, IHTTPSession session){
		if(theme == null){
			return null;
		}
		Chunk action_chunk			= theme.makeChunk("main#body");
		Map<String, List<String>> decodedQueryParameters =sofaServer.decodeParameters(session.getQueryParameterString());

    	  StringBuilder sb = new StringBuilder(); 
   //       sb.append("<p><blockquote><b>URI</b> = ").append(
    //          String.valueOf(session.getUri())).append("<br />");

     //     sb.append("<b>Method</b> = ").append(
      //        String.valueOf(session.getMethod())).append("</blockquote></p>");

   //       sb.append("<h3>Headers</h3><p><blockquote>").
  //            append(toString(session.getHeaders())).append("</blockquote></p>");

          sb.append("<h3>Parms</h3><p><blockquote>").
              append(toString(session.getParms())).append("</blockquote></p>");

    //      sb.append("<h3>Parms (multi values?)</h3><p><blockquote>").
   //           append(toString(decodedQueryParameters)).append("</blockquote></p>");
/*
          try {
              Map<String, String> files = new HashMap<String, String>();
              session.parseBody(files);
              sb.append("<h3>Files</h3><p><blockquote>").
                  append(toString(files)).append("</blockquote></p>");
          } catch (Exception e) {
              Initiator.logger.appendError(e);
          }*/
  
          Log.e("Translated_name size", "tutaj");
          
	//	List<Language> ls = Model.fetchQuery(ModelQuery.select().from(Language.class).getQuery(),Language.class);
		List<Translated_name> tn = Model.fetchQuery(ModelQuery.select().from(Translated_name.class).orderBy("Translated_name.table_name","Translated_name.element_id").getQuery(),Translated_name.class);
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();

		for (Translated_name item : tn) {
			data.add( item.toHashMap());
		}

	//	Log.e("Translated_name size", ""+data.size());
	//	Log.e("Translated_name size2",  ""+data.get(0));
	//	Log.e("Translated_name", tn.get(0).getJson().toString());

	//	action_chunk.set("languages",ls);
		action_chunk.set("names",data);
		

		/*

		for (Language lang : ls) {
			lang.name
			lang.id;
		}*/ 
          
 
          action_chunk.set("nums", new String[]{
          		"0","1","2","3","4","5","6","7","8","9",
          		"10","11","12","13","14","15","16","17","18","19",
          		"20","21","22","23","24","25","26","27","28","29",
          		"30","31","32","33","34","35","36","37","38","39",
          		"40","41","42","43","44","45","46","47","48","49",
          		"50","51","52","53","54","55","56","57","58","59",
          		"60","61","62","63","64","65","66","67","68","69",
          		"70","71","72","73","74","75","76","77","78","79",
          		"80","81","82","83","84","85","86","87","88","89",
          } );
          action_chunk.set("types", new String[]{
          		"0","1","2","3","4","5","6","7","8","9",
          		"10","11","12","13","14","15","16","17","18","19",
          		"20","21","22","23","24","25","26","27","28","29",

          } );
        action_chunk.set("devices", new String[]{
        		"Carret",
        		"Mainboard",
        		"Upanel",
        		"Load Cell",
        		"Stepper X",
        		"Servo Y",
        		"Servo Z",
        		"Hall X",
        		"Hall Y",
        		"Main Temp sensor",
        		"Carret Temp sensor",
        		"Temp sensor",
        		"Current Sensor X",
        		"Current Sensor Y",
        		"Current Sensor Z",	
        });
        action_chunk.set("lucky_item", new String[]{
        		"Gimme anything",
        		"Classical",
        		"Popular",
        		"By main ingredient",
        		"By sweetness",
        		"By color",
        		"By strength",
        		"Non-alcoholic"
        } ); 
        action_chunk.set("body2", sb.toString() );
    	return action_chunk.toString();
	}

    private String toString(Map<String, ? extends Object> map) {
        if (map.size() == 0) {
            return "";
        }
        return unsortedList(map);
    }

    private String unsortedList(Map<String, ? extends Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (Map.Entry entry : map.entrySet()) {
            sb.append("<li><code><b>").append(entry.getKey()).append("</b> = ").append(entry.getValue()).append("</code></li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }
}
