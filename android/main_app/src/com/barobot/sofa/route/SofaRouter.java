package com.barobot.sofa.route;

import com.barobot.web.route.EmptyRoute;
import com.barobot.web.route.MainPage;
import com.barobot.web.route.RPCPage;
import com.barobot.web.route.TemplateRendering;
import com.barobot.web.server.SimpleRouter;
import com.barobot.web.server.SofaServer;
import com.x5.template.Theme;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class SofaRouter implements SimpleRouter{

	@Override
	public void init(SofaServer ss) {
		ss.addRoute( new EmptyRoute() {
			@Override
			public String run(String url, SofaServer sofaServer,
					Theme theme, IHTTPSession session) {
				return null;
			}
			@Override
			public boolean match(String uri) {
				// TODO Auto-generated method stub
				return false;
			}
		} );
		ss.addRoute( new RPCPage() );
		ss.addRoute( new TemplateRendering() );
		ss.addRoute( new MainPage() );
		ss.addRoute( new SettingsPage() );
		ss.addRoute( new OrderPage() );
		ss.addRoute( new SynchroPage() );
		ss.addRoute( new InfoPage() );
		ss.addRoute( new DrinkCreatorPage() );
		ss.addRoute( new CommandRoute() );
		ss.addRoute( new CommandsPageRoute() );	
	}
}
