package com.schiller.veriasa.logexplore.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.schiller.veriasa.web.server.User;
import com.schiller.veriasa.web.server.logging.UserAction;
import com.schiller.veriasa.web.shared.logging.LogAction;

public class ViewUtil {

	public static String formatTimestamp(long t){
		return (new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS ")).format(new Date(t));
	}
	public static String userId(User u){
		return u.getWebId() == null ? Long.toHexString(u.getId()) : u.getWebId();
	}
	
	public static boolean hasId(User u){
		return u.getWebId() != null;
	}
	public static boolean hasId(LogAction a){
		if (a instanceof UserAction){
			User u = ((UserAction) a).getUser();
			return hasId(u);
		}else{
			return false;
		}
	}
	
	public static String userId(LogAction a){
		if (a instanceof UserAction){
			User u = ((UserAction) a).getUser();
			return ViewUtil.userId(u);
		}else{
			return null;
		}
	}
	
}
