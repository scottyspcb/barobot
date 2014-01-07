package com.barobot.web.route;

import java.util.List;
import java.util.Map;

import com.barobot.web.server.SofaServer;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class MainPage extends EmptyRoute{
	public MainPage(String uri) {
		super(uri);
		use_raw_output = false;	
	}
	public final static String regex = "^\\/$";

	public String run(SofaServer sofaServer, Theme theme, IHTTPSession session){
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
              e.printStackTrace();
          }*/
          
        action_chunk.set("nums", new String[]{
        		"0","1","2","3","4","5","6","7","8","9",
        		"10","11","12","13","14","15","16","17","18","19",
        		"20","21","22","23","24","25","26","27","28","29",
        		"30","31","32","33","34","35","36","37","38","39",
        		"40","41","42","43","44","45","46","47","48","49",        		
        } );
        action_chunk.set("types", new String[]{
        		"1","2","3","4","5","6","7","8","9","10",      		
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
