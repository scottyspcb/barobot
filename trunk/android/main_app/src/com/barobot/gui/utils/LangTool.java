package com.barobot.gui.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.orman.mapper.Model;
import org.orman.mapper.ModelQuery;
import org.orman.sql.C;
import org.orman.sql.Query;

import android.util.Log;

import com.barobot.common.Initiator;
import com.barobot.common.constant.Constant;
import com.barobot.gui.dataobjects.Language;
import com.barobot.gui.dataobjects.Translated_name;

public class LangTool {
	private static String order =  " ";
	private static int langId = 456;	// pl
	private static Map<String, String> translation_cache = new HashMap<String, String>();

	public static void setLanguage(String readLangCode) {
		ModelQuery query = ModelQuery.select().from(Language.class).where(C.eq("lang_code", readLangCode));
		Language l = Model.fetchSingle(query.getQuery(), Language.class);
		if( l == null ){
			Log.e("setLanguage", "no lang" + readLangCode);
			return;
		}
		setLanguage(l.id);
	}

	public static void setLanguage(int newLangId) {
		langId =newLangId;
		int orderIds[] = {0,1,2,3};
		if(langId == 456){ 			// pl, en, ru, de
			int order3[] = { 456,457, 459, 458};
			orderIds = order3;
		}else if(langId == 457){ 	// en, pl, de, ru
			int order3[] = { 457,456, 458, 459};
			orderIds = order3;
		}else if(langId == 458){ 	// de, en, pl, ru
			int order3[] = { 458,457, 456, 459};
			orderIds = order3;
		}else if(langId == 459){	// ru, en, pl, de
			int order3[] = { 459,457, 456, 458};
			orderIds = order3;
		}
		String o = "ORDER BY CASE `language_id`";
		for(int i=0;i<orderIds.length;i++){
			o+= " WHEN "+orderIds[i]+" THEN "+i;
		}
		o += " END ";
		order = o;

		Initiator.logger.i(Constant.TAG,"setLanguage order" + order );
		
		/*
		ORDER BYCASE ID
		    WHEN 4 THEN 0
		    WHEN 3 THEN 1
		    WHEN 1 THEN 2
		    WHEN 5 THEN 3
		    WHEN 6 THEN 4
		  END
		*/	
	}

	public static String translateName( int id, String table_name ){
		return translateName( id, table_name, null );
	}

	public static String translateName( int id, String table_name, String defaultVal ){
		String key = id+"."+table_name+"."+langId;
		if(translation_cache.containsKey(key)){
			return translation_cache.get(key);
		}
		Query query3 = new Query("SELECT `translated` FROM Translated_name WHERE `element_id` ='"+id+"' and `table_name`='"+table_name+"'" + order + "LIMIT 1;");
		String translated = (String) Model.fetchSingleValue(query3);
		if(translated == null){
			LangTool.InsertTranslation( id, table_name, defaultVal );
			return defaultVal;
		}
		if( !translated.equals("")){		// remember
			translation_cache.put(key, translated);
		}
		return translated;
	}

	public static boolean checkIsTranslated( int id, String table_name, String defaultVal ){
		Query query3 = new Query("SELECT `translated` FROM Translated_name WHERE `element_id` ='"+id+"' and `table_name`='"+table_name+"' and `language_id`='"+langId+ "' LIMIT 1;");
		String translated = (String) Model.fetchSingleValue(query3);
		if(translated == null){
			return false;
		}
		return true;
	}
	
	public static void InsertTranslation( int id, String table_name, String translatedName ) {
		Translated_name trn = new Translated_name();
		trn.element_id = id;
		trn.table_name = table_name;
		trn.language_id = langId;
		trn.translated = translatedName;
		trn.insert();

	//	Initiator.logger.i(Constant.TAG,"InsertTranslation table_name" + trn.table_name +", translatedName" +translatedName+", id" +id+", langId" +langId );
	}
	

	private static Map<Integer, String> languages = null;
	public static String getlangCode(int language_id2) {
		if( languages == null){
			List<Language> ls = Model.fetchQuery(ModelQuery.select().from(Language.class).getQuery(),Language.class);
			languages = new HashMap<Integer, String>();
			for (Language lang : ls) {
				languages.put(lang.id, ""+lang.lang_code);
			}
		}else{
		}
		return languages.get(language_id2);
	}
	
	public static void resetCache(int id, int langId, String table_name ) {
		String key = id+"."+table_name+"."+langId;
		if(translation_cache.containsKey(key)){
			translation_cache.remove(key);
		}
	}
}
