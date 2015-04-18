package com.schiller.veriasa.experiment;

import java.io.File;

public abstract class Common {

	public static final File RESULTS_DIR = new File("/home/tws/projects/asa/veriweb-paper/study/results");
	public static final File VWORKER_RESULTS_DIR = new File(RESULTS_DIR,"vworker");

	public static final File VWORKER_ECLIPSE_RESULTS = new File(VWORKER_RESULTS_DIR,"eclipse");
	public static final File VWORKER_VERIWEB_RESULTS = new File(VWORKER_RESULTS_DIR,"veriweb");
}
