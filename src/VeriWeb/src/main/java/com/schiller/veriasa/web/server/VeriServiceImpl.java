package com.schiller.veriasa.web.server;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.schiller.veriasa.callgraph.CallGraphNode;
import com.schiller.veriasa.web.client.VeriService;
import com.schiller.veriasa.web.client.views.ClauseWriter.WriteMode;
import com.schiller.veriasa.web.server.User.Service;
import com.schiller.veriasa.web.server.callgraph.ProblemGraph.ProblemGraphNode;
import com.schiller.veriasa.web.server.escj.EscJClient;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.ProjectAction;
import com.schiller.veriasa.web.server.logging.SessionAction;
import com.schiller.veriasa.web.server.logging.SwitchModeAction;
import com.schiller.veriasa.web.server.logging.VeriLog;
import com.schiller.veriasa.web.server.logging.VoteAction;
import com.schiller.veriasa.web.shared.config.BadSessionException;
import com.schiller.veriasa.web.shared.config.EscJResponseException;
import com.schiller.veriasa.web.shared.config.FeedbackException;
import com.schiller.veriasa.web.shared.config.JmlParseException;
import com.schiller.veriasa.web.shared.config.NoSuchProjectException;
import com.schiller.veriasa.web.shared.config.SharedConfig;
import com.schiller.veriasa.web.shared.config.UnknownServerException;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.dnd.InvElement;
import com.schiller.veriasa.web.shared.dnd.InvElement.RefType;
import com.schiller.veriasa.web.shared.dnd.InvFixed;
import com.schiller.veriasa.web.shared.dnd.InvLocal;
import com.schiller.veriasa.web.shared.dnd.InvRef;
import com.schiller.veriasa.web.shared.escj.Chunk;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.VerificationRequest;
import com.schiller.veriasa.web.shared.feedback.Feedback;
import com.schiller.veriasa.web.shared.intelli.FieldEdge;
import com.schiller.veriasa.web.shared.intelli.IntelliEdge;
import com.schiller.veriasa.web.shared.intelli.IntelliNode;
import com.schiller.veriasa.web.shared.intelli.MethodEdge;
import com.schiller.veriasa.web.shared.logging.LogAction;
import com.schiller.veriasa.web.shared.messaging.ImpossibleMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage;
import com.schiller.veriasa.web.shared.messaging.UserMessage.Vote;
import com.schiller.veriasa.web.shared.messaging.UserMessageThread;
import com.schiller.veriasa.web.shared.parsejml.JmlSpan;
import com.schiller.veriasa.web.shared.problems.Documentation;
import com.schiller.veriasa.web.shared.problems.MaybeProblem;
import com.schiller.veriasa.web.shared.problems.MethodDocumentation;
import com.schiller.veriasa.web.shared.problems.MethodProblem;
import com.schiller.veriasa.web.shared.problems.ProjectFinished;
import com.schiller.veriasa.web.shared.problems.SelectRequiresProblem;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;
import com.schiller.veriasa.web.shared.solutions.ImpossibleInfo.Reason;
import com.schiller.veriasa.web.shared.solutions.Solution;
import com.schiller.veriasa.web.shared.update.SelectRequiresUpdate;
import com.schiller.veriasa.web.shared.update.Update;
import com.schiller.veriasa.web.shared.update.WriteEnsuresUpdate;
import com.schiller.veriasa.web.shared.update.WriteExsuresUpdate;
import com.schiller.veriasa.web.shared.update.WriteRequiresUpdate;

/**
 * The server side implementation of the RPC service.
 */
public class VeriServiceImpl extends RemoteServiceServlet implements VeriService {

	private static final long serialVersionUID = 1L;
	
	static String ESCJ_SERVER_HOST = "127.0.0.1";
	static int ESCJ_SERVER_PORT = 4444;
	static int IS_ALIVE_TIMEOUT = 3 * SharedConfig.STAY_ALIVE_INTERVAL;
	
	private static Boolean configOk = false;
	private static File WORKSPACE_DIR = new File(System.getProperty("user.home"),"/asa/projs");
	private static File LOG_DIR = new File(System.getProperty("user.home"),"asa/log/");
	protected static File SESSION_DIR = new File(System.getProperty("user.home"),"asa/sessions/");
	
	private final static String LOG_SUFFIX = ".vlog";

	private final static String CALL_GRAPH_SUFFIX = ".graph";
	private final static String BASE_SUFFIX =  ".asa";
	private final static String INFERRED_SUFFIX = ".inf.asa";
	private final static String DEF_SUFFIX = ".def";
	private final static String DTRACE_SUFFIX = ".dtrace";
	private final static String INTELLI_SUFFIX = ".intelli";
	private final static String SESSION_KEY_STATE = "state";
	private final static Logger sharedLog = Logger.getLogger("VeriService");
	
	private static Map<VerificationRequest, ProjectResult> resultCache = 
		Collections.synchronizedMap(new WeakHashMap<VerificationRequest,ProjectResult>());
	
	private static final String FORCE_PROBLEM = null;
	
