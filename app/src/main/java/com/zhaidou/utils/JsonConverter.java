package com.zhaidou.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonConverter {
	public static void convert(JSONObject object, List<Map<String, Object>> items) {
		
	}
	
//	public static void convert(JSONArray array, List<Map<String, Object>> items) {
//		for (int i = 0; i < array.length(); i++) {
//			JSONObject jsonObj = array.getJSONObject(i);
//			Map<String, Object> data = new HashMap<String, Object>();
//
//			for (Iterator<String> iterator = jsonObj.keys(); iterator.hasNext();) {
//				String key = iterator.next();
//
////				data.put(key, jsonObj.get(key));
//
//			}
//			items.add(data);
//		}
//	}
//
}
