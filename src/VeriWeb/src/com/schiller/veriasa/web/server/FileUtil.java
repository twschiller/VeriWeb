package com.schiller.veriasa.web.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * File utility functions
 * @author Todd Schiller
 */
public class FileUtil {
	
	/**
	 * Read a single serialized object from a file
	 * @param file the file
	 * @return the object
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected static <T> T readObject(File file) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		@SuppressWarnings("unchecked")
		T result = (T) ois.readObject();
		ois.close();
		return result;
	}
	
	/**
	 * Write <code>object</code> to <code>file</code>
	 * @param file the file
	 * @param object the object to write
	 * @throws IOException
	 */
	protected static <T> void writeObject(File file, T object) throws IOException{;
		synchronized(object){
			BufferedOutputStream buffer = new BufferedOutputStream(new FileOutputStream(file));
			ObjectOutputStream out = new ObjectOutputStream(buffer);
			out.writeObject(object);
			out.close();
		}
	}

}
