package com.schiller.veriasa.logexplore;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import com.schiller.veriasa.web.server.escj.EscJClient;
import com.schiller.veriasa.web.shared.core.ProjectSpecification;
import com.schiller.veriasa.web.shared.escj.ProjectResult;
import com.schiller.veriasa.web.shared.escj.VerificationRequest;

public class FetchResult implements Callable<ProjectResult> {

	Logger log = Logger.getLogger(FetchResult.class);
	
	private static final String ESCJ_SERVER_HOST = "127.0.0.1";
	private static final Integer ESCJ_SERVER_PORT = 4444;
	private ProjectSpecification spec = null;
	
	public FetchResult(ProjectSpecification spec) {
		super();
		this.spec = spec;
	}

	@Override
	public ProjectResult call() throws Exception {
		EscJClient client = null;
		try {
			client = new EscJClient(ESCJ_SERVER_HOST,ESCJ_SERVER_PORT);
		} catch (Exception e) {
			log.error("Error creating client",e);
			throw new ExecutionException(e);
		}
		try {
			log.info("Fetching result");
			return client.tryProjectSpec(new VerificationRequest(spec));
		} catch (Exception e) {
			log.error("Error fetching result",e);
			throw new ExecutionException(e);
		} 
	}
}