	private static final String DEFAULT_PROJECT = "FixedSizeSet";
	private static final String [] PROJECT_NAMES = new String[]{ "FixedSizeSet" };

	private static final Set<String> idsSeen = Collections.synchronizedSet(new HashSet<String>());
	
	private static Long curId = System.currentTimeMillis();
	
	public static HashMap<String, VeriLog> logs = Maps.newHashMap();
	 
	private static HashMap<String, ProjectDescriptor> projects = Maps.newHashMap();
	
	private static ConcurrentMap<Long, User> activeUsers = Maps.newConcurrentMap();
		
	private static ConcurrentMap<String, InvElement> eltCache = Maps.newConcurrentMap();
	private static ConcurrentMap<String, JmlSpan> spanCache = Maps.newConcurrentMap();
	
	/**
	 * Projects shared by multiple users. Project Name -> Shared Project Key -> Project Instances
	 */
	private static Table<String, String, List<ProjectState>> sharedInstances = HashBasedTable.create();
	
	public static final String DEFAULT_SHARED_INSTANCE_KEY = "default";
	
	/**
	 * Projects owned by a single user
	 */
	private static List<ProjectState> ownedInstances = Lists.newArrayList();
	
	public static synchronized void tryLog(LogEntry entry){
		try {
			logs.get(entry.getContext().getName()).write(entry);
		} catch (Exception ex){
			sharedLog.error("Logging error", ex);
		}
	}
	
	public static synchronized void tryLog(LogAction action, String project, String sharedInstanceKey){
		ProjectState context = sharedInstances.get(project, sharedInstanceKey).get(0);
		tryLog(new LogEntry(action, context));
	}
	
	/**
	 * Add a project to the pool of shared project instances.
	 * @param name the project name
	 * @param state the state of the project
	 */
	private void addSharedProject(String name, String sharedInstanceKey, ProjectState state){
		synchronized(sharedInstances){
			if (!sharedInstances.contains(name, sharedInstanceKey)){
				sharedInstances.put(name, sharedInstanceKey, new ArrayList<ProjectState>());
			}
			sharedInstances.get(name, sharedInstanceKey).add(state);
		}
	}
	
	/**
	 * get the user's session
	 * @return the user's session
	 */
	public HttpSession getSession(){
		return this.getThreadLocalRequest().getSession(false);
	}
	
	/**
	 * true iff the user has an existing session
	 * @return true iff the user has an existing session
	 */
	public boolean hasSession(){
		return getSession() != null;
	}
	
	/**
	 * get the user "bean", reloading if necessary
	 * @return the user "bean"
	 */
	public User getUserState() throws BadSessionException{
		User user = (User) getSession().getAttribute(SESSION_KEY_STATE);
		if (user == null){
			throw new BadSessionException("The session contains no state");
		}	
		return user; 
	}
	
	/**
	 * Get the service type (e.g., MTURK or VWORKER) the user is using
	 * @param service the name of the service
	 * @return the service type the user is using
	 */
	public static Service getServiceByName(String service){
		Service s = User.Service.NONE;
		if (service != null && service.trim().toUpperCase().equals("MTURK")){
			s =  User.Service.MTURK;
		}else if (service != null && service.trim().toUpperCase().equals("VWORKER")){
			s =  User.Service.VWORKER;
		}
		return s;
	}

	@Override
	public MaybeProblem initSession(String service, String project, String webId, String assignmentId, String sharedInstanceKey, boolean share) throws NoSuchProjectException {
		boolean restart = false;
		
		if (project == null){
			project = DEFAULT_PROJECT;
		}
		
		if (!hasSession()){
			if (Persistance.userDataExists(webId)){
				sharedLog.debug("Restoring old user " + webId);
				
				try {
					User user = Persistance.hydrateUser(webId);
					this.getThreadLocalRequest().getSession(true).setAttribute(SESSION_KEY_STATE, user);
				} catch (Exception e) {
					sharedLog.fatal("Error restoring user " + webId, e);
				} 
				
				restart = true;
			}else{
				sharedLog.debug("Creating new user " + webId);
				userInit(getServiceByName(service), project, webId, assignmentId, sharedInstanceKey, share );			
			}
		}else{
			restart = true;
		}

		sharedLog.info("User requests project " + project);
		
		User user = getUserState();
		
		if (restart && user.getAssignmentId() == null){
			user.setAssignmentId(assignmentId);
		}
		
		if ((user.getWebId() == null && webId != null) || (webId != null && !webId.equals(user.getWebId()))){
			//update web id (but don't erase it)
			user.setWebId(webId);
		}
		
		if (webId != null){
			idsSeen.add(user.getWebId());
		}
		
		user.setSharedInstanceKey(sharedInstanceKey == null ? DEFAULT_SHARED_INSTANCE_KEY : sharedInstanceKey);
		
		synchronized(sharedInstances){
			if (!sharedInstances.contains(project, user.getSharedInstanceKey())){
				addSharedProject(project, user.getSharedInstanceKey(), createNewProject(project));
			}
		}
		
		synchronized(activeUsers){
			if (user.getActiveProblem() != null){
				user.cancelProblem();
			}
			
			if (restart){
				if (share){
					// Use a shared instance
					tryLog(new SessionAction(user, SessionAction.ActionType.Restart), user.getDesiredProject(), user.getSharedInstanceKey());
				}else{
					assignOwnedProject(user, project);
					tryLog(new LogEntry(new SessionAction(user, SessionAction.ActionType.Restart), user.getProject()));
				}
				user.log(Level.INFO,"RESTART SESSION");
			}else{
				if (share){
					tryLog(new SessionAction(user, SessionAction.ActionType.Start), user.getDesiredProject(), user.getSharedInstanceKey());
				}else{
					tryLog(new LogEntry(new SessionAction(user, SessionAction.ActionType.Start), user.getProject()));
				}
			}
			
			activeUsers.put(user.getId(), user);
		}
		
		user.markAlive();
		
		return requestProblem();
	}
	
