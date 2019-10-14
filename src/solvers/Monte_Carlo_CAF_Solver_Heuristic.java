package solvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import generators.ControllabilityEncoder;
import generators.RandomCAFRootCompletionGenerator;
import model.Argument;
import model.ArgumentFramework;
import model.CArgument;
import model.ControlAF;
import model.WeightedArgumentFramework;
import util.Util;
import model.StableControlConfiguration;
import model.StableExtension;

/**
 * Use of Monte Carlo simulation to calculate 
 * the control configurations of a CAF
 * You can give a fixed number of simulations (not recommended)
 * or give an error level of the confidence interval
 * @author Fabrice
 *
 */
public class Monte_Carlo_CAF_Solver_Heuristic {
	public static int INIT_CP = -1;
	public static int NO_CC = -2;

	private ControlAF CAF;
	private RandomCAFRootCompletionGenerator generator; 
	private double controllingPower;
	//private Map<StableControlConfiguration, SupportingPowerRecorder> recorders;
	private int total_simulations;

	public Monte_Carlo_CAF_Solver_Heuristic(ControlAF CAF) {
		this.CAF = CAF;
		this.generator = new RandomCAFRootCompletionGenerator(this.CAF);
		this.controllingPower = INIT_CP;
		//this.recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();
		this.total_simulations = 0;
	}

	public int getNumberSimu() {
		return this.total_simulations;
	}

	public double getControllingPower() {
		return this.controllingPower;
	}

	/*
	public Map<StableControlConfiguration, SupportingPowerRecorder> getSupportingPowerRecorders() {
		return this.recorders;
	}
	 */

	/**
	 * returns the set of credulous control configurations
	 * number of simulations is calculated according to error level in confidence interval
	 * @param error
	 * @return
	 */
	public Set<StableControlConfiguration> getCredulousControlConfigurations(double error) {
		return this.getControlConfigurations(error, ControllabilityEncoder.CREDULOUS);
	}

	/**
	 * returns the set of skeptical control configurations
	 * number of simulations is calculated according to error level in confidence interval
	 * @param error
	 * @return
	 */
	public Set<StableControlConfiguration> getSkepticalControlConfigurations(double error) {
		return this.getControlConfigurations(error, ControllabilityEncoder.SKEPTICAL);
	}


	/**
	 * isolate the most probable controlling entities from
	 * all found control entities
	 * @param imput
	 * @return
	 */
	private Map<StableControlConfiguration, Integer> takeMax(Map<StableControlConfiguration, Integer> imput) {
		Map<StableControlConfiguration, Integer> result = new HashMap<StableControlConfiguration, Integer>();
		for(StableControlConfiguration scc : imput.keySet()) {
			Integer value = imput.get(scc);
			double val = (double)value.intValue();
			if(val == this.controllingPower) {
				result.put(scc, value);
			}
		}
		return result;
	}

	/**
	 * Once done, controlling power is set
	 * @param imput
	 */
	private void setControllingPower(Map<StableControlConfiguration, Integer> imput) {		 
		for(StableControlConfiguration scc : imput.keySet()) {
			int value = imput.get(scc).intValue();
			if(value >= this.controllingPower) {
				this.controllingPower = (double)value;
			}
		}
	}

