package com.schiller.veriasa.web.server;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.schiller.veriasa.web.server.callgraph.ProblemGraph.ProblemGraphNode;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.ProblemAction;
import com.schiller.veriasa.web.server.logging.ProblemAction.ActionType;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;

/**
 * A VeriWeb user
 * @author Todd Schiller
 */
public class User implements Serializable {
	private static final long serialVersionUID = 3L;

	public enum Service { NONE, MTURK, VWORKER };
	
	private Long userId;
	private String webId;
	private Long lastAlive = System.currentTimeMillis();
	
	private Service service;
	private transient Logger log;
	
	private int problemsSolved = 0;
	
	private String desiredProject;
	
	private Double mturkRate;
	private List<String> mturkCodes = Lists.newLinkedList();
	private Integer previewSolved = 0;
	private String assignmentId = null;
	
	private Stack<ProblemGraphNode> prefer = new Stack<ProblemGraphNode>();
	private Set<ProblemGraphNode> avoid = Sets.newHashSet();
	
	/**
	 * The problem the user is currently solving
	 */
	private transient ProblemGraphNode activeProblem;
	
	/**
	 * The last problem the user worked on
	 */
	private transient ProblemGraphNode lastActiveProblem;
	
	/**
	 * The (possibly shared) project the user is working on
	 */
	private transient ProjectState project;
	
	/**
	 * The active specification; local to the user 
	 */
	private transient ProjectSpecification localSpecification;
	
	private transient String sharedInstanceKey;
	
	@SuppressWarnings("unused")
	private User(){	
	}
	
	public User(Long userId, Service service, String desiredProject) {
		this(userId, service, desiredProject, null);
	}
	
	public User(Long userId, Service service, String desiredProject, String webId) {
		super();
		this.userId = userId;
		this.service = service;
		this.desiredProject = desiredProject;
		this.webId = webId;
			
		restoreLogger();
		assignCode(Integer.toHexString(this.hashCode()));
	}
	
	public User(Long userId, Service service, String desiredProject, String webId, Double atmRate, String assignmentId) {
		this(userId, service, desiredProject, webId);
		this.mturkRate = atmRate;
		this.assignmentId = assignmentId;
	}
	
	/**
	 * @return <code>true</code> iff the user owns the project instance he is working on
	 */
	public boolean ownsProject(){
		return project == null ? false : project.isOwnedBy(this);
	}

	public Stack<ProblemGraphNode> getPreferences(){
		return prefer;
	}
	
	/**
	 * @return the last problem the user worked on
	 */
	public ProblemGraphNode getLastActive(){
		return lastActiveProblem;
	}
	
	public Set<ProblemGraphNode> getAvoiding(){
		return avoid;
	}
	
	/** 
	 * Start a new problem
	 * @param problem the problem
	 */
	public void startProblem(ProblemGraphNode problem){
		synchronized(project){
			activeProblem = problem;
			lastActiveProblem = problem;
			localSpecification = new ProjectSpecification(project.getName(), project.getActiveSpec().getTypeSpecs());
			
			project.getProblems().start(this, activeProblem);
			
			log.info("START PROBLEM " + problem);
			logProblemAction(ActionType.Start);
		}
	}
	
	/**
	 * Quit the current problem
	 */
	public void cancelProblem(){
		logProblemAction(ActionType.Cancel);
		log.info("CANCEL PROBLEM " + activeProblem);
		project.getProblems().cancel(this, activeProblem);
		activeProblem = null;
	}
	
	private void logProblemAction(ActionType action){
		VeriServiceImpl.tryLog(new LogEntry(
				new ProblemAction(this, activeProblem.getProblem(), action), 
				project));
	}
	
	/**
	 * @return the (possibly shared) project the user is working on
	 */
	public ProjectState getProject(){
		return project;
	}
	
	/**
	 * @return the problem the user currently working on
	 */
	public ProblemGraphNode getActiveProblem(){
		return activeProblem;
	}
	
	/**
	 * @return the user's server id
	 */
	public long getId(){
		return userId;
	}
	
	public void setProject(ProjectState project){
		this.project = project;
	}
	
	public ProjectSpecification getLocalSpecification(){
		return localSpecification;
	}
	
	public void markAlive(){
		synchronized(lastAlive){
			lastAlive = System.currentTimeMillis();
		}
	}
	
	/**
	 * @return <code>true</code> iff the server has seen the user recently
	 */
	public boolean isAlive(){
		synchronized(lastAlive){
			return (System.currentTimeMillis() - lastAlive) < VeriServiceImpl.IS_ALIVE_TIMEOUT;
		}
	}
	
	public Service getService(){
		return service;
	}
	
	public void log(Level level, String msg){
		log.log(level, toString() + " " + msg);
	}
	
	public void log(Level level, String msg, Throwable t){
		log.log(level, toString() + " " + msg,t);
	}
		
	public String generateCode(){
		String code = Integer.toHexString(this.hashCode());
		assignCode(code);
		return code;
	}
	
	private void assignCode(String code){
		log(Level.INFO, "ASSIGNED CODE " + code);
		synchronized(mturkCodes){
			mturkCodes.add(code);
		}
	}
	
	public int getNumSolved(){
		return problemsSolved;
	}
	
	public void addPreference(ProblemGraphNode p){
		log(Level.INFO, "PREFERS " + p);
		prefer.push(p);
	}

	public void addAvoid(ProblemGraphNode p){
		log(Level.INFO, "AVOIDS " + p);
		avoid.add(p);
	}
	
	public void addSolved(){
		logProblemAction(ActionType.Solved);
		log(Level.INFO, "SOLVED PROBLEM " + activeProblem);
		
		problemsSolved++;
	}
	
	public String getDesiredProject(){
		return desiredProject;
	}

	public String getWebId(){
		return webId;
	}
		
	/**
	 * Get the number of problems this user solved in preview mode
	 * @return the number of problems this user solved in preview mode
	 */
	public int getPreviewSolved(){
		return previewSolved;
	}
	
	/**
	 * Get the money earned per problem for a Mechanical Turk User
	 * @return money earned per problem for Mechanical Turk
	 */
	public Double getMTurkRate(){
		return mturkRate;
	}
	
	/**
	 * Get the user's assignment id
	 * @return the user's assignment id
	 */
	public String getAssignmentId(){
		return assignmentId;
	}
	
	
	public void clearMTurkInfo(){
		problemsSolved = 0;
		webId = null;
		assignmentId = null;
	}
	
	/**
	 * Set the web id for the user
	 * @param id the new web id
	 */
	public void setWebId(String id){
		this.webId = id;
	}
	
	public void setAssignmentId(String id){
		this.assignmentId = id;
	}

	
	
	/**
	 * Initialize the user logger
	 */
	public void restoreLogger(){
		this.log = Logger.getLogger("SessionLogger." + ((webId != null) ? webId : userId));
	}
	
	@Override
	public String toString() {
		if (webId != null){
			return "User [id=" + webId + ", srv=" + service + ", solved=" + problemsSolved + "]";
		}else{
			return "User [id=" + Long.toHexString(userId) + ", srv=" + service + ", solved=" + previewSolved + "]";
		}
	}

	/**
	 * @return the sharedInstanceKey
	 */
	public String getSharedInstanceKey() {
		return sharedInstanceKey;
	}

	/**
	 * @param sharedInstanceKey the sharedInstanceKey to set
	 */
	public void setSharedInstanceKey(String sharedInstanceKey) {
		this.sharedInstanceKey = sharedInstanceKey;
	}
}