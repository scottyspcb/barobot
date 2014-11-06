package com.barobot.sofa.api;

import com.barobot.common.Initiator;
import com.barobot.web.route.EmptyRoute;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public abstract class Page extends EmptyRoute {

	public Page()
	{
		use_raw_output=true;
	}

	@Override
	public String run(String url, SofaServer sofaServer, Theme theme,
			IHTTPSession session) {
		try {
			String res = runInternal(url, sofaServer, theme, session).getJSON();
			if ( session.getParms().containsKey("cb")) {				// callback i.e ?cb=myHandler => myHandler({a:"b"});
				boolean asString = false;
				if ( session.getParms().containsKey("as_string")) {		// callback i.e ?cb=myHandler&as_string=true => myHandler("{a:\"b\"}");
					String as = session.getParms().get("as_string");
					Initiator.logger.i("as : ", as );
					if(as!=null && as.equals("true")){
						asString = true;
					}
				}
				String cb =  session.getParms().get("cb");
				Initiator.logger.i("cb : ", cb );
				if( cb!=null && cb.matches("^[a-zA-Z_][a-zA-Z_0-9]*$")){		// A-Z a-Z 0-9 _ allowed
					if(asString){
						res = cb + "(\"" + addSlashes(res) +"\");";
					}else{
					//	Initiator.logger.i("res1 : ", res );
						res = cb + "(" + res +");";
					//	Initiator.logger.i("res2 : ", res );
					}
				}
			}
			//Initiator.logger.i("res4 : ", res );
			return res;
		} catch (Exception e){
			return "{ \"status\" : \"FATAL_ERROR\", \"message\": \""+e.getMessage()+"\"}";
		}
	}

    public static String addSlashes(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\");
        s = s.replaceAll("\\n", "\\\\n");
        s = s.replaceAll("\\r", "\\\\r");
        s = s.replaceAll("\\00", "\\\\0");
        s = s.replaceAll("'", "\\\\'");
        return s;
    }

	abstract protected JsonResponse runInternal(String Url, SofaServer sofaServer, Theme theme,
			IHTTPSession session);


}
