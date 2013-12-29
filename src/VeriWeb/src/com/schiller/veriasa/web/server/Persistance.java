package com.schiller.veriasa.web.server;

import java.io.File;
import java.io.IOException;

import com.schiller.veriasa.web.shared.config.EscJResponseException;
import com.schiller.veriasa.web.shared.escj.ProjectResult;

/**
 * Methods for loading and storing session state
 * @author Todd Schiller
 */
public abstract class Persistance {

	public static File dirForSharedProject(ProjectDescriptor project){
		return new File(VeriServiceImpl.SESSION_DIR, project.getProjectName());
	}
	
	public static File dirForUser(String webId){
		return new File(VeriServiceImpl.SESSION_DIR, webId);
	}
	
	public static File fileForUser(String webId){
		return new File(dirForUser(webId), webId + ".user");
	}
	
	public static boolean userDataExists(String webId){
		return fileForUser(webId).exists();
	}
	
	public static boolean projectDataExists(ProjectDescriptor sharedProject){
		return getLatestState(sharedProject) >= 0;
	}
	
	public static boolean projectDataExists(String webId, String project){
		return getLatestState(webId, project) >= 0;
	}
	
	public static User hydrateUser(String webId) throws IOException, ClassNotFoundException{
		User user = FileUtil.<User>readObject(fileForUser(webId));
		user.restoreLogger();
		return user;
	}
	
	public static ProjectState hydrateProject(ProjectDescriptor sharedProject) throws IOException, ClassNotFoundException, EscJResponseException{
		File stateFile = new File(
				dirForSharedProject(sharedProject),
				sharedProject.getProjectName() + "-" + getLatestState(sharedProject) + ".state");
		
		ProjectState state = FileUtil.<ProjectState>readObject(stateFile);
		state.restoreLogging();
		state.getDescriptor().hydrate(sharedProject);
		state.getProblems().init();
		
		ProjectResult result = VeriServiceImpl.getResult(state.getActiveSpec());
		
		VeriUpdate.updateSelectRequiresProblems(state, result);
		VeriUpdate.updateWriteRequiresProblems(state, result);
		VeriUpdate.updateWriteEnsuresProblems(state, result);
		VeriUpdate.updateWriteExsuresProblems(state, result);

		return state;
	}
	
	public static ProjectState hydrateProject(String webId, ProjectDescriptor project) throws IOException, ClassNotFoundException, EscJResponseException{
		File stateFile = new File(
				dirForUser(webId),
				project.getProjectName() + "-" + getLatestState(webId, project.getProjectName()) + ".state");
		
		ProjectState state = FileUtil.<ProjectState>readObject(stateFile);
		state.restoreLogging();
		state.getDescriptor().hydrate(project);
		state.getProblems().init();
		
		ProjectResult result = VeriServiceImpl.getResult(state.getActiveSpec());
		
		VeriUpdate.updateSelectRequiresProblems(state, result);
		VeriUpdate.updateWriteRequiresProblems(state, result);
		VeriUpdate.updateWriteEnsuresProblems(state, result);
		VeriUpdate.updateWriteExsuresProblems(state, result);

		return state;
	}
	
	/**
	 * Create the directory for <code>user</code>, if it does not already exist
	 * @param user the user
	 * @return the user directory
	 */
	private static File makeUserDirectory(User user){
		File userDir = dirForUser(user.getWebId());
		if (!userDir.exists()){
			userDir.mkdir();
		}
		return userDir;
	}
	
	private static File makeProjectDirectory(ProjectDescriptor sharedProject){
		File projectDir = dirForSharedProject(sharedProject);
		if (!projectDir.exists()){
			projectDir.mkdir();
		}
		return projectDir;
	}
	
	public static void record(ProjectState state) throws IOException{
		File projectDir = makeProjectDirectory(state.getDescriptor());
		File file = new File(projectDir, state.getName() + "-" + (getLatestState(state.getDescriptor()) + 1) + ".state");
		FileUtil.writeObject(file, state);
	}
	
	public static void record(User user, ProjectState state) throws IOException{
		File userDir = makeUserDirectory(user);
		File file = new File(userDir, state.getName() + "-" + (getLatestState(user.getWebId(), state.getName()) + 1) + ".state");
		FileUtil.writeObject(file, state);
	}

	public static void record(User user) throws IOException{
		makeUserDirectory(user);
		FileUtil.writeObject(fileForUser(user.getWebId()), user);
	}
		
	public static long getLatestState(String user, String project){
		File userDir = dirForUser(user);

		long latest = -1;
		
		if (userDir.exists()){
			for (String n : userDir.list()){
				String [] xx = n.split("[\\.-]");
					
				if (xx[0].equals(project)){
					latest = Math.max(latest, Long.parseLong(xx[1]));
				}
			}
			return latest;
		}
		return latest;
	}
	
	public static long getLatestState(ProjectDescriptor sharedProject){
		File projectDir = dirForSharedProject(sharedProject);
		
		long latest = -1;
		
		if (projectDir.exists()){
			for (String n : projectDir.list()){
				String [] xx = n.split("[\\.-]");
					
				if (xx[0].equals(sharedProject.getProjectName())){
					latest = Math.max(latest, Long.parseLong(xx[1]));
				}
			}
			return latest;
		}
		return latest;
	}
}
