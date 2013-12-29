package com.schiller.veriasa.util;

import com.google.common.base.Predicate;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.UserAction;

/**
 * Log utility methods (e.g., predicates over log entries)
 * @author Todd Schiller
 */
public class LogUtil {

	/**
	 * <tt>true</tt> for user actions performed by user <tt>webId</tt>
	 * @param webId the user
	 * @return <tt>true</tt> for user actions performed by user <tt>webId</tt>
	 */
	public static Predicate<LogEntry> forUser(final String webId){
		return new Predicate<LogEntry>(){
			@Override
			public boolean apply(LogEntry e) {
				return 	e.getAction() instanceof UserAction &&
						((UserAction) e.getAction()).getUser().getWebId().equalsIgnoreCase(webId);
			}
		};	
	}
	
	/**
	 * @param action reference action
	 * @return a predicate accepting log entries with the same user that performed <tt>action</tt>
	 */
	public static Predicate<LogEntry> forUser(final UserAction action){
		return forUser(action.getUser().getWebId());
	}
	
	/**
	 * <tt>true</tt> for log entries occurring before <tt>entry</tt>
	 * @param entry reference log entry
	 * @return the reference log entry
	 */
	public static Predicate<LogEntry> before(final LogEntry entry){
		return new Predicate<LogEntry>(){
			@Override
			public boolean apply(LogEntry e) {
				return e.getTimestamp() < entry.getTimestamp();
			}
		};
	}
}