	/**
	 * Create user bean, reloading if necessary
	 * @param service the service, e.g. MTURK, the user is using
	 * @param project the requested project
	 * @param webId the username or null
	 * @throws NoSuchProjectException the request project doesn't exist
	 */
	public void userInit(User.Service service, String project, String webId, String assignmentId, String sharedKey, boolean share) throws NoSuchProjectException{
		
		long userId;
		
		synchronized(curId){
			userId = ++curId;
		}

		//create a new state
		User user = new User(userId, service, project, webId);
		
		//create a new problem instance, if the user owns their project
		if (!share){
			assignOwnedProject(user, project);
		}
		
		user.setSharedInstanceKey(sharedKey);
		
		//create the session
		user.log(Level.INFO,"USER IP:" + getThreadLocalRequest().getRemoteAddr());
		if (webId != null){
			user.log(Level.INFO, "WEBID: " + webId);
		}
		
		try {
			Persistance.record(user);
		} catch (IOException e) {
			sharedLog.error("could not save user " + webId,e);
		}
		
		this.getThreadLocalRequest().getSession(true).setAttribute(SESSION_KEY_STATE, user);
	}
	
	/**
	 * Have <code>user</code> start <code>project</code>; the user owns the project
	 * instance 
	 * @param user the user
	 * @param project the project
	 */
	private void assignOwnedProject(User user, ProjectState project){
		user.setProject(project);
		
		try {
			Persistance.record(user, project);
		} catch (IOException e) {
			user.log(Level.FATAL,"Error recording project state",e);
		}
		
		try {
			VeriLog log = logs.get(user.getProject().getName());
			log.write(new LogEntry(new ProjectAction(project.getName(), ProjectAction.ActionType.Start), project));
		} catch (IOException e) {
			user.log(Level.ERROR,"Error logging start project action",e);
		}
		
		synchronized(ownedInstances){
			ownedInstances.add(project);
		}
	}
	
	/**
	 * Assign user to project of given name, creating a new project instance if the
	 * user's active project is not the project of the given name
	 * @param user the user
	 * @param project the project name
	 * @throws NoSuchProjectException iff project with given name is not available
	 */
	private void assignOwnedProject(User user, String project) throws NoSuchProjectException{
		synchronized(user){
			if (!projects.containsKey(project)){
				throw new NoSuchProjectException(project);
			}else if (user.getProject() != null && user.getProject().getName().equals(project)) {
				return;
			}else{
				if (Persistance.projectDataExists(user.getWebId(), project)){
					try {
						ProjectState state = Persistance.hydrateProject(user.getWebId(), projects.get(project));
						state.setOwner(user);
						assignOwnedProject(user, state);	
					} catch (Exception e) {
						sharedLog.fatal("Error restoring project " + project + " for user " + user.getWebId(),e);
					}
				}else{
					ProjectState state = new ProjectState(projects.get(project), user);
					assignOwnedProject(user,state);	
				}
			}
		}
	}
	
