package com.barobot.hardware;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.barobot.utils.Constant;
import android.content.Context;
import android.util.Xml;

public class DeviceSet {
	private static Map<String, LinkedList<Device>> featureIndex = new HashMap<String, LinkedList<Device>>();
	private static LinkedList<Device> fullSet = new LinkedList<Device>();
	private static List<Device> entries = new ArrayList<Device>();
    private static final String ns = null;	// We don't use namespaces

    private LinkedList<Device> set = new LinkedList<Device>();

	public DeviceSet(LinkedList<Device> linkedList){
		this.set = linkedList;
	}

	public static DeviceSet byFeature( String ff ){
		if(featureIndex.containsKey(ff)){
			return new DeviceSet(featureIndex.get(ff));
		}else{
			LinkedList<Device> list = new LinkedList<Device>();
			return new DeviceSet(list);
		}
	}
	public Device getItem( int location ) {
		return this.set.get(location);
	}
	public int getCount() {
		return this.set.size();
	}

	public DeviceSet add(String ff) {
		if(featureIndex.containsKey(ff)){
			
			
			
			
			this.set.addAll(featureIndex.get(ff));
		}
		return this;
	}
	
	public static void loadXML( Context ctx, int resId ){
	    InputStream inputStream			= ctx.getResources().openRawResource(resId);
	//    Constant.log("loadXML", "----------------------------" );
	    try {
			try {
				XmlPullParser parser = Xml.newPullParser();            
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(inputStream, null);
				parser.nextTag();
				readFeed(parser);
			} finally {
				inputStream.close();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    private static void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "devices");
        
  //      Constant.log("XML", "----------------------------" );
        
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagname = parser.getName();   // Starts by looking for the entry tag
            if (tagname.equals("device")) {
            	 String type = parser.getAttributeValue(null, "type");
            	 String name = parser.getAttributeValue(null, "name");
         //   	 Constant.log("readFeed","device: "+ type);
            	 Device d = readEntry(parser);
            	 d.setTypeName(type); 
            	 d.setTypeName(name);
            	 fullSet.add(d);
            } else {
                skip(parser);
            }
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private static Device readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "device");
        Device e =  new Device();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagname = parser.getName();
       //     Constant.log("readEntry", tagname);
            if (tagname.equals("feature")) {
				//feat_name = readTitle(parser);      		// dodaj do indexu
				parser.require(XmlPullParser.START_TAG, ns, "feature");
				String feat_name 	= parser.getAttributeValue(null, "name");
				String value		= parser.getAttributeValue(null, "value");
				parser.nextTag();
				parser.require(XmlPullParser.END_TAG, ns, "feature");
            	e.addFeature( feat_name, value );
         //   	Constant.log("readEntry","feature: " + feat_name);
            } else if (tagname.equals("config")) {     	// rozpocznij parsowanie
				parser.require(XmlPullParser.START_TAG, ns, "config");
				String feat_name 	= parser.getAttributeValue(null, "name");
				String size			= parser.getAttributeValue(null, "size");
				String persistent	= parser.getAttributeValue(null, "persistent");
				String value		= readText(parser);//parser.getAttributeValue(null, "value");
			//	parser.nextTag();
				parser.require(XmlPullParser.END_TAG, ns, "config");

            	e.addConfig( feat_name, value, size, persistent );
            } else if (tagname.equals("device")) {     	// rozpocznij parsowanie
                String type = parser.getAttributeValue(null, "type");
                String name = parser.getAttributeValue(null, "name");
            //	Constant.log("readEntry","device start: " + type);
                Device c = readEntry(parser);
                fullSet.add(c);
                c.setName(name);
                c.setTypeName(type);
            	e.addChild( c );
            //    Constant.log("readEntry","device end");
            } else {
                skip(parser);
            }
        }
        entries.add(e);
        return e;
    }
 
    public static void indexFeature(String feat_name, Device e) {
		if(featureIndex.containsKey(feat_name)){
			LinkedList<Device> list = featureIndex.get(feat_name);
			list.add(e);
		}else{
			LinkedList<Device> list = new LinkedList<Device>();
			featureIndex.put(feat_name, list);
			list.add(e);
    	}
	}

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
/*
    
    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link = "";
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");
        if (tag.equals("link")) {
            if (relType.equals("alternate")) {
                link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    // For the tags title and summary, extracts their text values.
   
*/
    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        Constant.log("SKIP", parser.toString());
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                    depth--;
                    break;
            case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
	public static DeviceSet getAll() {
		return new DeviceSet(DeviceSet.fullSet);
	}
}