	private boolean hasPotentialControlEntity(Map<StableControlConfiguration, Integer> imput, int nbSimu) {
		for(StableControlConfiguration scc : imput.keySet()) {
			int value = imput.get(scc).intValue();
			if(nbSimu == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns the control configurations if they exist
	 * use of a CSP_Completion_Solver to get Credulous/Skeptical Set<StableControlConfiguration>
	 * stored in a Map
	 * @param error, width of confidence interval at 95%
	 * @param type, ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	 * @return
	 */
	private Set<StableControlConfiguration> getControlConfigurations(double error, int type) {
		Map<StableControlConfiguration, Integer> result = new HashMap<StableControlConfiguration, Integer>();

		// now we remember the previous root AF by their signature and the strength of the target
		Map<SortedSet<String>, Double> memory = new HashMap<SortedSet<String>, Double>();
		Map<SortedSet<String>, List<StableControlConfiguration>> memory_list = new HashMap<SortedSet<String>, List<StableControlConfiguration>>();

		// Target
		Set<CArgument> target = this.CAF.getTarget();

		this.controllingPower = INIT_CP;
		double current_max = 0;
		int N = Util.MINIMUM_SIMULATION;
		int current_simu = 0;

		while(current_simu < N || current_simu < util.Util.MINIMUM_SIMULATION) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();

			// retrieves the signature of the af
			SortedSet<String> signature = af.getSignature();
			// retrieves the list of stables control conf with the corresponding signature 
			List<StableControlConfiguration> mem_list = memory_list.get(signature);

			if(isHarder(memory, af, target)) {
				CSP_Completion_Solver solver = new CSP_Completion_Solver(this.CAF, af);
				Map<StableControlConfiguration, Set<StableExtension>> solutions = null;
				Set<StableControlConfiguration> cc_list = null;
				//Set<StableExtension> stables = null;
				if(type == ControllabilityEncoder.CREDULOUS) {
					solutions = solver.getCredulousControlConfigurations();
				} else {
					solutions = solver.getSkepticalControlConfigurations();
				}
				cc_list = solutions.keySet();
				// if there ever is a root AF with no control configuration
				// we can stop the search here
				if(cc_list.isEmpty()) {
					this.controllingPower = NO_CC;
					this.total_simulations = current_simu;
					break;
				}
				for(StableControlConfiguration scc : cc_list) {
					StableControlConfiguration present = util.Util.find(result.keySet(), scc);
					if(present != null) {
						Integer count = result.get(present);
						Integer newVal = new Integer(count.intValue()+1);
						result.put(present, newVal);
						if(newVal.intValue() > current_max) {
							current_max = count.intValue() +1;
						}
					} else {
						result.put(scc, new Integer(1));
						if(current_max <1) {
							current_max = 1;
						}
					}
					// updating the signature memory list (this is to increase the 
					// correct val(cc) when we find an af that with the same signature that is less
					// hard
					if(mem_list == null) {
						mem_list = new ArrayList<StableControlConfiguration>();
						memory_list.put(signature, mem_list);
					}
					mem_list.add(scc);
				}
			}
			// increase the number of simulations
			current_simu++;

			// here must check if we still have a control entity with controlling power of 1
			// if not we can stop the simulation at this point
			if(!this.hasPotentialControlEntity(result, current_simu)) {
				this.controllingPower = NO_CC;
				this.total_simulations = current_simu;
				break;
			}

			N = (int)util.Util.getNewSimulationNumber(current_max, current_simu, error);
		}

		if(this.controllingPower > NO_CC) {
			this.setControllingPower(result);
			this.total_simulations = current_simu;
			Set<StableControlConfiguration> selection = this.takeMax(result).keySet();
			//System.out.println("controlling power = " + this.controllingPower + "/" + current_simu + "=");
			this.controllingPower = this.controllingPower/current_simu;
			//System.out.println("=" + this.controllingPower);
			if(controllingPower < 1) {
				return null;
			} else {
				return selection;
			}
		} else {
			return null;
		}
	}

	/**
	 * calculate Sum(deg(t)) for t in target
	 */
	private Double getCumulStrength(ArgumentFramework af, Set<CArgument> target) {
		WeightedArgumentFramework waf = new WeightedArgumentFramework(af);
		Map<Argument, Double> strengths = waf.h_categorizer();
		double result = 0;
		for(Argument arg : target) {
			Double temp = strengths.get(arg);
			result = result + temp.doubleValue();
		}
		return new Double(result);
	}

	/**
	 * returns true if it is the best sum of deg(t) for a given signature
	 * returns false else
	 * In the mean time updates the memory if harder (return true)
	 */
	private boolean isHarder(Map<SortedSet<String>, Double> memory, ArgumentFramework af, Set<CArgument> target) {
		Double strength = this.getCumulStrength(af, target);
		SortedSet<String> signature = af.getSignature();
		Double prevStrength = memory.get(signature);
		if(prevStrength == null) {
			memory.put(signature, strength);
			return true;
		}
		if(strength.doubleValue() >= prevStrength.doubleValue()) {
			return false;
		} else {
			memory.put(signature,  strength);
		}
		return true;
	}
}
