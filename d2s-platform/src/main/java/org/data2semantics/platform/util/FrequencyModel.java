package org.data2semantics.platform.util;

import java.io.*;
import java.util.*;

import org.data2semantics.platform.Global;

import static java.util.Collections.*;

public class FrequencyModel<T> 
{

	protected Map<T, Double> frequencies = new LinkedHashMap<T, Double>();
	protected double total = 0;
	
	// * A sorted list of the tokens in this model
	protected List<T> sorted = null;
	// * The mod count the last time the sorted list was recomputed
	private long modsAtLastSort = -1;
	
	private long mods = 0;
	
	public FrequencyModel()
	{
	}
	
	/**
	 * Constructs an independent copy of a given BasicFrequencyModel
	 */	
	public FrequencyModel(FrequencyModel<T> model)
	{
		this(model, model.tokens());		
	}		
	
	/**
	 * Constructs an independent copy of a given FrequencyModel, containing a 
	 * given set of tokens.
	 */
	public FrequencyModel(FrequencyModel<T> model, Collection<T> tokens)
	{
		this();
		for(T token : tokens)
			add(token, model.frequency(token));
	}	
	
	public FrequencyModel(Collection<T> corpus)
	{
		add(corpus);
	}
	
	public void add(Collection<T> corpus)
	{
		for(T token : corpus)
			add(token);
	}
	
	public void add(T token)
	{
		add(token, 1.0);
	}
	
	public void add(T token, double weight)
	{
		mods++;
		
		if(frequencies.containsKey(token))
			frequencies.put(token, frequencies.get(token) + weight);
		else
			frequencies.put(token, weight);
		
		total += weight;
	}
	
	public double distinct()
	{
		return frequencies.keySet().size();
	}

	public double frequency(T token)
	{
		if(! frequencies.containsKey(token))
			return 0.0;
		
		return frequencies.get(token);
	}

	public double total()
	{
		return total;
	}
	
	/**
	 * All the tokens encountered so far. The set returned is unmodifiable, and
	 * is backed by the model. 
	 * 
	 * @return A set of all tokens encountered so far.
	 */
	public Set<T> tokens()
	{
		return Collections.unmodifiableSet(frequencies.keySet());
	}
	
	/**
	 * All the tokens encountered so far, sorted by frequency. The list
	 * is unmodifiable and backed by the model.
	 * 
	 * The list is recomputed when necessary. This means that successive calls to
	 * this method will only be computationally expensive if the model is 
	 * modified in between.
	 * 
	 * The list is reverse-sorted by frequency, so that the highest frequency 
	 * token has the lowest index (and vice versa). 
	 * 
	 * @return A list of all tokens encountered so far.
	 */
	public List<T> sorted()
	{
		if(modsAtLastSort != mods || sorted == null)
		{
			sorted = new ArrayList<T>(tokens());
			Collections.sort(sorted,
				Collections.reverseOrder(new FrequencyModel.Comparator<T>(this)));
			sorted = Collections.unmodifiableList(sorted);
		}
		
		return sorted;
	}	
	
	/**
	 * Returns the tokens with the highest probability
	 * 
	 * @return
	 */
	public T maxToken()
	{
		
		double maxValue = Double.MIN_VALUE;
		T maxToken = null;
		for(T key : frequencies.keySet())
		{
			double prob = probability(key);
			if(prob > maxValue)
			{
				maxToken = key;
				maxValue = prob;
			}
		}
		
		return maxToken;
	}

	/**
	 * Returns a random token from this model, according to its probabilities.
	 * @return
	 */
	public T choose()
	{
		// * select random op
		double draw = Global.random().nextDouble();
		double total = 0.0;
		
		int op = 0;
		
		T choice = null;
		for(T token : frequencies.keySet())
		{
			choice = token;
			
			total += probability(token);
			if(total > draw)
				break;
		}
		
		return choice;
	}
	
	/**
	 * Chooses a number of elements, without replacement
	 * 
	 * The returned set is distinct form this model, and can be freely edited.
	 * 
	 * FIXME: Naive implementation, only works well with small num
	 * 
	 * @param num
	 * @return
	 */
	public Set<T> chooseWithoutReplacement(int num)
	{	
		if(num < 0)
			throw new IllegalArgumentException("Input ("+num+") cannot be negative.");		
		if(num > distinct())
			throw new IllegalArgumentException("Input ("+num+") must be smaller than number of distinct elements in model ("+distinct()+").");
		
		HashSet<T> elements = new HashSet<T>();
		while(elements.size() < num)
		{
			T candidate = choose();
			if(! elements.contains(candidate))
				elements.add(candidate);
		}
		
		return elements;
	}
	
	/**
	 * Calculates the entropy
	 * 
	 * @return A non-negative finite value representing the entropy of the
	 * probability model.
	 */
	public double entropy()
	{
		double sum = 0.0;
		for(T token : tokens())
		{
			double p = probability(token);
			if(p != 0.0)
				sum += p * (Math.log(p) / L2);
		}
		
		return - sum;	
	}
	
	private static final double L2 = Math.log(2.0);
	
	/**
	 * Prints an extensive multiline summary of the model to an outputstream
	 * 
	 * @param out The printstream to print to. Use {@link System.out} for
	 *            printing to the console.
	 */
	public void print(PrintStream out)
	{
		out.printf("total:    %.0f \n", total());
		out.printf("distinct: %.0f \n", distinct());
		out.printf("entropy:  %.3f \n", entropy()); 
		out.println("tokens: ");		
		
		// * Create a list of key, sorted by probability/frequency 
		List<T> keys = new ArrayList<T>(tokens());
		Collections.sort(keys, reverseOrder(new FrequencyModel.Comparator<T>(this)));
		
		for(T key : keys)
			out.println("  " + key + ", " + frequency(key));
	}
	
	/**
	 * Prints out a single line string with all the relative frequencies in the model.
	 * @return
	 */
	public String toStringLong()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
			
		List<T> keyList = new ArrayList<T>(frequencies.keySet());
		
		if(! keyList.isEmpty())
			if(keyList.get(0) instanceof Comparable<?>)
			{
				List sortedKeyList = keyList;
				Collections.sort(sortedKeyList);
				keyList = sortedKeyList;				
			}
		
		for(T key : keyList)
		{
			if(sb.length() != 1)
				sb.append(", ");
			sb.append(key + ":");
			sb.append(String.format("%.2f", probability(key)));			
		}
		sb.append(']');		
		
		return sb.toString();
	}
	
	public void cut() {
	}
	
	public long state() {
		return mods;
	}
	
	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		buff.append('[');
		for(T key : frequencies.keySet())
			buff.append(buff.length() == 1? "":", ").append(key + ":" + frequencies.get(key));
		
		buff.append(']');		
		
		return buff.toString();
	}
	
	public double probability(T token) {
		return frequency(token)/total();
	}

	public double logProbability(T token) {
		return Math.log(probability(token));
	}
	
	public static class Comparator<T> implements java.util.Comparator<T>
	{
		private FrequencyModel<T> model;
		
		public Comparator(FrequencyModel<T> model)
		{
			this.model = model;
		}
		
		@Override
		public int compare(T first, T second) {
			return Double.compare(model.frequency(first), model.frequency(second));
		}
	}	
}
