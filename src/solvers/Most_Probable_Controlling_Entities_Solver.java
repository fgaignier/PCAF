package solvers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import generators.ControllabilityEncoder;
import generators.RandomProbaRootCompletionGenerator;
import model.ArgumentFramework;
import model.PControlAF;

/**
 * Use of Monte Carlo simulation to calculate 
 * the most probable controlling entities (together with their controlling power)
 * @author Fabrice
 *
 */
public class Most_Probable_Controlling_Entities_Solver {
	private PControlAF PCAF;
	private RandomProbaRootCompletionGenerator generator; 
	private double controllingPower;
	
	public Most_Probable_Controlling_Entities_Solver(PControlAF PCAF) {
		this.PCAF = PCAF;
		this.generator = new RandomProbaRootCompletionGenerator(this.PCAF);
		this.controllingPower = -1;
	}
	
	public double getControllingPower() {
		return this.controllingPower;
	}
	
	/**
	 * returns the most probable controlling entities
	 * use of a CSP_Completion_Solver to get Credulous/Skeptical Set<StableControlConfiguration>
	 * stored in a Map
	 * @param N, number of simulations
	 * @param type, ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	 * @return
	 */
	 public Set<StableControlConfiguration> getMostProbableControllingEntities(int N, int type) {
		Map<StableControlConfiguration, Integer> result = new HashMap<StableControlConfiguration, Integer>();
		this.controllingPower = -1;
		for(int i = 0; i<N; i++) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();
			CSP_Completion_Solver solver = new CSP_Completion_Solver(this.PCAF, af);
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
		double min = this.controllingPower - this.getConfidenceInterval(N);
		double max = this.controllingPower + this.getConfidenceInterval(N);
		System.out.println("confidence interval 95%: [" + min + " , " + max + "]");
		return selection;
	}
	 
	 private double getConfidenceInterval(int nbSimu) {
		 double value = 1.96;
		 double temp = this.controllingPower*(1-this.controllingPower)/nbSimu;
		 value = value*Math.sqrt(temp);
		 return value;
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
		 System.out.println("------------ TAKE MAX ------------------");
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
		 System.out.println("------------ CALCULATE CONTROLLING POWER ------------------");
		 for(StableControlConfiguration scc : imput.keySet()) {
			 int value = imput.get(scc).intValue();
			 if(value >= this.controllingPower) {
				 this.controllingPower = (double)value;
			 }
		 }
		
	 }
}
