package com.schiller.veriasa.web.server.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.schiller.veriasa.web.server.ProjectState;

public class VeriLog{
	public static final String DATE_FORMAT_NOW = "yyyyMMdd";
	
	private final ObjectOutputStream out;

    public VeriLog(File dir, String base, String suffix) throws IOException{
    		
    	Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_NOW);
    	String date = format.format(calendar.getTime());
    	
    	File file = new File(dir, base + "." + date + suffix);
    	
    	// look for the next available filename
    	if (file.exists()){
    		for (int i = 2; i < 100; i++){
    			file = new File(dir, base + "." + date + "." + i  + suffix);
    			if (!file.exists()){
    				break;
    			}
    		}
    	}
    	
    	if (file.exists()){
    		throw new RuntimeException("Failed to find non-existant log filename");
    	}
    	
    	//TODO: buffer log output?
    	out = new ObjectOutputStream(new FileOutputStream(file));
    }
	
    public synchronized void closeLog() throws IOException{
    	out.close();
    }
    
    public synchronized void write(LogEntry entry) throws IOException{
    	ProjectState context = entry.getContext();
    	synchronized(context){
    		out.writeObject(entry);
        	out.writeObject(context.getActiveSpec());
    		out.flush();
    	}
	}
}
