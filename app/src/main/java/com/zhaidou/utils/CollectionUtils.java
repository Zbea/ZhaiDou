package com.zhaidou.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CollectionUtils {

	public static boolean isNotNull(Collection<?> collection) {
		if (collection != null && collection.size() > 0) {
			return true;
		}
		return false;
	}
	
	/** map转list
	  * @Title: set2list
	  * @return List<String>
	  * @throws
	  */
	public static List<String> set2list(Set<String> set){
        List<String> list = new ArrayList<String>();
		Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            if (!TextUtils.isEmpty(next))
                list.add(next);
        }
		return list;
	}
}
