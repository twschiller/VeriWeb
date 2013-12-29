package com.veriasa.speceditor.server;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.veriasa.speceditor.client.GreetingService;
import com.veriasa.speceditor.shared.FieldVerifier;
import com.veriasa.speceditor.shared.FunctionDoc;
import com.veriasa.speceditor.shared.FunctionInfo;
import com.veriasa.speceditor.shared.FunctionSig;
import com.veriasa.speceditor.shared.ParamDoc;
import com.veriasa.speceditor.shared.SpecProblem;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
		GreetingService {

	public final static String PROBLEM_FILE = "C:\\Users\\tws\\Documents\\fundmp.xml";
	public final static String CCHECK_FILE = "C:\\Users\\tws\\Documents\\ccout.xml";
	
	public static List<FunctionDoc> docTable;
	
	public String greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		userAgent = escapeHtml(userAgent);

		return "Hello, " + input + "!<br><br>I am running " + serverInfo
				+ ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	@Override
	public FunctionDoc requestDoc(int id) {
		return docTable.get(id);
	}

	private static FunctionDoc CreateDoc(String docXml){

		String summary = null;
		String remarks = null;
		List<ParamDoc>  params = new ArrayList<ParamDoc>();
		String returnValue = null;
		 DocumentBuilderFactory dbf =
	            DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(docXml));
			
			Document doc = db.parse(is);
			NodeList s = doc.getElementsByTagName("summary");
			NodeList r= doc.getElementsByTagName("returns");
			NodeList ps = doc.getElementsByTagName("param");
			
			if (s.getLength() > 0){
				summary = ((Element) s.item(0)).getTextContent();
			}
			if (r.getLength() > 0){
				returnValue = ((Element) r.item(0)).getTextContent();
			}
			for (int i = 0; i < ps.getLength(); i++){
				String name = ((Element) ps.item(i)).getAttribute("name");
				String desc = ((Element) ps.item(i)).getTextContent();
				params.add(new ParamDoc(name,desc));
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return new FunctionDoc(summary,remarks,params,returnValue);
	
	}
	
	enum CONTRACT_TYPE { PRE, POST, INVARIANT};
	
	public Map<String,Set<String>> getCcInferred(CONTRACT_TYPE t){
		String query;
		if (t.equals(CONTRACT_TYPE.POST)){
			query = "Contract.Ensures(";
		}else if(t.equals(CONTRACT_TYPE.PRE)){
			query = "Contract.Requires(";
		}else{
			throw new IllegalArgumentException();
		}
		
		
		Map<String,Set<String>>  ps = new HashMap<String,Set<String>>();
		
		File file = new File(CCHECK_FILE);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document doc = null;
		
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(file);	
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		NodeList ms = doc.getElementsByTagName("Method");
		
		for (int i = 0; i < ms.getLength(); i++){
			Node n =  ms.item(i);
			String name = n.getAttributes().getNamedItem("Name").getNodeValue();
			
			Set<String> ss = new HashSet<String>();
			
			NodeList cs = n.getChildNodes();
			
			for (int j =0; j < cs.getLength(); j++)
			{
				String v = cs.item(j).getTextContent();
				if (!v.trim().isEmpty() && v.contains(query)){
					ss.add(v.substring(v.indexOf(query) + query.length(), v.lastIndexOf(")")));
				}
			}
			ps.put(name,ss);
		}
		return ps;
		
	}
	
	
	private static List<FunctionDoc> readDocTable(File file){
		Document doc = readXmlDoc(file);
		NodeList nodeLst = doc.getElementsByTagName("docentry");
		List<FunctionDoc> docTable = new ArrayList<FunctionDoc>();
		
		for(int i = 0; i < nodeLst.getLength(); i++){
			
			docTable.add(CreateDoc( "<root>" + ((CDATASection) nodeLst.item(i).getFirstChild()).getData() + "</root>"));
		}
		return docTable;
		
	}
	
	private static Document readXmlDoc(File f)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document doc = null;
		
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(f);	
			doc.getDocumentElement().normalize();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return doc;
	}
	
	@Override
	public SpecProblem requestProblem() {
		Map<String,Set<String>> ps = getCcInferred(CONTRACT_TYPE.POST);
		Map<String,Set<String>> rs = getCcInferred(CONTRACT_TYPE.PRE);
		
		File file = new File(PROBLEM_FILE);
		docTable = readDocTable(file);
		Document doc = readXmlDoc(file);
	
		
		NodeList nodeLst = doc.getElementsByTagName("function");
		
		List<SpecProblem> fs = new ArrayList<SpecProblem>();
		
		for(int i = 0; i < nodeLst.getLength(); i++){
			Element e = (Element) nodeLst.item(i);
			String name = e.getElementsByTagName("name").item(0).getFirstChild().getTextContent();
			name += "()";
			
			String body = ((CDATASection) e.getElementsByTagName("body").item(0).getFirstChild()).getData();
			
			FunctionDoc d = CreateDoc( ((CDATASection) e.getElementsByTagName("doc").item(0).getFirstChild()).getData());
			
			FunctionSig s = new FunctionSig(FunctionSig.textFromBody(body),null,null,null,null);
			
			FunctionInfo ii = new FunctionInfo(s,d, body );
			SpecProblem sp = new SpecProblem(ii,
					rs.containsKey(name) ? new ArrayList<String>(rs.get(name)) : new ArrayList<String>(),
					ps.containsKey(name) ? new ArrayList<String>(ps.get(name)) : new ArrayList<String>());
			
			//TODO: all functions can be problems
			if (rs.containsKey(name) || ps.containsKey(name) ){
				fs.add(sp);
			}
		}
		return fs.get((int) (Math.random() * fs.size()));
	}
}
 