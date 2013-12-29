package com.schiller.veriasa.logexplore;


import static com.google.common.base.Predicates.and;
import static com.schiller.veriasa.util.LogUtil.before;
import static com.schiller.veriasa.util.LogUtil.forUser;
import static com.schiller.veriasa.web.shared.core.MethodSpecBuilder.builder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.schiller.veriasa.daikon.DaikonAdapter;
import com.schiller.veriasa.daikon.DaikonTypeSet;
import com.schiller.veriasa.executejml.CollectDataProcessor;
import com.schiller.veriasa.logexplore.action.ProblemActionView;
import com.schiller.veriasa.logexplore.action.ProjectActionView;
import com.schiller.veriasa.logexplore.action.SessionActionView;
import com.schiller.veriasa.logexplore.action.SwitchModeActionView;
import com.schiller.veriasa.logexplore.action.TrySpecsView;
import com.schiller.veriasa.logexplore.action.UserMessageView;
import com.schiller.veriasa.logexplore.views.InformationBar;
import com.schiller.veriasa.logexplore.views.ResultView;
import com.schiller.veriasa.logexplore.views.SourceView;
import com.schiller.veriasa.view.SpecTreePanel;
import com.schiller.veriasa.web.server.SpecUtil;
import com.schiller.veriasa.web.server.Util;
import com.schiller.veriasa.web.server.escj.AnnotateFile;
import com.schiller.veriasa.web.server.logging.LogEntry;
import com.schiller.veriasa.web.server.logging.MessageAction;
import com.schiller.veriasa.web.server.logging.ProblemAction;
import com.schiller.veriasa.web.server.logging.ProjectAction;
import com.schiller.veriasa.web.server.logging.SessionAction;
import com.schiller.veriasa.web.server.logging.SwitchModeAction;
import com.schiller.veriasa.web.server.logging.TrySpecsAction;
import com.schiller.veriasa.web.server.logging.UserAction;
import com.schiller.veriasa.web.shared.core.MethodContract;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.core.Clause;
import com.schiller.veriasa.web.shared.core.TypeSpecification;
import com.schiller.veriasa.web.shared.escj.AnnotatedFile;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.logging.LogAction;
import com.schiller.veriasa.web.shared.problems.MethodProblem;
import com.schiller.veriasa.web.shared.problems.SelectRequiresProblem;
import com.schiller.veriasa.web.shared.problems.WriteEnsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteExsuresProblem;
import com.schiller.veriasa.web.shared.problems.WriteRequiresProblem;

import daikon.FileIO;
import daikon.PptMap;

@SuppressWarnings("serial")
public class MainApp extends JFrame {

	private static final Logger log =  Logger.getLogger("LogViewer");
	
	private final ExecutorService pool = Executors.newFixedThreadPool(10);
	
	private static File WORKSPACE_DIR = new File(System.getProperty("user.home"),"/asa/projs");

	private Map<String,Map<String, DaikonTypeSet>> daikon = Maps.newHashMap();
	
	private final JPanel sPanel = new JPanel(new BorderLayout());
	private final JSplitPane topPanel;
	private final JSplitPane mainPanel;
	
	private final boolean REQUIRE_ID = true;
	
	private List<LogEntry> entries;
	private final HashMap<LogEntry,ProjectSpecification> mm;
	private final SpecTreePanel specTree= new SpecTreePanel();
	private final InformationBar explore = new InformationBar();
	
	private  ProjectSpecification activeSpec = null;
	
	private final JTabbedPane tabbedPane = new JTabbedPane();
	
	private final JMenuBar menuBar = new JMenuBar();
	
	/**
	 * scrollable source code view
	 */
	private final JScrollPane sourceScroll = new JScrollPane();
	
	/**
	 * scrollable result
	 */
	private final JScrollPane resultScroll = new JScrollPane();
	
