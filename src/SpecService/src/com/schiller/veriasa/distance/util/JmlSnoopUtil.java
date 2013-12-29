package com.schiller.veriasa.distance.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.schiller.veriasa.util.ParseSource;
import com.schiller.veriasa.util.ParseSource.AlsoException;
import com.schiller.veriasa.util.ParseSource.BadCnt;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.TypeSpecification;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import edu.washington.cs.plse.jmlsnoop.logging.Edit;
import edu.washington.cs.plse.jmlsnoop.logging.LogEntry;
import edu.washington.cs.plse.jmlsnoop.logging.Snapshot;

/**
 * Utilities for reading the output logs of JML snoop
 * @author Todd Schiller
 */
public class JmlSnoopUtil {
	
	/**
	 * Load data for <code>project</code> from <code>directory</code>
	 * @param project project name (e.g., StackAr)
	 * @param directory the directory
	 * @return data Load data for <code>project</code> from directory <code>dir</code>
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<Pod> loadData(String project, File directory) throws FileNotFoundException, IOException{
		List<Pod> ps = Lists.newArrayList();
		
		for (final File userDir : directory.listFiles()){
			if (userDir.isDirectory() && !userDir.isHidden()){
				ps.addAll(make(userDir.getName(), project, readFiles(userDir)));	
			}
		}
		
		return ps;
	}
	
	/**
	 * Load JML Snoop log entries from <code>directory</code>, sorted in chronological order
	 * @param directory the directory
	 * @return JML Snoop log entries from <code>directory</code>, sorted in chronological order
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<LogEntry> readFiles(File directory) throws FileNotFoundException, IOException{
		List<LogEntry> xs = Lists.newArrayList();
		
		for (final File log : directory.listFiles()){
			if (log.isFile()){
				xs.addAll(readFile(log));
			}
		}

		Collections.sort(xs, new Comparator<LogEntry>(){
			@Override
			public int compare(LogEntry lhs, LogEntry rhs) {
				return Long.valueOf(lhs.getTimeStamp()).compareTo(rhs.getTimeStamp());
			}
		});
		
		return xs;
	}

	/**
	 * Load JML Snoop log entries from a file
	 * @param file the file
	 * @return the log entries from the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<LogEntry> readFile(File file) throws FileNotFoundException, IOException{
		List<LogEntry> xs = Lists.newArrayList();
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		Object o;
		
		try {
			while ((o = ois.readObject()) != null){
				if (o instanceof LogEntry){
					xs.add((LogEntry) o );
				}
			}
		}catch(EOFException e){
			//ok
		}catch (ClassNotFoundException e) {
			throw new Error(e);
		}
		
		return xs;
	}
	

	/**
	 * Create a common data interface data point
	 * @param timestamp the log entry time
	 * @param user the user
	 * @param project the project
	 * @param content the program source
	 * @return a common data interface data point
	 * @throws InvalidSpecification iff the source cannot be parsed
	 */
	private static Pod make(final long timestamp, final String user, final String project, final String content) throws InvalidSpecification{
		BadCnt<TypeSpecification> t = null;	

		try {
			t = ParseSource.readSpec(content, "\r\n");
		} catch (AlsoException cause) {
			throw new Error(cause);
		} catch (Exception cause){
			throw new InvalidSpecification(cause);
		}

		final ProjectSpecification spec = new ProjectSpecification(project, Lists.newArrayList(t.item));
		final int penalty = t.bad;

		return new Pod(){
			@Override
			public long getTimestamp() {
				return timestamp;
			}
			@Override
			public String getUser() {
				return user;
			}
			@Override
			public ProjectSpecification getSpec() {
				return spec;
			}
			@Override
			public int getPenalty() {
				return penalty;
			}
		};
	}
	
	/**
	 * Convert consecutive log entries into the common data interface. Fails if textual difference
	 * information is malformed.
	 * @param user the user
	 * @param project the project
	 * @param entries consecutive log entries
	 * @return the corresponding common data interface entries
	 */
	public static List<Pod> make(final String user, String project, List<LogEntry> entries){
		List<Pod> data = Lists.newArrayList();

		String lastKnown = null;
		
		boolean badPatch = false;
		
		int patchCount = 0;
		int patchesSkipped = 0;
		
		for (LogEntry e : entries){
			if (e instanceof Snapshot){
				final Snapshot s = (Snapshot) e;
			
				if (s.getPath().contains(project + ".java")){
					lastKnown = s.getContent();
					
					try {
						data.add(make(s.getTimeStamp(), user, project, lastKnown));
					} catch (InvalidSpecification e1) {
						// OK (since the user may be mid-edit)
					}
					
					badPatch = false;
				}
			} else if (e instanceof Edit && !badPatch){
				final Edit edit = (Edit) e;
				
				if (edit.getPath().contains(project + ".java")){
					patchCount++;
					Patch patch = DiffUtils.parseUnifiedDiff(edit.getUnifiedDiff());
					List<String> lines = Lists.newArrayList(Splitter.on("\r\n").split(lastKnown));
					List<String> patched;
					try {
						patched = (List<String>) DiffUtils.patch(lines, patch);
						lastKnown = Joiner.on("\r\n").join(patched);
					} catch (PatchFailedException e1) {
						badPatch = true;
						patchesSkipped++;
						continue;
					}
					
					try {
						data.add(make(edit.getTimeStamp(), user, project, lastKnown));
					} catch (InvalidSpecification ex) {
						// OK (since the user may be mid-edit)
					}
				}
			}
		}
		
		if (patchesSkipped > 0){
			throw new RuntimeException("Skipped " + patchesSkipped + " (of " + patchCount + ") patches for user " + user);
		}
				
		return data;
	}
}
