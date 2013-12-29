package com.schiller.veriasa.experiment;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Data for a given point in time
 * @author Todd Schiller
 * @param <T> the type of data
 */
public class DataPoint<T> {

	private final long x;
	private final T value;
	private final Double pay;
	
	/**
	 * Create a data point for <code>timestamp</code>
	 * @param timestamp the time
	 * @param value the value
	 * @param pay hourly pay of the worker
	 */
	public DataPoint(long x, T value, Double pay) {
		this.x = x;
		this.value = value;
		this.pay = pay;
	}
	
	/**
	 * Create a data point for <code>timestamp</code> with no hourly pay
	 * information
	 * @param timestamp
	 * @param value
	 */
	public DataPoint(long timestamp, T value) {
		this(timestamp,value, null);
	}
	
	/**
	 * @return the timestamp
	 */
	public long getX() {
		return x;
	}
	
	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}
	
	/**
	 * @return the pay
	 */
	public Double getPay() {
		return pay;
	}
	
	@Override
	public String toString() {
		return x + ", " + value.toString();
	}
	
	public static <V> List<DataPoint<V>> elapsed(List<DataPoint<V>> xs){
		final long base = xs.get(0).getX();
		return Lists.newArrayList(Iterables.transform(xs, new Function<DataPoint<V>,DataPoint<V>>(){
			@Override
			public DataPoint<V> apply(DataPoint<V> arg0) {
				return new DataPoint<V>(arg0.getX() - base,arg0.getValue());
			}
		}));
	}
	
	public static <V> List<DataPoint<V>> scaleTime(List<DataPoint<V>> xs, final double factor){
		return Lists.newArrayList(Iterables.transform(xs, new Function<DataPoint<V>,DataPoint<V>>(){
			@Override
			public DataPoint<V> apply(DataPoint<V> arg0) {
				return new DataPoint<V>((long)(arg0.getX() * factor),arg0.getValue());
			}
		}));
		
	}
	
	public static <V> List<DataPoint<V>> collapseBreaks(List<DataPoint<V>> xs, long thresholdInMillis, long measurePeriodInMillis){
		ArrayList<DataPoint<V>> whole = Lists.newArrayListWithCapacity(xs.size());
		
		whole.add(xs.get(0));
		
		long acc = 0;
		
		for (int i = 1; i < xs.size(); i++){
			DataPoint<V> p = xs.get(i);
			
			long pause = Math.max(p.getX() - xs.get(i-1).x, measurePeriodInMillis);
			
			if (pause > thresholdInMillis){
				acc += (pause - measurePeriodInMillis); 
			}
			
			whole.add(new DataPoint<V>(p.getX() - acc, p.value));
		}
		return whole;
	}
	
}
