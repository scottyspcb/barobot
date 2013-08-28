package com.barobot.webview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import android.content.Context;
import android.webkit.WebViewClient;

public class htmlBrowser {
	private MainActivity m	=null;

	public htmlBrowser(MainActivity mainActivity) {
		// TODO Auto-generated constructor stub
		this.m=mainActivity;
	}

	private class MyWebViewClient extends WebViewClient {
	}

	//console.log("zmienionych elementow przez ("+ selector +"): " + affected.length);
	public void startPage() {
	//	String aa = htmlBrowser.readRawTextFile(this.m, R.raw.main_page);
		//String tplcc = this.fetchTpl();
		//String tplcc = this.fetchTpl5();
		
		VelocityContext context = new VelocityContext();
		/*
        ArrayList<String> list = new ArrayList<String>();
        list.add("ArrayList element 1");
        list.add("ArrayList element 2");
        list.add("ArrayList element 3");
        list.add("ArrayList element 4");
  //      context.put("elements", list);
   

		Vector<String> v = new Vector<String>();
		v.addElement("Hello");
		v.addElement("There");
		v.addElement("fkfkty");
	//	context.put("elements2", v.iterator() );

		Vector<Element> v2 = new Vector<Element>();
		v2.addElement( new Element(1,"exhibit","50/26755023_m.jpg") );
		v2.addElement( new Element(2,"exhibit","84/37284_m.jpg") );
		v2.addElement( new Element(3,"exhibit","62/49276223_m.jpg") );
		v2.addElement( new Element(4,"exhibit","44/50554423_m.jpg") );
		v2.addElement( new Element(5,"exhibit","53/51745323_m.jpg") );
		v2.addElement( new Element(6,"exhibit","18/54831823_m.jpg") );
		v2.addElement( new Element(7,"exhibit","12/55171223_m.jpg") );
		v2.addElement( new Element(8,"exhibit","17/54831723_m.jpg") );
		v2.addElement( new Element(9,"exhibit","50/26755023_m.jpg") );
		v2.addElement( new Element(10,"exhibit","84/37284_m.jpg") );
		v2.addElement( new Element(11,"exhibit","17/54831723_m.jpg") );
		context.put("elements", v2.iterator() );
*/
		
		String tplcc = this.fetchTplVel("scroll_view", context );
	//	String tplcc = this.fetchTplVel("main_page_test", context );
	//	Log.d("+HTML",tplcc);
		this.m.webview.loadDataWithBaseURL("file:///android_asset/", tplcc, "text/html", "UTF-8", null );
	}

	private void VelocityInit(){
		Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, "com.collector.main.VelocityLogger");
		Velocity.setProperty("resource.loader", "android");
		Velocity.setProperty("android.resource.loader.class", "com.collector.main.AndroidResourceLoader");
		Velocity.setProperty("android.content.res.Resources",this.m.getResources());
		Velocity.setProperty("packageName", "com.collector.main");
		Velocity.init();
	}
	
	private String fetchTplVel( String resource, VelocityContext context ){
		VelocityInit();
		try{
			Template template 		= Velocity.getTemplate(resource);
			StringWriter sw			= new StringWriter();
			//	context.put( "name", new String("Velocity") );
			template.merge(context, sw);
			return sw.toString();

		}catch( ResourceNotFoundException e ){
			e.printStackTrace();
		}catch( ParseErrorException e ){
			e.printStackTrace();
		}catch( MethodInvocationException e ){
			e.printStackTrace();
		}catch( Exception e ){
			e.printStackTrace();
		}
	    return "";
	}
	/*
	private static Engine smartyEngine = new Engine();
	private String fetchTpl(){
		org.lilystudio.smarty4j.Template template;
	//	String templateContent = "<hr>[{$number8}/{$number9}/{$number8}]";
		String templateContent = "<hr>[]<hr>[]<hr><hr>";
		try {
			template = new org.lilystudio.smarty4j.Template(smartyEngine, templateContent);
			//template = smartyEngine.getTemplate("/src/test/resources/demo.tpl");
			org.lilystudio.smarty4j.Context context = new org.lilystudio.smarty4j.Context();
		//	context.set("number8", "123");
		//	context.set("number9", "123");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			template.merge(context, out);
			return out.toString("utf-8");	
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	} 

	private String fetchTpl5(){
		Theme theme = new Theme("examples");
		TemplateLoader pp = new TemplateLoader();
		theme.addProtocol( pp );

	    // Fetch template from this file: themes/examples/hello.chtml
	    // Inside that file there is a template "snippet" named #example_1
	    Chunk html = theme.makeChunk("res#example_1");

	  //  html.set("name", "Bob");

	    String output = html.toString();
	    return output;
	}
	*/
	public static String readRawTextFile(Context ctx, int resId){
	    InputStream inputStream			= ctx.getResources().openRawResource(resId);
	    InputStreamReader inputreader	= new InputStreamReader(inputStream);
	    BufferedReader buffreader		= new BufferedReader(inputreader);
	    String line;
	    StringBuilder text				= new StringBuilder();
	    try {
	        while (( line = buffreader.readLine()) != null) {
	            text.append(line);
	            text.append('\n');
	        }
	    } catch (IOException e) {
	        return null;
	    }
	    return text.toString();
	}
}
