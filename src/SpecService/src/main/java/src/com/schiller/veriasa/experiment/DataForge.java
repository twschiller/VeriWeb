package com.schiller.veriasa.experiment;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DataForge<T>{
	
	private HashMap<String, HashMap<Double,DataPoint<T>>> data = Maps.newHashMap();
	
	/**
	 * function used to determine the x-value for a {@link DataPoint}
	 */
	private final Function<DataPoint<T>, Double> extractKey;
	
	/**
	 * column header ordering
	 */
	private final Comparator<String> headerOrder;
	
	/**
	 * x-values for the <code>merged</code> table.
	 */
	private Double [] xs;
	
	/**
	 * data series headers for the <code>merged</code> table
	 */
	private String [] yHeaders;
	
	/**
	 * the data in a tabular format
	 */
	private T [][] table;
	
	/**
	 * true iff all of the data has been transferred to the table
	 */
	private boolean merged = false;
	
	/**
	 * @param extractKey function used to determine the x-value for a {@link DataPoint}
	 * @param headerOrder column ordering
	 */
	public DataForge(Function<DataPoint<T>, Double> extractKey, Comparator<String> headerOrder){
		this.extractKey = extractKey;
		this.headerOrder = headerOrder;
	}
	
	/**
	 * Add data series <code>header</code> to the set of data
	 * @param header the name of the data series
	 * @param data the data
	 */
	public void add(String header, List<DataPoint<T>> data ){
		HashMap<Double, DataPoint<T>> xx = Maps.newHashMap();
		for ( DataPoint<T> x : data){
			xx.put(extractKey.apply(x), x);
		}
		this.data.put(header, xx);
		
		merged = false;
	}
	
	/**
	 * 
	 * @param ps output stream
	 * @param interpolate true iff the data should be interpolated
	 * @param transform function to transform the raw data
	 */
	public void csv(PrintStream ps, boolean interpolate, Function<T, String> transform){
		if (!merged){
			merge(interpolate);
		}
		
		double minX = xs[0];
		double maxX = xs[xs.length-1];
		csv(ps, interpolate, transform, minX, maxX);
	}
	
	public void csv(PrintStream ps, boolean interpolate, Function<T, String> transfrom, double minX, double maxX){
		if (!merged){
			merge(interpolate);
		}
		
		ps.println("Time," + Joiner.on(",").join(yHeaders));
		
		for (int r = 0; r < xs.length; r++){
			if (xs[r] >= minX && xs[r] <= maxX){
				ps.println(xs[r] + "," + Joiner.on(",").join(Iterables.transform(Arrays.asList(table[r]), transfrom)));
			}
		}
	}
	
	/**
	 * Perform a left-value interpolation
	 */
	private void interpolate(){
		T last = null;
		
		for (int c = 0; c < yHeaders.length; c++){
			for (int r = 0; r < xs.length; r++){
				if (table[r][c] == null){
					table[r][c] = last;
				}
				
				last = table[r][c];		
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void merge(boolean interpolate){
		Set<Double> xs = Sets.newHashSet();
		for (HashMap<Double,DataPoint<T>> x : data.values()){
			xs.addAll(x.keySet());
		}
		
		List<Double> all = Lists.newArrayList(xs);
		Collections.sort(all);
		this.xs = all.toArray(new Double[] {});
		
		List<String> cols = Lists.newArrayList(data.keySet());
		Collections.sort(cols, headerOrder);
		this.yHeaders = cols.toArray(new String[] {});
		
		table = (T[][]) new Object[xs.size()][data.keySet().size()];
		
		for (int r = 0; r < this.xs.length; r++){
			for (int c = 0; c < this.yHeaders.length; c++){
				DataPoint<T> t = data.get(this.yHeaders[c]).get(this.xs[r]);
				
				table[r][c] = t == null ? null : t.getValue();
			}
		}
		
		if (interpolate){
			this.interpolate();
		}
		
		merged = true;
	}
}
