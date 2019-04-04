package solvers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import generators.ControllabilityEncoder;
import generators.RandomRootCompletionGenerator;
import model.ArgumentFramework;
import model.ControlAF;

/**
 * Use of Monte Carlo simulation to calculate 
 * the control configurations of a CAF
 * in this version you need to give a number of simulations
 * Need to implement with max error of 95% confidence interval
 * @author Fabrice
 *
 */
public class Monte_Carlo_CAF_Solver {
	private ControlAF CAF;
	private RandomRootCompletionGenerator generator; 
	private double controllingPower;
	
	public Monte_Carlo_CAF_Solver(ControlAF CAF) {
		this.CAF = CAF;
		this.generator = new RandomRootCompletionGenerator(this.CAF);
		this.controllingPower = -1;
	}
	
	public double getControllingPower() {
		return this.controllingPower;
	}
	
	/**
	 * returns the set of credulous control configurations
	 * @param N
	 * @return
	 */
	public Set<StableControlConfiguration> getCredulousControlConfigurations(int N) {
		return this.getControlConfigurations(N, ControllabilityEncoder.CREDULOUS);
	}
	
	/**
	 * returns the set of skeptical control configurations
	 * @param N
	 * @return
	 */
	public Set<StableControlConfiguration> getSkepticalControlConfigurations(int N) {
		return this.getControlConfigurations(N, ControllabilityEncoder.SKEPTICAL);
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
		this.controllingPower = -1;
		for(int i = 0; i<N; i++) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();
			CSP_Completion_Solver solver = new CSP_Completion_Solver(this.CAF, af);
			Set<StableControlConfiguration> cc_list = null;
			if(type == ControllabilityEncoder.CREDULOUS) {
				cc_list = solver.getCredulousControlConfigurations();
			} else {
				cc_list = solver.getSkepticalControlConfigurations();
			}
			for(StableControlConfiguration scc : cc_list) {
				StableControlConfiguration present = this.find(result.keySet(), scc);
				if(present != null) {
					Integer count = result.get(present);
					Integer newVal = new Integer(count.intValue()+1);
					result.put(present, newVal);
				} else {
					result.put(scc, new Integer(1));
				}
			}
		}
		this.setControllingPower(result);
		Set<StableControlConfiguration> selection = this.takeMax(result).keySet();
		this.controllingPower = this.controllingPower/N;
		if(controllingPower < 1) {
			return null;
		} else {
			return selection;
		}
	}
	 
	 /**
	  * looks for a StableControlConfiguration in a list
	  * This is done since new objects are calculated all the time.
	  * Therefore the objects are different, but we want to know if the control elements are the same
	  * @param list
	  * @param cc
	  * @return
	  */
	 private StableControlConfiguration find(Set<StableControlConfiguration> list, StableControlConfiguration cc) {
		 for(StableControlConfiguration scc : list) {
			 if(cc.equals(scc)) {
				 return scc;
			 }
		 }
		 return null;
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
}