	@Override
	public void init(ServletConfig config){	
		try {
			super.init(config);
		} catch (ServletException e2) {
			throw new RuntimeException("Error initializing configuration.");
		}
		synchronized(configOk){
		    	if (!configOk){
				
				BasicConfigurator.configure();
				
				sharedLog.info("Java version: " + System.getProperty("java.version"));
				
				if (config.getServletContext().getInitParameter("workspace") != null){
					WORKSPACE_DIR = new File(config.getServletContext().getInitParameter("workspace"));
				}else if(System.getenv().containsKey("VERIASA_PROJS")){
					WORKSPACE_DIR = new File(System.getenv("VERIASA_PROJS"));
				}else{
					sharedLog.warn("Defaulting to WORKSPACE to :" + WORKSPACE_DIR.getAbsolutePath());
				}
				
				if (!WORKSPACE_DIR.exists() || !WORKSPACE_DIR.isDirectory()){
					throw new RuntimeException("Invalid workspace directory " + WORKSPACE_DIR.getAbsolutePath());
				}
				
				sharedLog.info("Workspace directory:" + WORKSPACE_DIR.getAbsolutePath());
				
				if (config.getServletContext().getInitParameter("escj_host") != null){
					ESCJ_SERVER_HOST = config.getServletContext().getInitParameter("escj_host");
				}else{
					sharedLog.warn("Defaulting to ESCJ server host:" + ESCJ_SERVER_HOST);
				}
				if (config.getServletContext().getInitParameter("escj_port") != null){
					ESCJ_SERVER_PORT = Integer.parseInt(config.getServletContext().getInitParameter("escj_port"));
				}else{
					sharedLog.warn("Defaulting to ESCJ server port:" + ESCJ_SERVER_PORT);
				}
				
				sharedLog.info("Esc/Java 2 server:" + ESCJ_SERVER_HOST + ":" + ESCJ_SERVER_PORT);
				
				for (String name : PROJECT_NAMES){
					try {
						File projDir = new File(WORKSPACE_DIR, name);
						File projMetaDir = new File(projDir, "veriasa");

						sharedLog.info("Loading project " + name + " from " + projDir);
						
						ProjectDescriptor descriptor = ProjectDescriptor.create(
								name,
								WORKSPACE_DIR,
								new File(projMetaDir, name + DEF_SUFFIX), 
								new File(projMetaDir, name + CALL_GRAPH_SUFFIX), 
								new File(projMetaDir, name + BASE_SUFFIX), 
								new File(projMetaDir, name + INFERRED_SUFFIX),
								new File(projMetaDir, name + INTELLI_SUFFIX),
								new File(projMetaDir, name + DTRACE_SUFFIX)
						);
						
						projects.put(name, descriptor);
						
						logs.put(name, new VeriLog(LOG_DIR, name, LOG_SUFFIX));
					
						ProjectState state = Persistance.projectDataExists(descriptor)
								? Persistance.hydrateProject(descriptor)
								: new ProjectState(descriptor);
									
						addSharedProject(name, DEFAULT_SHARED_INSTANCE_KEY, state);
				
						for (TypeSpecification type : descriptor.getInferredSpec().getTypeSpecs()){
							parseAndCache(type.getInvariants());
							for (MethodContract method : type.getMethods()){
								parseAndCache(method.getRequires());
								parseAndCache(method.getEnsures());
								for (Collection<Clause> exsures : method.getExsures().values()){
									parseAndCache(exsures);
								}
							}
						}
						
						sharedLog.info("Loaded project " + name);
						
					} catch (Exception e) {
						sharedLog.fatal("Error loading project " + name,e);
					}
				}
				configOk = true;
			}
		}
	}

	/**
	 * Create an instance of that project.
	 * @param desired the desired project
	 * @return a new project instance
	 * @throws NoSuchProjectException the desired project is not available
	 */
	public static ProjectState createNewProject(String desired) throws NoSuchProjectException{
		if (projects.containsKey(desired)){
			return new ProjectState(projects.get(desired));
		}else{
			throw new NoSuchProjectException(desired);
		}
	}
	
	/**
	 * Choose a problem from a list of active problems, selecting a problem as follows:
	 * 
	 * 1) last problem worked on by user
	 * 2) problem preferred by user
	 * 3) problems not disliked by user
	 * 4) any problem
	 * 
	 * @param user the user
	 * @param enabled the available problems
	 * @return a problem for the user to solve
	 */
	private static ProblemGraphNode chooseProblem(final User user, Set<ProblemGraphNode> enabled){	
		ProjectState state = user.getProject();
		
		synchronized(state){
			//hack for fixed size set walkthrough
			String[] tutorialOrder = new String[] { "FixedSizeSet.FixedSizeSet", "FixedSizeSet.add", "FixedSizeSet.union", "FixedSizeSet.similar"};
			for (String method : tutorialOrder){
				for (ProblemGraphNode problem : enabled){
					if (((MethodProblem) problem.getProblem()).qualifiedSignature().contains(method)){
						user.startProblem(problem);
						return problem;
					}
				}
			}
			//end hack

			try{
				ProblemGraphNode last = find(enabled, GenericUtil.refEq(user.getLastActive()));
				user.startProblem(last);
				return last;
			}catch(NoSuchElementException ex){
				//OK, we need to pick one
			}
		
			for (int i = user.getPreferences().size() - 1; i >= 0; i--){
				try{
					ProblemGraphNode preferred = find(enabled, GenericUtil.refEq(user.getPreferences().get(i)));
					user.startProblem(preferred);
					return preferred;
				}catch(NoSuchElementException ex){
					//OK, keep looking
				}
			}
			
			Collection<ProblemGraphNode> notAvoided = Collections2.filter(enabled, new Predicate<ProblemGraphNode>(){
				@Override
				public boolean apply(ProblemGraphNode problem) {
					return !user.getAvoiding().contains(problem);
				}
			});
			
			if (!notAvoided.isEmpty()){
				user.startProblem(notAvoided.toArray(new ProblemGraphNode[]{})[(int) (Math.random() * notAvoided.size())]);
				return user.getActiveProblem();
			}else{
				user.startProblem(enabled.toArray(new ProblemGraphNode[]{})[(int) (Math.random() * enabled.size())]);
				return user.getActiveProblem();
			}
		}
	}
	
