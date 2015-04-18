package com.schiller.veriasa.experiment;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;

import com.google.common.collect.Maps;
import com.schiller.veriasa.distance.Distance.DistanceType;

public class ExperimentOptions {

	public final double timeScale;
	public final boolean interpolate;

	public final int measurePeriodMillis;
	
	public final boolean collapseBreaks;
	public final int breakThresholdMillis;
	
	public final DistanceType metric;
	
	public final HashMap<String,Double> payRate;
	
	public final File input;
	
	public ExperimentOptions(
			File input,
			double timeScale,
			DistanceType metric, 
			int measurePeriodMillis, 
			boolean interpolate,
			boolean collapseBreaks, 
			int breakThresholdMillis,
			HashMap<String,Double> payRate
		) {
		
		super();
		this.input = input;
		this.timeScale = timeScale;
		this.metric = metric;
		this.measurePeriodMillis = measurePeriodMillis;
		this.interpolate = interpolate;
		this.collapseBreaks = collapseBreaks;
		this.breakThresholdMillis = breakThresholdMillis;
		this.payRate = Maps.newHashMap(payRate); 
	}
	
	public static final double MILLIS_TO_MINUTES =  1./(60 * 1000);
	
	static HashMap<String,Double> PAY_RATE = Maps.newHashMap();
	static{
		
		PAY_RATE.put("kbains", 6.);
		PAY_RATE.put("remer", 6.);
		//PAY_RATE.put("wenkroy", 6.);
		PAY_RATE.put("wenkroy2", 6.);
		PAY_RATE.put("mego", 6.21);
		PAY_RATE.put("experience", 6.21);
		PAY_RATE.put("rhacotis", 9.);
		PAY_RATE.put("vasile", 9.89);
		PAY_RATE.put("sai", 10.);
		PAY_RATE.put("danusoft", 11.);
		PAY_RATE.put("kodiakns", 11.);
		PAY_RATE.put("fabian", 11.);
		PAY_RATE.put("alexey007", 13.);
		PAY_RATE.put("cristi", 16.);
		PAY_RATE.put("techsoft", 16.48);
		//PAY_RATE.put("hardcodedstuff", 17.);
		PAY_RATE.put("gissoft", 17.);
		PAY_RATE.put("virtualorg", 19.78);
		PAY_RATE.put("madept", 22.);
	}
	
	/**
	 * Sort names by worker pay
	 */
	public static final Comparator<String> WORKER_PAY = new Comparator<String>(){
		@Override
		public int compare(String lhs, String rhs) {
			return PAY_RATE.get(lhs).compareTo(PAY_RATE.get(rhs));
		}
	};
	
	public static final ExperimentOptions VWORKER_ECLIPSE = new ExperimentOptions(
			Common.VWORKER_ECLIPSE_RESULTS,  
			MILLIS_TO_MINUTES,
			DistanceType.ECLIPSE,
			(2 * 60 * 1000),
			true,//interpolate
			true,//collapse breaks
			(15 * 60 * 1000),
			PAY_RATE
	);
	public static final ExperimentOptions VWORKER_VERIWEB = new ExperimentOptions(
			Common.VWORKER_VERIWEB_RESULTS,  
			MILLIS_TO_MINUTES,
			DistanceType.VERIWEB,
			(1 * 60 * 1000),
			true,
			true,
			(3 * 60 * 1000),
			PAY_RATE
	);
}
