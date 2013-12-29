package com.schiller.veriasa.distance.util;

import static com.google.common.collect.Iterables.transform;

import java.util.HashMap;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.UserAction;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;

/**
 * Utility methods for the unified data interface
 * @author Todd Schiller
 */
public class PodUtil {

	public static class MakePod implements Function<LogEntry, Pod>{
		private final HashMap<LogEntry, ProjectSpecification> lookup;
		
		public MakePod(HashMap<LogEntry, ProjectSpecification> lookup) {
			super();
			this.lookup = lookup;
		}

		@Override
		public Pod apply(final LogEntry entry) {
			return new Pod(){
				@Override
				public long getTimestamp() {
					return entry.getTimestamp();
				}
				@Override
				public String getUser() {
					return ((UserAction) entry.getAction()).getUser().getWebId();
				}
				@Override
				public ProjectSpecification getSpec() {
					return lookup.get(entry);
				}
				@Override
				public int getPenalty() {
					return 0;
				}
			};
		}
	}
	
	/**
	 * Return a view of <code>data</code> for <code>user</code>
	 * @param data raw data
	 * @param user the user to view
	 * @return a view of <code>data</code> for <code>user</code>
	 */
	public static Iterable<Pod> forUser(Iterable<Pod> data, final String user){
		return Iterables.filter(data, new Predicate<Pod>(){
			@Override
			public boolean apply(Pod arg0) {
				return user.toUpperCase().equals(arg0.getUser().toUpperCase());
			}
		});
	}
	
	/**
	 * Get the set of users that <code>data</code> contains data for
	 * @param data data set
	 * @return the set of users that <code>data</code> contains data for
	 */
	public static Set<String> users(Iterable<Pod> data){
		return Sets.newHashSet(transform(data, new Function<Pod,String>(){
			@Override
			public String apply(Pod p) {
				return p.getUser();
			}
		}));
	}
}
