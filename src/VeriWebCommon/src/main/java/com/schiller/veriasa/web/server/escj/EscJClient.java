package com.schiller.veriasa.web.server.escj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.schiller.veriasa.web.shared.escj.*;

/**
 * Client for communicating with ESC/Java2 service
 * @author Todd Schiller
 */
public class EscJClient{

	private Socket socket;
	private ObjectOutputStream oos = null;
	private ObjectInputStream ois = null;
	
	public EscJClient(String host, int port) throws UnknownHostException, IOException{
		socket = new Socket(host, port);
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
	}
	
	public ProjectResult tryProjectSpec(VerificationRequest request) throws IOException, ClassNotFoundException {
		oos.writeObject(request);
		
		Object result = ois.readObject();
		
		if (result instanceof ProjectResult){
			return (ProjectResult) result;
		}else if (result instanceof Exception){
			throw new RuntimeException("ESC/Java2 returned an exception: " + ((Exception)result).getMessage(), (Exception)result);
		}else{
			throw new RuntimeException("Read unexpected object from ESC/Java2 of type " + result.getClass().getName());
		}
	}
}
