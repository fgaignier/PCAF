package solvers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import generators.ControllabilityEncoder;
import generators.HardRootCompletionGenerator;
import generators.RandomCAFRootCompletionGenerator;
import model.ArgumentFramework;
import model.CArgument;
import model.ControlAF;
import model.SupportingPowerRecorder;
import util.Util;
import model.StableControlConfiguration;
import model.StableExtension;

/**
 * Use of Monte Carlo simulation to calculate 
 * the control configurations of a CAF
 * You can give a fixed number of simulations (not recommended)
 * or give an error level of the confidence interval
 * HERE WE USE A HEURISTIC (so it should be faster than Monte_Carlo_CAF_Solver):
 * 1) the first completion is not random, but given by HardRootCompletionGenerator
 * 2) for the other random completions, we do not calculate the control configurations, 
 * but only that check the one we got from the first completion are working (if not, not kept) 
 * @author Fabrice
 *
 */
public class Monte_Carlo_CAF_Solver_Heuristic {
	public static int INIT_CP = -1;
	public static int NO_CC = -2;

	private ControlAF CAF;
	private RandomCAFRootCompletionGenerator generator; 
	private double controllingPower;
	private Map<StableControlConfiguration, SupportingPowerRecorder> recorders;
	private int total_simulations;

	public Monte_Carlo_CAF_Solver_Heuristic(ControlAF CAF) {
		this.CAF = CAF;
		this.generator = new RandomCAFRootCompletionGenerator(this.CAF);
		this.controllingPower = INIT_CP;
		this.recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();
		this.total_simulations = 0;
	}

	public int getNumberSimu() {
		return this.total_simulations;
	}

	public double getControllingPower() {
		return this.controllingPower;
	}

	public Map<StableControlConfiguration, SupportingPowerRecorder> getSupportingPowerRecorders() {
		return this.recorders;
	}

	/**
	 * returns the set of credulous control configurations
	 * with fixed number of simulations
	 * @param N
	 * @return
	 */
	public Set<StableControlConfiguration> getCredulousControlConfigurations(int N) {
		return this.getControlConfigurations(N, ControllabilityEncoder.CREDULOUS);
	}

	/**
	 * returns the set of skeptical control configurations
	 * with fixed number of simulations
	 * @param N
	 * @return
	 */
	public Set<StableControlConfiguration> getSkepticalControlConfigurations(int N) {
		return this.getControlConfigurations(N, ControllabilityEncoder.SKEPTICAL);
	}

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
	 * get a hard root completion for each target element and calculates the cc for each.
	 * we keep only the intersection of the results.
	 *  this is the starting set before runing monte_carlo simulation
	 * @return
	 */
	private Set<StableControlConfiguration> getStartingSet(int type) {
		Set<StableControlConfiguration> result = null;
		HardRootCompletionGenerator hard_generator = new HardRootCompletionGenerator(this.CAF);
		Set<CArgument> target = CAF.getTarget();
		Map<StableControlConfiguration, Set<StableExtension>> solutions = null;

		for(CArgument t : target) {
			ArgumentFramework af = hard_generator.getHardestRootCompletionWRT(t);
			CSP_Completion_Solver solver = new CSP_Completion_Solver(this.CAF, af);
			if(type == ControllabilityEncoder.CREDULOUS) {
				solutions = solver.getCredulousControlConfigurations();
			} else {
				solutions = solver.getSkepticalControlConfigurations();
			}
			if(result == null) {
				result = solutions.keySet();
			} else {
				result = util.Util.intersect(result, solutions.keySet());
			}
		}
	
		return result;
	}

	/**
	 * returns the control configurations if they exist
	 * use of a CSP_Completion_Solver to get Credulous/Skeptical Set<StableControlConfiguration>
	 * stored in a Map
	 * @param N, number of simulations
	 * @param type, ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	 * @return
	 */
	private Set<StableControlConfiguration> getControlConfigurations(int N, int type) {
		this.controllingPower = -1;
		// first getting the starting set of StableControlConfigurations
		Set<StableControlConfiguration> cc_list = this.getStartingSet(type);
		for(int i = 0; i<N; i++) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();

			CSP_Completion_Verifier checker = new CSP_Completion_Verifier(this.CAF, af);

			Set<StableControlConfiguration> temp = new HashSet<StableControlConfiguration>();
			for(StableControlConfiguration cc : cc_list) {
				if(type == ControllabilityEncoder.CREDULOUS) {
					if(checker.isCredulousControlConfigurations(cc)) {
						//System.out.println("cc : " + cc.toString() + " is working");
						temp.add(cc);
					}
				} else {
					if(checker.isSkepticalControlConfigurations(cc)) {
						temp.add(cc);
					}
				}
			}
			cc_list = temp;
			if(cc_list.isEmpty()) {
				this.controllingPower = NO_CC;
				this.total_simulations = i;
				break;
			}

		}
		if(cc_list.isEmpty()) {
			return null;
		} else {
			this.controllingPower = 1;
			this.total_simulations = N;
			return cc_list;
		}
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

		this.controllingPower = -1;
		int N = Util.MINIMUM_SIMULATION;
		int current_simu = 0;
		// first getting the starting set of StableControlConfigurations
		Set<StableControlConfiguration> cc_list = this.getStartingSet(type);

		while(current_simu < N || current_simu < util.Util.MINIMUM_SIMULATION) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();
			CSP_Completion_Verifier checker = new CSP_Completion_Verifier(this.CAF, af);
			Set<StableControlConfiguration> temp = new HashSet<StableControlConfiguration>();
			for(StableControlConfiguration cc : cc_list) {
				if(type == ControllabilityEncoder.CREDULOUS) {
					if(checker.isCredulousControlConfigurations(cc)) {
						//System.out.println("cc : " + cc.toString() + " is working");
						temp.add(cc);
					}
				} else {
					if(checker.isSkepticalControlConfigurations(cc)) {
						temp.add(cc);
					}
				}
			}
			cc_list = temp;
			// increase the number of simulations
			current_simu++;
			if(cc_list.isEmpty()) {
				this.controllingPower = NO_CC;
				this.total_simulations = current_simu;
				break;
			}
			N = (int)util.Util.getNewSimulationNumber(current_simu, current_simu, error);
		}
		if(cc_list.isEmpty()) {
			return null;
		} else {
			this.controllingPower = 1;
			this.total_simulations = current_simu;
			return cc_list;
		}
	}

}
