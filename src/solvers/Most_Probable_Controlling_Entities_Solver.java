package solvers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import generators.ControllabilityEncoder;
import generators.RandomPCAFRootCompletionGenerator;
import model.ArgumentFramework;
import model.PControlAF;
import util.Util;

/**
 * Use of Monte Carlo simulation to calculate 
 * the most probable controlling entities (together with their controlling power)
 * Two ways: Indicate the number of simulations you want
 * or the error (width) of the 95% confidence interval 
 * @author Fabrice
 *
 */
public class Most_Probable_Controlling_Entities_Solver {
	private PControlAF PCAF;
	private RandomPCAFRootCompletionGenerator generator; 
	private double controllingPower;
	private double min_interval;
	private double max_interval;
	
	public Most_Probable_Controlling_Entities_Solver(PControlAF PCAF) {
		this.PCAF = PCAF;
		this.generator = new RandomPCAFRootCompletionGenerator(this.PCAF);
		this.controllingPower = -1;
		this.min_interval = 0;
		this.max_interval = 0;
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
		this.controllingPower = -1;
		for(int i = 0; i<N; i++) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();
			/*
			System.out.println("random root completion:");
			System.out.println(af.toString());
			System.out.println("--------------------------");
			*/
			CSP_Completion_Solver solver = new CSP_Completion_Solver(this.PCAF, af);
			Set<StableControlConfiguration> cc_list = null;
			if(type == ControllabilityEncoder.CREDULOUS) {
				cc_list = solver.getCredulousControlConfigurations();
			} else {
				cc_list = solver.getSkepticalControlConfigurations();
			}
			for(StableControlConfiguration scc : cc_list) {
				StableControlConfiguration present = util.Util.find(result.keySet(), scc);
				if(present != null) {
					Integer count = result.get(present);
					Integer newVal = new Integer(count.intValue()+1);
					result.put(present, newVal);
					//System.out.println("updates cc with value: " + newVal.toString() + " over " + i + " tries");
				} else {
					result.put(scc, new Integer(1));
				}
			}
		}
		this.setControllingPower(result);
		Set<StableControlConfiguration> selection = this.takeMax(result).keySet();
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
	 
	 private double getNewSimulationNumber(double max, double nbSimu, double error) {
		 double result = (max/nbSimu)*(1-(max/nbSimu));
		 result = result*Math.pow(Util.CONFIDENCE_INT,2);
		 result = result / (Math.pow(error,2));
		 return result;
	 }
	 
	 /**
	  * returns the most probable controlling entities
	  * using monte carlo simulations, reaching a maximum width of confidence interval
	  * at 95% of error
	  * @param type: ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	  * @return
	  */
	 private Set<StableControlConfiguration> getMostProbableControllingEntities(int type, double error) {
			Map<StableControlConfiguration, Integer> result = new HashMap<StableControlConfiguration, Integer>();
			this.controllingPower = -1;
			double current_max = 0;
			int N = Util.MINIMUM_SIMULATION;
			int current_simu = 0;
			while(current_simu < N) {
				ArgumentFramework af = this.generator.getRandomRootCompletion();
				CSP_Completion_Solver solver = new CSP_Completion_Solver(this.PCAF, af);
				Set<StableControlConfiguration> cc_list = null;
				if(type == ControllabilityEncoder.CREDULOUS) {
					cc_list = solver.getCredulousControlConfigurations();
				} else {
					cc_list = solver.getSkepticalControlConfigurations();
				}
				for(StableControlConfiguration scc : cc_list) {
					StableControlConfiguration present = util.Util.find(result.keySet(), scc);
					if(present != null) {
						Integer count = result.get(present);
						Integer newVal = new Integer(count.intValue()+1);
						result.put(present, newVal);
						if(count.intValue() +1 > current_max) {
							current_max = count.intValue() +1;
						}
					} else {
						result.put(scc, new Integer(1));
						if(current_max <1) {
							current_max = 1;
						}
					}
				}
				current_simu++;
				if(current_max > 1) {
					N = (int)this.getNewSimulationNumber(current_max, current_simu, error);
				}
			}
			//this.setControllingPower(result);
			this.controllingPower = current_max;
			//System.out.println("controlling power raw: " + this.controllingPower);
			//System.out.println("nbr simulations to reach 0.1 error: " + current_simu);
			Set<StableControlConfiguration> selection = this.takeMax(result).keySet();
			//this.controllingPower = this.controllingPower/N;
			this.controllingPower = current_max/current_simu;

			this.min_interval = this.controllingPower - this.getConfidenceInterval(current_simu);
			this.max_interval = this.controllingPower + this.getConfidenceInterval(current_simu);
			//System.out.println("confidence interval 95%: [" + min + " , " + max + "]");
			return selection;
		}

	 /**
	  * looks for a StableControlConfiguration in a list
	  * This is done since new objects are calculated all the time.
	  * Therefore the objects are different, but we want to know if the control elements are the same
	  * @param list
	  * @param cc
	  * @return
	  */
	 /*
	 private StableControlConfiguration find(Set<StableControlConfiguration> list, StableControlConfiguration cc) {
		 for(StableControlConfiguration scc : list) {
			 if(cc.equals(scc)) {
				 return scc;
			 }
		 }
		 return null;
	 }
	 */
	 
	 /**
	  * isolate the most probable controlling entities from
	  * all found control entities
	  * @param imput
	  * @return
	  */
	 private Map<StableControlConfiguration, Integer> takeMax(Map<StableControlConfiguration, Integer> imput) {
		 Map<StableControlConfiguration, Integer> result = new HashMap<StableControlConfiguration, Integer>();
		// System.out.println("------------ TAKE MAX ------------------");
		 for(StableControlConfiguration scc : imput.keySet()) {
			 Integer value = imput.get(scc);
			 double val = (double)value.intValue();
			// System.out.println("val for scc " + val);
			// System.out.println("max val " + this.controllingPower);
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
