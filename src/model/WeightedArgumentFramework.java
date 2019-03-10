package model;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;

public class WeightedArgumentFramework extends ArgumentFramework {
	public static Double original_flat = new Double(1);
	public static double precision = 0.001;
	public static int max_steps = 1000;
	
	protected Map<Argument, Double> weights; 

	/*
	 * flat WAF => all weights are set to 1
	 */
	public WeightedArgumentFramework() {
		super();
		initFlatOriginalWeights();
	}
	
	/*
	 * Weighted AF => need to know the weights
	 */
	public WeightedArgumentFramework(Map<Argument, Double> weights) {
		super();
		this.weights = weights;
	}
	
	public WeightedArgumentFramework(ArgumentFramework af) {
		super();
		this.weights = new HashMap<Argument, Double>();
		this.addAllArguments(af.getAllArguments());
		this.addAllAttacks(af.getAllAttacks());
	}
	
	/*
	 * clone a WAF (never duplicates the arguments)
	 */
	public WeightedArgumentFramework clone() {
		WeightedArgumentFramework result = new WeightedArgumentFramework();
		Set<Argument> args = this.getAllArguments();
		Iterator<Argument> iter = args.iterator();
		System.out.println("begin cloning");
		while(iter.hasNext()) {
			Argument current = iter.next();
			Double weight = this.getWeight(current);
			System.out.println("adding argument " + current.getName() + " with weight " + weight.toString());
			result.addArgument(current, weight);
		}
		result.addAllAttacks(super.getAllAttacks());
		return result;
	}
	
	private void initFlatOriginalWeights() {
		this.weights = new HashMap<Argument, Double>();
		Iterator<Argument> iter = this.getAllArguments().iterator();
		while(iter.hasNext()) {
			Argument current = iter.next();
			weights.put(current, original_flat);
		}
	}
	
	/*
	 * if we add an argument without weight, its weight is set to 1
	 */
	public void addArgument(Argument a) {
		super.addArgument(a);
		this.addWeight(a, original_flat);
	}
	
	/*
	 * every time we add an argument, we must store its original weight
	 */
	public void addArgument(Argument a, Double weight) {
		super.addArgument(a);
		this.addWeight(a, weight);
	}
	
	/*
	 * (non-Javadoc)
	 * @see model.ArgumentFramework#removeArgument(model.Argument)
	 */
	public void removeArgument(Argument a) {
		super.removeArgument(a);
		this.removeWeight(a);
	}
	
	private void addWeight(Argument a, Double weight) {
		weights.put(a, weight);
	}
	
	private void removeWeight(Argument a) {
		weights.remove(a);
	}
	
	public void addAllArguments(Set<Argument> args) {
		Iterator<Argument> iter = args.iterator();
		while(iter.hasNext()) {
			this.addArgument(iter.next());
		}
	}
	
	/*
	 * returns the original weight for an argument
	 */
	public Double getWeight(Argument a) {
		return this.weights.get(a);
	}
	
	/*
	 * returns a map argument, value with the strength of each argument according to hsb scemantic
	 */
	public Map<Argument, Double> h_categorizer() {
		return this.h_categorizer(null);
	}

	/*
	 * returns a map argument, value with the strength of each argument according to hsb scemantic
	 * according to an existing strength valuation => update only (presumably faster than full calculation for a small update
	 * in the af)
	 */
	public Map<Argument, Double> h_categorizer(Map<Argument, Double> strength) {
		int iterations = 0;
		double error = 1.0;
		Map<Argument, Double> result = null;
		if(strength == null) {
			result = new HashMap<Argument, Double>();
		} else {
			result = new HashMap<Argument, Double>(strength);
		}
		result.putAll(this.weights);
		Collection<Argument> argsSorted = this.getAllArgumentsSorted(ArgumentFramework.BY_RECEIVED_ATT);
		while(iterations < max_steps && error >= precision) {
			error = calculateStrength(result, argsSorted);
			iterations ++;
		}
		return result;
	}

	/*
	 * We evaluate the node according to the number of attackers it has
	 * The less attackers, the sooner we calculate its strength
	 */
	private  double calculateStrength(Map<Argument, Double> current, Collection<Argument> argsSorted) {
		double error = 0;
//		System.out.println(argsSorted.size());
		Iterator<Argument> iter = argsSorted.iterator();
		while(iter.hasNext()) {
			Argument arg = iter.next();
			Double actualStrength = current.get(arg);
			Double newStrength = calculateIndivStrength(arg, current);
//			System.out.println("calculating strength for argument: " + arg.getName() + " updating from " + actualStrength.toString() + " to " + newStrength.toString());
			double delta = java.lang.Math.abs(actualStrength.doubleValue() - newStrength.doubleValue()); 
			if( delta > error) {
				error = delta; 
			}
			current.remove(arg);
			current.put(arg,  newStrength);
		}
		return error;
	}
	
	private Double calculateIndivStrength(Argument a, Map<Argument, Double> current) {
		Double wa = this.getWeight(a);
		Double result = wa.doubleValue();
		double others = 0;
		Set<Argument> attackers = this.getAttackingArguments(a);
		Iterator<Argument> iter = attackers.iterator();
		while(iter.hasNext()) {
			Argument att = iter.next();
			others = others + current.get(att).doubleValue();
		}
		result = result / (1 + others);
		
		return result;
	}
	
	
}