	public MainApp(ArrayList<LogEntry> entries, HashMap<LogEntry,ProjectSpecification> mm){
		this.setTitle("VeriWeb Log Viewer");
		
		this.setResizable(true);
		this.setSize(1000, 600);

		JMenu menu = new JMenu("File");
		JMenuItem menuItem = new JMenuItem("Save Specification",KeyEvent.VK_S);
		menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveCurrent();
			}
		});
		menu.add(menuItem);
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
		
		setLayout(new BorderLayout());

		this.entries = Lists.newArrayList(entries);
		this.mm = mm;
		
		tabbedPane.add("Entry",new JPanel());
		tabbedPane.add("Report",resultScroll);
		tabbedPane.add("Source",sourceScroll);
	
		topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,  tabbedPane,  new JScrollPane(specTree));
		topPanel.setDividerLocation(450);
	
		mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel,sPanel);
			
		add(explore,BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	
		mainPanel.setDividerLocation(375);

		updateSelectorPanel();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	interface ResultCallback{
		void onResult(ProjectResult r);
	}
	
	private void saveCurrent(){
		final JFileChooser fc = new JFileChooser();
		
		if (activeSpec == null){
			JOptionPane.showMessageDialog(this, "No active specification");
			return;
		}
		
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
			 File file = fc.getSelectedFile();
			 
			try {
				OutputStream oos = new FileOutputStream(file);
				ObjectOutputStream out = new ObjectOutputStream(oos);
				out.writeObject(Util.filterSpecs(activeSpec,SpecUtil.ACCEPT_GOOD));
				out.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error saving specification");
			}
			 
		}
	}
	
	
	
	private void updateSelectorPanel(){
		final ActionTable actions =  new ActionTable(entries, new ActionTable.SelectionHook(){
			@Override
			public void select(LogEntry entry) {
				showEntry(entry, entries);
			}
		});
		
		UserTable utable = new UserTable(new UserTable.SelectHook(){
			@Override
			public void select(final String user) {
				Collection<LogEntry> f = Collections2.filter(entries, forUser(user));
				actions.update(Lists.newArrayList(f));
			}
		});
		utable.populate(entries,REQUIRE_ID );
		
		sPanel.setMaximumSize(new Dimension(1500,200));
		
		sPanel.removeAll();
		
		JScrollPane userScroll = new JScrollPane(utable);
		JScrollPane actionScroll = new JScrollPane(actions);
		
		sPanel.add(userScroll, BorderLayout.WEST);
		sPanel.add(actionScroll, BorderLayout.CENTER);
		validate();
	}
	

	
	private MethodContract findMethodSpec(ProjectSpecification proj, String sig){
		for (TypeSpecification type : proj.getTypeSpecs()){
			for (MethodContract fun : type.getMethods()){
				if (fun.getSignature().equals(sig)){
					return fun;
				}
			}
		}
		return null;
	}
	
	private List<Clause> addPendingSpecs(List<Clause> base, Collection<Clause> toAdd){
		List<Clause> n = new ArrayList<Clause>(base);
		
		HashSet<String> old = new HashSet<String>(Lists.transform(base, SpecUtil.INV));
		
		for (Clause s: toAdd){
			if (!old.contains(s.getClause())){
				n.add(new Clause(s.getClause(), s.getProvenance(), Clause.Status.PENDING, s.getReason()));
			}
		}
		
		return n;
	}
	
	
	
	private ProjectSpecification addDaikon(ProjectSpecification base) throws IOException{
		if (!daikon.containsKey(base.getName())){
			File f = new File(new File(new File(WORKSPACE_DIR, base.getName()), "veriasa"), base.getName() + ".inf");
			
			daikon.put(base.getName(), DaikonAdapter.parseDaikonFile(f));
		}
		
		File f = new File(new File(new File(WORKSPACE_DIR, base.getName()), "veriasa"), base.getName() + ".dtrace");
		CollectDataProcessor processor = new CollectDataProcessor();
		PptMap ppts = new PptMap();
		FileIO.read_data_trace_files (Arrays.asList(f.getAbsolutePath()), ppts, processor, false);
		
		
		return DaikonAdapter.populateProject(daikon.get(base.getName()), base, processor.samples);
	}
		
	
	private ProjectSpecification addTried(ProjectSpecification base, List<LogEntry> entries, Predicate<LogEntry> filter ){
		
		ProjectSpecification up = base;

		for (LogEntry e : entries){
			if (filter.apply(e)){
				LogAction a = e.getAction();
				
				if (a instanceof TrySpecsAction){
					List<Clause> ss = ((TrySpecsAction) a).getSpecs();
					
					MethodProblem p = (MethodProblem) ((TrySpecsAction) a).getProblem();
					
					MethodContract fs = findMethodSpec(up, p.getFunction().getSignature());
					
					if (p instanceof SelectRequiresProblem || p instanceof WriteRequiresProblem){
						up = Util.modifySpec(up, builder(fs).setRequires(addPendingSpecs(fs.getRequires(), ss)).getSpec());		
					}else if (p instanceof WriteEnsuresProblem){
						up = Util.modifySpec(up, builder(fs).setEnsures(addPendingSpecs(fs.getEnsures(), ss)).getSpec());
					}else if (p instanceof WriteExsuresProblem){
						Map<String,List<Clause>> exs = Maps.newHashMap(fs.getExsures());	
						exs.put("RuntimeException", addPendingSpecs(fs.getExsures().get("RuntimeException"), ss));
						up = Util.modifySpec(up, builder(fs).setExsures(exs).getSpec());
					}else{
						throw new RuntimeException("unknown problem");
					}	
				}
			}
		}
		
		return up;
	}

	
	private synchronized void updateResult(ProjectResult r){
		if (r != null){				
			if (r.hasFatalProjError()){
				resultScroll.setViewportView(new JPanel());
				tabbedPane.setTitleAt(1, "FATAL ESC/Java2 Error");
			}else{
				ResultView v = new ResultView(r);
				resultScroll.setViewportView(v);
				tabbedPane.setTitleAt(1, v.getNumErrors() + " errors");
			}
		}else{
			resultScroll.setViewportView(new JPanel());
			tabbedPane.setTitleAt(1, "No ESC/Java2 Report");
		}
	}
	private synchronized void setEntry(Component c){
		tabbedPane.setComponentAt(0, c);
		tabbedPane.setTitleAt(0, "Entry");
	}
	
	private void showSource(ProjectSpecification active){
		if (active != null){
			File f = new File(new File(WORKSPACE_DIR, active.getName()), active.getName() + ".java");
			try {
				AnnotatedFile af = AnnotateFile.annotateJavaFile(f, active, SpecUtil.ACCEPT_GOOD);
				sourceScroll.setViewportView(new SourceView(af));
			}catch (Exception e) {
				System.err.println("Error annotating file: " + e.getMessage());
				sourceScroll.setViewportView(new JPanel());
				throw new RuntimeException(e);
			
			}
		}else{
			sourceScroll.setViewportView(new JPanel());
		}
	
	}
	
	private synchronized void showKnownSpec(final LogEntry entry, List<LogEntry> all, ProjectSpecification active){
		try {
			active = addDaikon(active);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Error loading Daikon invariants:" + e1.getMessage());
		}
		
		if (entry.getAction() instanceof UserAction){
			ProjectSpecification more = addTried(active, all, 
					and(before(entry), forUser((UserAction) entry.getAction())));
				
			specTree.setSpec(more);
		}else{
			specTree.setSpec(active);
		}
		specTree.setBackground(Color.WHITE);
	}
	
	private synchronized void showEntry(final LogEntry entry, List<LogEntry> all){
			
		int old = topPanel.getDividerLocation();
	
		activeSpec = mm.get(entry);
		ProjectSpecification good = Util.filterSpecs(activeSpec, SpecUtil.ACCEPT_GOOD);
	
		final ListenableFutureTask<ProjectResult> future = new ListenableFutureTask<ProjectResult>(new FetchResult(good));
		future.addListener(new Runnable(){
			@Override
			public void run() {
				try {
					log.info("Updating results");
					updateResult(future.get());
				} catch (InterruptedException e) {
					log.error("Result thread interrupted",e);
				} catch (ExecutionException e) {
					log.error("Error fetching result",e);
				}
			}
		}, pool);
		pool.submit(future);
		
		showSource(activeSpec);
	
		showKnownSpec(entry, all, activeSpec);
	
		explore.populate(entry);
			
		topPanel.invalidate();

		if (entry.getAction() instanceof TrySpecsAction){
			setEntry(new TrySpecsView((TrySpecsAction) entry.getAction()));
		}else if(entry.getAction() instanceof MessageAction){
			setEntry(new UserMessageView((MessageAction) entry.getAction()));
		}else if(entry.getAction() instanceof ProblemAction){
			setEntry(new ProblemActionView((ProblemAction) entry.getAction()));
		}else if(entry.getAction() instanceof SessionAction){
			setEntry(new SessionActionView((SessionAction) entry.getAction()));
		}else if(entry.getAction() instanceof ProjectAction){
			setEntry(new ProjectActionView((ProjectAction) entry.getAction()));
		}else if(entry.getAction() instanceof SwitchModeAction){
			setEntry(new SwitchModeActionView((SwitchModeAction) entry.getAction()));
		}else{
			setEntry(new JPanel());
		}
	
		topPanel.setDividerLocation(old);
		//validate();
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		BasicConfigurator.configure();
		
		FileInputStream fis = new FileInputStream(args[0]);
		ObjectInputStream ois = new ObjectInputStream(fis);

		ArrayList<LogEntry> entries = Lists.newArrayList();
		LogEntry o;

		HashMap<LogEntry,ProjectSpecification> mm = Maps.newHashMap();

		try{
			while ((o = (LogEntry) ois.readObject()) != null){
				if (o instanceof LogEntry){
					LogEntry e = (LogEntry) o;
					ProjectSpecification s = (ProjectSpecification) ois.readObject();
					mm.put(e, s);
					entries.add(e);
				}
			}	
		}catch(EOFException e){
			log.warn("Unexpected end of file");
		}
		
		log.info("Loaded " + entries.size() + " entries");
		
		MainApp v = new MainApp(entries,mm);
		v.setVisible(true);
	}
	
}