	@Override
	public MaybeProblem requestProblem() {
		final User user = getUserState();
	
		if (user.ownsProject()){
			ProjectState project = user.getProject();
			
			if (project.isSolved()){	
				return new MaybeProblem(user.getId(), new ProjectFinished());
			}else if(FORCE_PROBLEM != null){
				ProblemGraphNode forced = project.getProblems().find(SelectRequiresProblem.class, FORCE_PROBLEM);
				user.startProblem(forced);
				return new MaybeProblem(user.getId(), forced.getProblem());
			}else{
				Set<ProblemGraphNode> leaves = project.getProblems().getLeaves();
				return new MaybeProblem(user.getId(), chooseProblem(user, leaves).getProblem());
			}
		}else{
			String project = user.getDesiredProject();
			
			while (true){
				synchronized(sharedInstances){
					boolean allSolved = true;
					
					for (ProjectState state : sharedInstances.get(project, user.getSharedInstanceKey())){
						synchronized(state){
							if (!state.isSolved()){
								allSolved = false;
								List<ProblemGraphNode> leaves = newArrayList(state.getProblems().getLeaves());
								if (!leaves.isEmpty()){
									user.setProject(state);
									return new MaybeProblem(user.getId(), chooseProblem(user, Sets.newHashSet(leaves)).getProblem());
								}
							}
						}
					}
					
					// no available problems. why?
					
					if (allSolved){
						return new MaybeProblem(user.getId(), new ProjectFinished());
					}
				}
				safeSleep();
			}			
		}	
	}
	
