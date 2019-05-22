package solvers;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import generators.ControllabilityEncoder;
import generators.RandomCAFRootCompletionGenerator;
import model.ArgumentFramework;
import model.ControlAF;
import model.SupportingPowerRecorder;
import model.StableControlConfiguration;
import model.StableExtension;

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
	private RandomCAFRootCompletionGenerator generator; 
	private double controllingPower;
	private Map<StableControlConfiguration, SupportingPowerRecorder> recorders;
	
	public Monte_Carlo_CAF_Solver(ControlAF CAF) {
		this.CAF = CAF;
		this.generator = new RandomCAFRootCompletionGenerator(this.CAF);
		this.controllingPower = -1;
		this.recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();
	}
	
	public double getControllingPower() {
		return this.controllingPower;
	}
	
	public Map<StableControlConfiguration, SupportingPowerRecorder> getSupportingPowerRecorders() {
		return this.recorders;
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
		Map<StableControlConfiguration, SupportingPowerRecorder> temp_recorders = new HashMap<StableControlConfiguration, SupportingPowerRecorder>();

		this.controllingPower = -1;
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
			for(StableControlConfiguration scc : cc_list) {
				stables = solutions.get(scc);
				StableControlConfiguration present = util.Util.find(result.keySet(), scc);
				if(present != null) {
					Integer count = result.get(present);
					Integer newVal = new Integer(count.intValue()+1);
					result.put(present, newVal);
					SupportingPowerRecorder recorder = temp_recorders.get(present);
					recorder.updateOccurencesList(stables);
				} else {
					result.put(scc, new Integer(1));
					SupportingPowerRecorder recorder = new SupportingPowerRecorder();
					recorder.updateOccurencesList(stables);
					temp_recorders.put(scc,  recorder);
				}
			}
		}
		
		this.setControllingPower(result);
		//System.out.println("controlling power = " + this.controllingPower);
		Set<StableControlConfiguration> selection = this.takeMax(result, temp_recorders).keySet();
		this.controllingPower = this.controllingPower/N;
		if(controllingPower < 1) {
			this.recorders = null;
			return null;
		} else {
			return selection;
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
		
	 }
}
