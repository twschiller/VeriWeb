package com.schiller.veriasa.web.server.escj.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.xml.XMLLayout;

import com.schiller.veriasa.web.server.escj.EscJInterop.EscjOptions;

/**
 * ESC/Java2 verification server
 * @author Todd Schiller
 */
public class EscJServer {

	private static final Logger log = Logger.getLogger("escj-server");
	
	public static int PORT;
	public static String WORKSPACE_DIR;
	
	public static void main(String [] args){

		if (args.length < 6){
			System.err.println("Usage: escj-server PORT WORKSPACE_DIR LOG_DIR ESCTOOLS_DIR SIMPLIFY JAVA_HOME [OBSERVE]*");
			System.exit(1);
		}
		
		PORT = Integer.parseInt(args[0]);
		WORKSPACE_DIR = args[1];
	
		File logDir = new File(args[2]);
		String simplify = args[4];
		String jdkHome = args[5];
		String escDir = args[3];
		
		Set<String> observe = new HashSet<String>();
		for (int i = 6; i < args.length; i++){
			observe.add(args[i]);
		}
		
		EscjOptions opt = new EscjOptions(escDir, simplify, jdkHome, logDir, observe);
	
		try {
			BasicConfigurator.configure(new RollingFileAppender(new XMLLayout(),"escj-server.log"));
		} catch (IOException e) {
			log.fatal("error opening log file",e);
			System.err.println("Fatal error opening log file: " + e.getMessage());
			System.exit(-1);
		}
				
		log.info("Port:" + PORT);
		log.info("Workspace:" + WORKSPACE_DIR);
		log.info("Log Directory:" + logDir.getAbsolutePath());
		log.info("ESCJ Tools:" + opt.getEscjDir());
		log.info("Simplify:" + simplify);
		log.info("Java:" + jdkHome);
		
		for (String compilationUnit : observe){
			log.info("Dumping enabled:" + compilationUnit);
		}
	
		ServerSocket serverSocket = null;
		boolean listening = true;
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			log.fatal("Error listening on port " + PORT, e);
			System.exit(-1);
		}

		log.info("Listening on port " + PORT);
		
		while (listening){
			Socket socket;
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				log.error("Error accepting socket connection", e);
				continue;
			}
			new EscJServerThread(socket, opt, log).start();
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			log.error("Error closing socket",e);
			System.exit(-1);
		}
	}
}
