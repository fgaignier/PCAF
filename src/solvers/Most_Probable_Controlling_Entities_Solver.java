package solvers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import generators.ControllabilityEncoder;
import generators.RandomPCAFRootCompletionGenerator;
import model.ArgumentFramework;
import model.PControlAF;
import model.StableControlConfiguration;
import model.StableExtension;
import model.SupportingPowerRecorder;
import util.Util;

/**
 * Use of Monte Carlo simulation to calculate 
 * the most probable controlling entities (together with their controlling power)
 * Two ways: Indicate the number of simulations you want
 * or the error (width) of the 95% confidence interval 
 * @author Fabrice
 *
 */
public class Most_Probable_Controlling_Entities_Solver implements I_Monte_Carlo_Solver {
	private PControlAF PCAF;
	private RandomPCAFRootCompletionGenerator generator; 
	private double controllingPower;
	private int total_simulations;
	private Map<StableControlConfiguration, SupportingPowerRecorder> recorders;
	private double min_interval;
	private double max_interval;
	private int solver_type;
	
	public Most_Probable_Controlling_Entities_Solver(PControlAF PCAF, int solver_type) {
		this.PCAF = PCAF;
		this.generator = new RandomPCAFRootCompletionGenerator(this.PCAF);
		this.controllingPower = -1;
		this.total_simulations = 0;
		this.recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();
		this.min_interval = 0;
		this.max_interval = 0;
		this.solver_type = solver_type;
	}
	
	public int getNumberSimu() {
		return this.total_simulations;
	}
	
	public double getControllingPower() {
		return this.controllingPower;
	}
	
	public double getLowInterval() {
		return this.min_interval;
	}
	
	public double getHighInterval() {
		return this.max_interval;
	}
	
	public Map<StableControlConfiguration, SupportingPowerRecorder> getSupportingPowerRecorders() {
		return this.recorders;
	}
	
	public Set<StableControlConfiguration> getCredulousControlConfigurations(int N) {
		return this.getMostProbableControllingEntities(N, ControllabilityEncoder.CREDULOUS);
	}
	
	public Set<StableControlConfiguration> getSkepticalControlConfigurations(int N) {
		return this.getMostProbableControllingEntities(N, ControllabilityEncoder.SKEPTICAL);
	}

	public Set<StableControlConfiguration> getCredulousControlConfigurations(double error) {
		return this.getMostProbableControllingEntities(ControllabilityEncoder.CREDULOUS, error);
	}
	
	public Set<StableControlConfiguration> getSkepticalControlConfigurations(double error) {
		return this.getMostProbableControllingEntities(ControllabilityEncoder.SKEPTICAL, error);
	}

	/**
	 * returns the most probable controlling entities
	 * using monte carlo simulation (N simulations).
	 * use of a CSP_Completion_Solver to get Credulous/Skeptical Set<StableControlConfiguration>
	 * stored in a Map
	 * @param N, number of simulations
	 * @param type, ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	 * @return
	 */
	 private Set<StableControlConfiguration> getMostProbableControllingEntities(int N, int type) {
		Map<StableControlConfiguration, Integer> result = new HashMap<StableControlConfiguration, Integer>();
		Map<StableControlConfiguration, SupportingPowerRecorder> temp_recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();
		
		this.controllingPower = -1;
		
		for(int i = 0; i<N; i++) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();
			
			I_Completion_Solver solver = null;
			if(this.solver_type == I_Monte_Carlo_Solver.CSP_SOLVER) {
				solver = new CSP_Completion_Solver(this.PCAF, af);
			} else {
				solver = new SAT_Completion_Solver(this.PCAF, af);
			}
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
				} else {
					result.put(scc, new Integer(1));
					recorder = new SupportingPowerRecorder();
					temp_recorders.put(scc,  recorder);
				}
				recorder.updateOccurencesList(stables, af);
				/*
				if(type == ControllabilityEncoder.CREDULOUS) {
					recorder.updateOccurencesListCred(stables);
				} else {
					recorder.updateOccurencesListSke(stables, af);
				} */
			}
		}
		this.setControllingPower(result);
		this.total_simulations = N;
		Set<StableControlConfiguration> selection = this.takeMax(result, temp_recorders).keySet();
		this.controllingPower = this.controllingPower/N;
		this.min_interval = this.controllingPower - this.getConfidenceInterval(N);
		this.max_interval = this.controllingPower + this.getConfidenceInterval(N);
		return selection;
	}
	 
	 private double getConfidenceInterval(int nbSimu) {
		 double value = Util.CONFIDENCE_INT;
		 double temp = this.controllingPower*(1-this.controllingPower)/nbSimu;
		 value = value*Math.sqrt(temp);
		 return value;
	 }
	 
	 
	 /**
	  * returns the most probable controlling entities
	  * using monte carlo simulations, reaching a maximum width of confidence interval
	  * at 95% of error
	  * In any case, we never run less simulations than the minimum of Util.MINIMUM_SIMUALTION
	  * in order to keep a good level of estimation for the supporting power
	  * @param type: ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	  * @return
	  */
	 private Set<StableControlConfiguration> getMostProbableControllingEntities(int type, double error) {
			Map<StableControlConfiguration, Integer> result = new HashMap<StableControlConfiguration, Integer>();
			Map<StableControlConfiguration, SupportingPowerRecorder> temp_recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();
			
			this.controllingPower = -1;
			double current_max = 0;
			int N = Util.MINIMUM_SIMULATION;
			int current_simu = 0;
			while(current_simu < N || current_simu < util.Util.MINIMUM_SIMULATION) {
				ArgumentFramework af = this.generator.getRandomRootCompletion();
				
				CSP_Completion_Solver solver = new CSP_Completion_Solver(this.PCAF, af);
				Map<StableControlConfiguration, Set<StableExtension>> solutions = null;
				Set<StableControlConfiguration> cc_list = null;
				Set<StableExtension> stables = null;
				//System.out.println("nbr of simulations: " + current_simu);
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
						if(count.intValue() +1 > current_max) {
							current_max = count.intValue() +1;
						}
					} else {
						result.put(scc, new Integer(1));
						recorder = new SupportingPowerRecorder();
						temp_recorders.put(scc,  recorder);
						if(current_max <1) {
							current_max = 1;
						}
					}
					//System.out.println("stable extensions found for:" + scc.toString() + " = " + stables.size());
					recorder.updateOccurencesList(stables, af);
					/*
					if(type == ControllabilityEncoder.CREDULOUS) {
						recorder.updateOccurencesListCred(stables);
					} else {
						recorder.updateOccurencesListSke(stables);
					} */
				}
				
				current_simu++;
				N = (int)util.Util.getNewSimulationNumber(current_max, current_simu, error);
				//System.out.println("nbr of simulations needed: " + N);
			}

			//System.out.println("number of simulation to reach error level of : " + error + " is: " + current_simu);
			this.controllingPower = current_max;
			this.total_simulations = current_simu;
			Set<StableControlConfiguration> selection = this.takeMax(result, temp_recorders).keySet();
			this.controllingPower = current_max/current_simu;

			this.min_interval = this.controllingPower - this.getConfidenceInterval(current_simu);
			this.max_interval = this.controllingPower + this.getConfidenceInterval(current_simu);

			return selection;
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
		// System.out.println("------------ CALCULATE CONTROLLING POWER ------------------");
		 for(StableControlConfiguration scc : imput.keySet()) {
			 int value = imput.get(scc).intValue();
			 if(value >= this.controllingPower) {
				 this.controllingPower = (double)value;
				// System.out.println("controlling power raw: " + this.controllingPower);
			 }
		 }
		
	 }
}
