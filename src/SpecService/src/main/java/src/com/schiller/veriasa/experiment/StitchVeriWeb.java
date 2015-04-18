package com.schiller.veriasa.experiment;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Combine multiple VeriWeb logs into a single log
 * @author Todd Schiller
 */
public class StitchVeriWeb {

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		String project = "StackAr";
		
		File dir = new File("/home/tws/projects/asa/veriweb-paper/study/results/vworker/veriweb");
		String servers[] = {"beta", "delta"};
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(dir, project + ".vlog")));
		
		for (String s : servers){
			System.out.println("Reading " +  project + "." + s + ".vlog");
			
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(dir, project + "." + s + ".vlog")));

			Object o;

			try {

				while ((o = ois.readObject()) != null){
					oos.writeObject(o);
				}

			}catch(EOFException e){
				//ok
			}
			
			ois.close();
		}

		oos.close();
	}

}
