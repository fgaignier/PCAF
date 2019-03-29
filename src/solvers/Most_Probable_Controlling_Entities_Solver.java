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
			Completion_Proba_Calculator cpgen = new Completion_Proba_Calculator(this.PCAF);
			System.out.println("generated random af number " + i );
			System.out.println(cpgen.getProbability(af));
			CSP_Completion_Solver solver = new CSP_Completion_Solver(this.PCAF, af);
			Set<StableControlConfiguration> cc_list = null;
			if(type == ControllabilityEncoder.CREDULOUS) {
				cc_list = solver.getCredulousControlConfigurations();
			} else {
				cc_list = solver.getSkepticalControlConfigurations();
			}
			for(StableControlConfiguration scc : cc_list) {
				System.out.println("stable control configuration found:");
				System.out.println(scc.toString());
				StableControlConfiguration present = this.find(result.keySet(), scc);
				if(present != null) {
					Integer count = result.get(present);
					Integer newVal = new Integer(count.intValue()+1);
					result.put(present, newVal);
					System.out.println("already present, increasing its value to " + newVal.intValue());
				} else {
					result.put(scc, new Integer(1));
					System.out.println("new, setting its value to 1");
				}
			}
		}
		this.setControllingPower(result);
		Set<StableControlConfiguration> selection = this.takeMax(result).keySet();
		this.controllingPower = this.controllingPower/N;
		return selection;
	}
	 
	 private StableControlConfiguration find(Set<StableControlConfiguration> list, StableControlConfiguration cc) {
		 for(StableControlConfiguration scc : list) {
			 if(cc.equals(scc)) {
				 return scc;
			 }
		 }
		 return null;
	 }
	 
	 /*
	 private SortedMap<Integer, StableControlConfiguration> reverse(Map<StableControlConfiguration, Integer> imput) {
		 SortedMap<Integer, StableControlConfiguration> result = new TreeMap<Integer, StableControlConfiguration>();
		 for(StableControlConfiguration scc : imput.keySet()) {
			 Integer value = imput.get(scc);
			 result.put(value,  scc);
		 }
		 return result;
	 }
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
