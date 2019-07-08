package solvers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import generators.ControllabilityEncoder;
import generators.RandomCAFRootCompletionGenerator;
import model.ArgumentFramework;
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
 * @author Fabrice
 *
 */
public class Monte_Carlo_CAF_Solver {
	public static int INIT_CP = -1;
	public static int NO_CC = -2;

	private ControlAF CAF;
	private RandomCAFRootCompletionGenerator generator; 
	private double controllingPower;
	private Map<StableControlConfiguration, SupportingPowerRecorder> recorders;
	private int total_simulations;

	public Monte_Carlo_CAF_Solver(ControlAF CAF) {
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
	 * returns the control configurations if they exist
	 * use of a CSP_Completion_Solver to get Credulous/Skeptical Set<StableControlConfiguration>
	 * stored in a Map
	 * @param N, number of simulations
	 * @param type, ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	 * @return
	 */
	private Set<StableControlConfiguration> getControlConfigurations(int N, int type) {
		Map<StableControlConfiguration, Integer> result = new HashMap<StableControlConfiguration, Integer>();
		Map<StableControlConfiguration, SupportingPowerRecorder> temp_recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();

		this.controllingPower = INIT_CP;
		for(int i = 0; i<N; i++) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();

			CSP_Completion_Solver solver = new CSP_Completion_Solver(this.CAF, af);
			Map<StableControlConfiguration, Set<StableExtension>> solutions = null;
			Set<StableControlConfiguration> cc_list = null;
			Set<StableExtension> stables = null;
			if(type == ControllabilityEncoder.CREDULOUS) {
				solutions = solver.getCredulousControlConfigurations();
			} else {
				solutions = solver.getSkepticalControlConfigurations();
			}
			cc_list = solutions.keySet();
			SupportingPowerRecorder recorder = null;
			for(StableControlConfiguration scc : cc_list) {
				stables = solutions.get(scc);
				StableControlConfiguration present = util.Util.find(result.keySet(), scc);
				if(present != null) {
					Integer count = result.get(present);
					Integer newVal = new Integer(count.intValue()+1);
					result.put(present, newVal);
					recorder = temp_recorders.get(present);
					//recorder.updateOccurencesList(stables);
				} else {
					result.put(scc, new Integer(1));
					recorder = new SupportingPowerRecorder();
					//recorder.updateOccurencesList(stables);
					temp_recorders.put(scc,  recorder);
				}
				if(type == ControllabilityEncoder.CREDULOUS) {
					recorder.updateOccurencesListCred(stables);
				} else {
					recorder.updateOccurencesListSke(stables);
				}
			}
			// here must check if we still have a control entity with controlling power of 1
			// if not we can stop the simulation at this point

			if(!this.hasPotentialControlEntity(result, i+1)) {
				//System.out.println(af.toString());
				this.controllingPower = NO_CC;
				this.total_simulations = i;
				break;
			}

		}

		if(this.controllingPower > NO_CC) {
			this.setControllingPower(result);
			this.total_simulations = N;
			Set<StableControlConfiguration> selection = this.takeMax(result, temp_recorders).keySet();
			//System.out.println("controlling power = " + this.controllingPower + "/" + N + "=");
			this.controllingPower = this.controllingPower/N;
			//System.out.println("=" + this.controllingPower);
			if(controllingPower < 1) {
				this.recorders = null;
				return null;
			} else {
				return selection;
			}
		} else {
			return null;
		}
	}


	/**
	 * isolate the most probable controlling entities from
	 * all found control entities
	 * @param imput
	 * @return
	 */
	private Map<StableControlConfiguration, Integer> takeMax(Map<StableControlConfiguration, Integer> imput, Map<StableControlConfiguration, SupportingPowerRecorder> temp_recorders) {
		Map<StableControlConfiguration, Integer> result = new HashMap<StableControlConfiguration, Integer>();
		this.recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();
		for(StableControlConfiguration scc : imput.keySet()) {
			Integer value = imput.get(scc);
			double val = (double)value.intValue();
			if(val == this.controllingPower) {
				result.put(scc, value);
				this.recorders.put(scc, temp_recorders.get(scc));
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
		//System.out.println("controlling power = " + this.controllingPower);
	}

	private boolean hasPotentialControlEntity(Map<StableControlConfiguration, Integer> imput, int nbSimu) {
		for(StableControlConfiguration scc : imput.keySet()) {
			int value = imput.get(scc).intValue();
			//System.out.println("value of cc=" + value);
			//System.out.println("nbSimu=" + nbSimu);
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
		Map<StableControlConfiguration, SupportingPowerRecorder> temp_recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();

		this.controllingPower = INIT_CP;
		double current_max = 0;
		int N = Util.MINIMUM_SIMULATION;
		int current_simu = 0;
		
		while(current_simu < N || current_simu < util.Util.MINIMUM_SIMULATION) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();

			CSP_Completion_Solver solver = new CSP_Completion_Solver(this.CAF, af);
			Map<StableControlConfiguration, Set<StableExtension>> solutions = null;
			Set<StableControlConfiguration> cc_list = null;
			Set<StableExtension> stables = null;
			if(type == ControllabilityEncoder.CREDULOUS) {
				solutions = solver.getCredulousControlConfigurations();
			} else {
				solutions = solver.getSkepticalControlConfigurations();
			}
			cc_list = solutions.keySet();
			SupportingPowerRecorder recorder = null;
			for(StableControlConfiguration scc : cc_list) {
				stables = solutions.get(scc);
				StableControlConfiguration present = util.Util.find(result.keySet(), scc);
				if(present != null) {
					Integer count = result.get(present);
					Integer newVal = new Integer(count.intValue()+1);
					result.put(present, newVal);
					recorder = temp_recorders.get(present);
					//recorder.updateOccurencesList(stables);
				} else {
					result.put(scc, new Integer(1));
					recorder = new SupportingPowerRecorder();
					//recorder.updateOccurencesList(stables);
					temp_recorders.put(scc,  recorder);
				}
				if(type == ControllabilityEncoder.CREDULOUS) {
					recorder.updateOccurencesListCred(stables);
				} else {
					recorder.updateOccurencesListSke(stables);
				}
			}
			// increase the number of simulations
			current_simu++;

			// here must check if we still have a control entity with controlling power of 1
			// if not we can stop the simulation at this point
			if(!this.hasPotentialControlEntity(result, current_simu)) {
				//System.out.println(af.toString());
				this.controllingPower = NO_CC;
				this.total_simulations = current_simu;
				break;
			}

			N = (int)util.Util.getNewSimulationNumber(current_max, current_simu, error);
		}

		if(this.controllingPower > NO_CC) {
			this.setControllingPower(result);
			this.total_simulations = current_simu;
			Set<StableControlConfiguration> selection = this.takeMax(result, temp_recorders).keySet();
			//System.out.println("controlling power = " + this.controllingPower + "/" + current_simu + "=");
			this.controllingPower = this.controllingPower/current_simu;
			//System.out.println("=" + this.controllingPower);
			if(controllingPower < 1) {
				this.recorders = null;
				return null;
			} else {
				return selection;
			}
		} else {
			return null;
		}
	}
}