	/**
	 * Sleeps thread for 300 milliseconds, ignoring {@link InterruptedException}
	 */
	private void safeSleep(){
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// doesn't matter
		}
	}
	
	@Override
	public MaybeProblem requestProblem(Solution solution) throws UnknownServerException {
		User user = getUserState();
		try {
			VeriRecord.recordSolution(user, solution);
		} catch (EscJResponseException e) {
			throw new UnknownServerException(e);
		}
		
		// Save the user's state and the project state
		try {
			Persistance.record(user);
		} catch (IOException e) {
			user.log(Level.ERROR, "Error recording user state", e);
		}
		
		try {
			Persistance.record(user, user.getProject());
		} catch (IOException e) {
			user.log(Level.ERROR, "Error recording project state", e);
		}
		
		if (!user.ownsProject()){
			try {
				Persistance.record(user.getProject());
			} catch (IOException e) {
				user.log(Level.ERROR, "Error recording shared project state", e);
			}
		}
	
		return requestProblem();
	}
	
	protected static ProjectResult getResult(ProjectSpecification project) throws EscJResponseException{
		VerificationRequest request = new VerificationRequest(project);
		
		ProjectResult result = resultCache.get(request);
			
		if (result != null){
			sharedLog.log(Level.DEBUG, "CACHE HIT");
			return result;
		}
		
		try {
			EscJClient client = new EscJClient(ESCJ_SERVER_HOST, ESCJ_SERVER_PORT);
			result = client.tryProjectSpec(request);
		} catch (Exception e) {
			throw new EscJResponseException(e);
		} 
		resultCache.put(request, result);
		return result;
	}

	@Override
	public List<Feedback> requestFeedbacks(List<Update> update) throws FeedbackException {
		User user = getUserState();
		
		FeedbackWorker[] threads = new FeedbackWorker[update.size()];
		Feedback[] results = new Feedback[update.size()];
		Update[] work = update.toArray(new Update[]{});
		
		for (int i=0 ; i < work.length; i++){
			threads[i] = new FeedbackWorker(work[i],user);
			threads[i].start();
		}
		
		boolean allDone = true;
		do{
			allDone = true;
			for (int i = 0 ; i < work.length; i++){
				if (!threads[i].done){
					allDone = false;
					break;
				}
			}
			if (!allDone){
				 safeSleep();
			}
		}while(!allDone);
		
		for (int i=0 ; i < work.length; i++){	
			if (threads[i].error != null){
				throw threads[i].error;
			}else{
				results[i] = threads[i].result; 
			}
		}
		
		return newArrayList(results);
	}

	/**
	 * A thread that retrieves feedback for an update
	 * @author Todd Schiller
	 */
	private class FeedbackWorker extends Thread{
		private final Update update;
		private final User user;
		
		private FeedbackException error = null;
		private Feedback result = null;
		private Boolean done = false;
		
		public FeedbackWorker(Update update, User user) {
			super();
			this.update = update;
			this.user = user;
		}

		@Override
		public void run() {
			try {
				result = requestFeedback(update, user);
			} catch (FeedbackException e) {
				error = e;
			}
			done = true;
		}
	}
	
	public Feedback requestFeedback(Update update, User user) throws FeedbackException{
		ProblemGraphNode problem = user.getActiveProblem();
		ProjectDescriptor descriptor = user.getProject().getDescriptor();
		
		try{
			if (update instanceof WriteEnsuresUpdate){
				return VeriFeedback.ensuresFeedback((WriteEnsuresUpdate) update, (WriteEnsuresProblem) problem.getProblem(), user);
			}else if(update instanceof WriteExsuresUpdate){
				return VeriFeedback.exsuresFeedback((WriteExsuresUpdate) update, (WriteExsuresProblem) problem.getProblem(), user);
			}else if (update instanceof WriteRequiresUpdate){
				return VeriFeedback.writeRequiresFeedback((WriteRequiresUpdate) update, (WriteRequiresProblem) problem.getProblem(), descriptor, user);
			}else if (update instanceof SelectRequiresUpdate){
				return VeriFeedback.selRequiresFeedback((SelectRequiresUpdate) update, (SelectRequiresProblem) problem.getProblem(), descriptor, user);
			}else{
				throw new FeedbackException("Unknown update type");
			}
		}catch (Exception e){
			user.log(Level.ERROR, "Error generating feedback", e);
			throw new FeedbackException(e);
		}
	}
		
	@Override
	public Feedback requestFeedback(Update update) throws FeedbackException{
		User user = getUserState();
		return requestFeedback(update, user);
	}
	
	@Override
	public String requestWarning(int id) {
		return Util.idToWarning.get(id);
	}
	
	@Override
	public Documentation requestDoc(int id) {
		return requestDoc(id, null);
	}
	
	@Override
	public Documentation requestDoc(int id, Integer warningId) {
		User state = getUserState();
		
		String htmlDoc = Util.idToDoc.get(id);
		String signature = Util.idToSig.get(id);
		
		MethodProblem active = (MethodProblem) state.getActiveProblem().getProblem();
		
		if (signature != null 
			&& !signature.contains("Check.")  // NOT "check" client method
			&& !(Util.anyCtorSignature(active.getFunction().qualifiedSignature()) && new Util.SameSignature(signature).apply(active))) // NOT active constructor
		{
			MethodContract method = Iterables.find(Util.allMethods(state.getProject().getActiveSpec()), new Util.SameSignature(signature));
			ProblemGraphNode problem = state.getActiveProblem();
			return new MethodDocumentation(signature, htmlDoc, method.getRequires(), method.getEnsures(), method.getExsures(),
							warningId == null ? new HashSet<Clause>() : getBadRequires(warningId, signature, method.getRequires()),
							getBadEnsures(problem, signature),
							getBadExsures(problem, signature));
		}else{
			return new Documentation(htmlDoc);
		}
	}
	
	public static Set<Clause> getBadRequires(int warningId, String methodSignature, Collection<Clause> all){
		HashSet<Clause> result = Sets.newHashSet();
		
		if (Util.idToWarningChunks.containsKey(warningId)){
			Set<Chunk> warnings = Util.idToWarningChunks.get(warningId);
		
			for (Chunk warning : warnings){
				if (warning.getMessage().contains("not established")){
					String fp = warning.getBadLine().substring(0, warning.getBadLineOffset());
					
					String qualifiedName = methodSignature.substring(0, methodSignature.lastIndexOf("("));//qualified name of method to retrieve doc for
					String simpleName = qualifiedName.substring(qualifiedName.lastIndexOf(".") >= 0 ? qualifiedName.lastIndexOf(".") + 1: 0);//simple name of method to retrieve doc for
					
					if (fp.endsWith(simpleName)){
						String cc = warning.getAssociatedDeclaration().getContents().replace((String) "//@requires", "").trim().replaceAll("\\s", "");
						//remove trailing ;
						cc = cc.substring(0, cc.length()-1);
						for (Clause spec : all){
							if (cc.equals(spec.getClause().trim().replaceAll("\\s", ""))){
								result.add(spec);
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the known bad ensures statements for subproblems for <code>method</code> that <code>node</code>
	 * depends on
	 * @param node the problem
	 * @param method the query method
	 * @return the bad ensures statements from <code>method</code> that <code>node</code> depends on
	 */
	public Set<Clause> getBadEnsures(ProblemGraphNode node, String method){
		Set<Clause> result = Sets.newHashSet();
		for (ProblemGraphNode dependency : node.getDependsOn()){
			MethodProblem problem = (MethodProblem) dependency.getProblem();
			if (problem.getFunction().getSignature().equals(method) && problem instanceof WriteEnsuresProblem){
				Collection<Clause> bad = Collections2.filter(((WriteEnsuresProblem) problem).getKnown(), SpecUtil.ACCEPT_BAD);
				result.addAll(bad);
			}
		}
		return result;
	}

	/**
	 * Returns the known bad exsures statements for subproblems for <code>method</code> that <code>node</code>
	 * depends on
	 * @param node the problem
	 * @param method the query method
	 * @return the bad ensures statements from <code>method</code> that <code>node</code> depends on
	 */
	public HashMap<String,Set<Clause>> getBadExsures(ProblemGraphNode node, String method){
		Set<Clause> acc = Sets.newHashSet();
		
		for (ProblemGraphNode dependency : node.getDependsOn()){
			MethodProblem problem = (MethodProblem) dependency.getProblem();
			if (problem.getFunction().getSignature().equals(method) && problem instanceof WriteExsuresProblem){
				Collection<Clause> bad = Collections2.filter(((WriteExsuresProblem) problem).getKnown(), SpecUtil.ACCEPT_BAD);
				acc.addAll(bad);
			}
		}
		
		//TODO: Update this to support other exception names
		HashMap<String,Set<Clause>> result = Maps.newHashMap();
		result.put("RuntimeException", acc);
		return result;
	}
	
	@Override
	public List<String> requestSignatures() {
		if (!hasSession()){
			return newArrayList();
		}
		
		User state = getUserState();
		ProblemGraphNode active = state.getActiveProblem();
		
		if (active == null || !(active.getProblem() instanceof MethodProblem)){
			return newArrayList(state.getProject().getDescriptor().getSignatures());
		}else{
			String method = ((MethodProblem) active.getProblem()).getFunction().getSignature();
			
			List<CallGraphNode> callees = state.getProject().getDescriptor().getCallGraph().getCallees(method);
			
			Iterable<String> calleeSignatures = Iterables.transform(callees, new Function<CallGraphNode, String>(){
				@Override
				public String apply(CallGraphNode node) {
					return node.getQualifiedMethodName();
				}
			});
			
			return newArrayList(Sets.newHashSet(calleeSignatures));
		}
	}

	
	private Predicate<UserMessageThread> hasReason(final Reason r){
		return new Predicate<UserMessageThread>(){
			@Override
			public boolean apply(UserMessageThread thread) {
				UserMessage first = thread.getFirst();
				
				return (first instanceof ImpossibleMessage) 
					&& ((ImpossibleMessage) first).getReason().equals(r);
			}
		};
	}
	
	@Override
	public List<UserMessageThread> requestMessages() {
		User user = getUserState();
		
		synchronized(user.getProject()){
			MethodProblem problem = (MethodProblem) user.getActiveProblem().getProblem();
			
			List<UserMessageThread> all = user.getProject().getMessages(problem);

			if (problem instanceof SelectRequiresProblem){
				return newArrayList(filter(all, hasReason(Reason.STRONG_REQ)));
			}else if (problem instanceof WriteRequiresProblem){
				return newArrayList(filter(all, hasReason(Reason.NOT_LISTED)));
			}else if (problem instanceof WriteEnsuresProblem){
				return newArrayList(filter(all, hasReason(Reason.WEAK_ENS)));
			}else if (problem instanceof WriteExsuresProblem){
				return newArrayList(filter(all, hasReason(Reason.WEAK_EXS)));
			}else{
				return newArrayList();
			}
		}
	}

	@Override
	public boolean submitVote(final UserMessageThread thread, final UserMessage message, Vote vote, UserMessage response) {
		User user = getUserState();
		
		VeriServiceImpl.tryLog(new LogEntry(
				new VoteAction(user, user.getActiveProblem().getProblem(), message, vote),
				user.getProject()));
		
		String method = ((MethodProblem) user.getActiveProblem().getProblem()).getFunction().getSignature();
		
		user.log(Level.INFO, "VOTE " + vote.toString() + " " + method + " " + message.toString());
		
		synchronized(user.getProject()){
			// find the local message thread
			UserMessageThread localThread = find(user.getProject().getMessages(method), new Predicate<UserMessageThread>(){
				@Override
				public boolean apply(UserMessageThread otherThread) {
					return Util.messageContentEq.compare(otherThread.getFirst(), thread.getFirst()) == 0;
				}
			});
			
			// find the local message
			UserMessage localMessage = find(localThread.getMessages(), new Predicate<UserMessage>(){
				@Override
				public boolean apply(UserMessage otherMessage) {
					return Util.messageContentEq.compare(otherMessage, message) == 0;
				}
			});
			
			//TODO: will this work, or do we have to search for the message on the server side?
			localMessage.setVote(vote);
			
			if (response != null){
				localThread.add(response);
			}
			
		}
		return true;
	}
	
	@Override
	public boolean stayAlive() {
		if (!hasSession()){
			return false;
		}
		User user = getUserState();
		user.markAlive();
		return true;
	}
	
	@Override
	public List<String> requestParams() {
		MethodProblem method = (MethodProblem) getUserState().getActiveProblem().getProblem();
		return method.getFunction().getParameters();
	}
	
	@Override
	public List<InvElement> requestLocals() {
		User user = getUserState();
		MethodProblem method = (MethodProblem) getUserState().getActiveProblem().getProblem();
		TypeSpecification type = Util.findType(user.getProject().getActiveSpec(), method.getFunction());
		
		if (type == null){
			throw new RuntimeException("Type not found");
		}
	
		return newLinkedList(requestFields("this"));
	}
	
	@Override
	public List<InvElement> requestFields(String expr) {
		if (expr.indexOf("this.") >= 0){
			expr = expr.substring(expr.indexOf("this.") + "this.".length());
		}
		
		User user = getUserState();
		MethodProblem method = (MethodProblem) getUserState().getActiveProblem().getProblem();
		
		TypeSpecification type = Util.findType(user.getProject().getActiveSpec(), method.getFunction());
		IntelliNode typeNode = user.getProject().getDescriptor().getIntelliMap().getIntelliNode(type.getFullyQualifiedName());
		
		List<InvElement> result = newLinkedList();
		
		for (IntelliEdge child : typeNode.getChildren().keySet()){
			if (child instanceof FieldEdge){
				FieldEdge field = (FieldEdge) child;
				result.add(new InvLocal("this."+ field.getName(), RefType.Expression));
				if (field.isArray()){
					result.add(new InvElement(new InvRef[] {
							new InvRef(new InvLocal("this." + field.getName(), RefType.BoilerPlate)),
							new InvRef(new InvFixed("[", RefType.BoilerPlate)),
							new InvRef(RefType.Expression),
							new InvRef(new InvFixed("]", RefType.BoilerPlate)),
					}, RefType.Expression));
					
					result.add(new InvElement(new InvRef[] {
							new InvRef(new InvLocal("this." + field.getName() + ".length", RefType.BoilerPlate)),
					}, RefType.Expression));
				}
			}else if(child instanceof MethodEdge){
				MethodEdge eMethod = (MethodEdge) child;
				MethodContract cCallee = Util.lookupMethod(type, eMethod.getName()); 
				
				if (!cCallee.getSignature().equals(method.qualifiedSignature()) // not the same method
					&& Iterables.any(cCallee.getEnsures(), SpecUtil.ACCEPT_GOOD) // has known post-conditions
					&& cCallee.getExsures().isEmpty()){ // no checked exceptions
				
					if (eMethod.getName().startsWith("is") || eMethod.getName().startsWith("get")){
						result.add(new InvLocal("this." + eMethod.getName() + "()", RefType.Expression));
					}	
				}
			}
		}
		
		//TODO: fix this hack for FixedSizeSet
		
		if (type.getFullyQualifiedName().equals("FixedSizeSet")){
			
			if (method.getFunction().getParameters().contains("other")){
				result.add(new InvElement(new InvRef[] {
						new InvRef(new InvLocal("other.bits", RefType.BoilerPlate)),
				}, RefType.Expression));
				result.add(new InvElement(new InvRef[] {
						new InvRef(new InvLocal("other.bits.length", RefType.BoilerPlate)),
				}, RefType.Expression));
				result.add(new InvElement(new InvRef[] {
						new InvRef(new InvLocal("other.bits", RefType.BoilerPlate)),
						new InvRef(new InvFixed("[", RefType.BoilerPlate)),
						new InvRef(RefType.Expression),
						new InvRef(new InvFixed("]", RefType.BoilerPlate)),
				}, RefType.Expression));
			}
			
			if (method.getFunction().getParameters().contains("digits")){
				result.add(new InvElement(new InvRef[] {
						new InvRef(new InvLocal("digits", RefType.BoilerPlate)),
						new InvRef(new InvFixed("[", RefType.BoilerPlate)),
						new InvRef(RefType.Expression),
						new InvRef(new InvFixed("]", RefType.BoilerPlate)),
				}, RefType.Expression));
				
				result.add(new InvElement(new InvRef[] {
						new InvRef(new InvLocal("digits.length", RefType.BoilerPlate)),
				}, RefType.Expression));
			}
		}
	
		return result;
	}

	@Override
	public InvElement stringToElt(String statement) throws JmlParseException {
		String clean = SpecUtil.clean(statement);
		
		if (eltCache.containsKey(clean)){
			return eltCache.get(clean).duplicate();
		}else{
			InvElement elt = JmlDndParser.specToFragment(clean);
			eltCache.put(clean, elt);
			return elt;
		}
	}

	@Override
	public JmlSpan specToSpan(String statement) throws JmlParseException{
		String clean = SpecUtil.clean(statement);
		if (spanCache.containsKey(clean)){
			return spanCache.get(clean);
		}else{
			JmlSpan fragment = Util.parseSpan(clean);
			spanCache.put(clean, fragment);
			return fragment;
		}
	}
	
	private void parseAndCache(Collection<Clause> jmlStatements){
		for (Clause statement : jmlStatements){
			String clean = SpecUtil.clean(statement.getClause());
			
			if (!eltCache.containsKey(clean)){
				try {
					eltCache.put(clean, JmlDndParser.specToFragment(clean));
				} catch (JmlParseException e) {
					sharedLog.warn("Failed to cache " + clean, e);
				}
			}
			
			if (!spanCache.containsKey(clean)){
				try {
					spanCache.put(clean, Util.parseSpan(clean));
				} catch (JmlParseException e) {
					sharedLog.warn("Failed to cache " + clean, e);
				}
			}	
		}
	}
	
	@Override
	public String requestAssignmentId() {
		return getUserState().getAssignmentId();
	}

	@Override
	public boolean writeModeChanged(WriteMode mode) {
		User user = getUserState();
		LogEntry entry = new LogEntry(new SwitchModeAction(user, mode), user.getProject());
		VeriServiceImpl.tryLog(entry);
		return true;
	}
}
