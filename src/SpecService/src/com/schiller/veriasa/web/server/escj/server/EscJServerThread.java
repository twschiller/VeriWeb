package com.schiller.veriasa.web.server.escj.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import com.google.common.base.Predicate;
import com.schiller.veriasa.web.server.escj.EscJInterop;
import com.schiller.veriasa.web.server.escj.EscJInterop.EscjOptions;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.Clause.Status;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.VerificationRequest;

/**
 * ESC/Java2 verification thread
 * @author Todd Schiller
 */
public class EscJServerThread extends Thread{
	private final Socket socket;
	private final EscjOptions opt;
	private final Logger log;
	
	private static Map<VerificationRequest, ProjectResult> resultCache = 
		Collections.synchronizedMap(new WeakHashMap<VerificationRequest,ProjectResult>());
	
	public EscJServerThread(Socket socket,EscjOptions opt, Logger log) {
		super("EscJServerThread");
		this.socket = socket;
		this.opt = opt;
		this.log = log;
	}
	
	public void sendResponse(ObjectOutputStream oos, Object response){
		try {
			oos.writeObject(response);
		} catch (IOException e) {
			log.error("error sending response",e);
		}
	}
	
	public void run() {
		
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			log.error("error opening socket", e);
			e.printStackTrace();
			return;
		}
	
		Object packet = null;

		do{
			try {
				packet = ois.readObject();
			} catch (IOException e) {
				log.warn("Error reading from socket", e);
				continue;
			} catch (ClassNotFoundException e) {
				log.warn("Malformed request", e);
			}
			
			if (packet instanceof VerificationRequest){
				VerificationRequest request = (VerificationRequest) packet;
				ProjectResult escjOut = resultCache.get(request);

				if (escjOut == null){
					try {
						escjOut = EscJInterop.annotateAndRun(
								request.getProject(), 
								new File(EscJServer.WORKSPACE_DIR,request.getProject().getName()),
								opt,
								new Predicate<Clause>(){
									@Override
									public boolean apply(Clause spec) {
										return spec.getStatus().equals(Status.PENDING) || spec.getStatus().equals(Status.KNOWN_GOOD);
									}
								});
					} catch (ParseException e) {
						log.error("Error parsing ESC/Java2 output", e);
						sendResponse(oos,e);
						break;
					}  catch (Exception e) {
						log.error("Error creating result", e);
						sendResponse(oos, e);
						break;
					}
				}

				resultCache.put(request, escjOut);
				sendResponse(oos, escjOut);
				break;
			}else if (packet != null){
				log.warn("Malformed request");
			}
				
		}while (packet != null);
		
		try {
			oos.close();
			ois.close();
			socket.close();
		} catch (IOException e) {
			log.error("error closing socket",e);
		}
	}
}
