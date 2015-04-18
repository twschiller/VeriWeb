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

       	public static final String ESCJ_SERVER_HOST = "127.0.0.1";
        public static final int ESCJ_SERVER_PORT = 4444;

        public static final int IS_ALIVE_TIMEOUT = 3 * STAY_ALIVE_INTERVAL;
}
