package com.schiller.veriasa.web.shared.config;

/**
 * Shared (between server and client) configuration options
 * @author Todd Schiller
 */
public final class SharedConfig {

	/**
	 * How often the client pings the server (in ms)
	 */
	public static final int STAY_ALIVE_INTERVAL = 7000;
	
	/**
	 * True iff Mechanical Turk users are each given their own project
	 */
	public static final boolean MTURK_OWNS_PROJECT = true;
}
