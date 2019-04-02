package solvers;

import java.util.Set;

import generators.ControllabilityEncoder;
import generators.RandomProbaRootCompletionGenerator;
import model.ArgumentFramework;
import model.CArgument;
import model.PControlAF;

public class Controlling_Power_Solver {
	private PControlAF PCAF;
	private RandomProbaRootCompletionGenerator generator; 
	
	public Controlling_Power_Solver(PControlAF PCAF) {
		this.PCAF = PCAF;
		this.generator = new RandomProbaRootCompletionGenerator(this.PCAF);
	}
	
	/**
	 * returns the set of credulous control configurations
	 * @param N
	 * @return
	 */
	public double getCredulousControllingPower(int N, StableControlConfiguration ce, Set<CArgument> target) {
		return this.getControllingPower(N, ControllabilityEncoder.CREDULOUS, ce, target);
	}
	
	/**
	 * returns the set of skeptical control configurations
	 * @param N
	 * @return
	 */
	public double getSkepticalControllingPower(int N, StableControlConfiguration ce, Set<CArgument> target) {
		return this.getControllingPower(N, ControllabilityEncoder.SKEPTICAL, ce, target);
	}
	
	/**
	 * returns the controlling power of a given Control Entity (modeled with a StableControlConfiguration)
	 * with regard to a given target
	 * use of a CSP_Completion_Verifier to test Credulous/Skeptical acceptance of ce
	 * @param N, number of simulations
	 * @param type, ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	 * @return
	 */
	 private double getControllingPower(int N, int type, StableControlConfiguration ce, Set<CArgument> target) {
		double result = 0;
		this.PCAF.setTarget(target);
		for(int i = 0; i<N; i++) {
			ArgumentFramework af = this.generator.getRandomRootCompletion();
			CSP_Completion_Verifier verifier = new CSP_Completion_Verifier(this.PCAF, af);
			if(type == ControllabilityEncoder.CREDULOUS) {
				if(verifier.isCredulousControlConfigurations(ce)) {
					result = result + 1;
				}
			} else {
				if(verifier.isSkepticalControlConfigurations(ce)) {
					result = result + 1;
				}
			}
		}
		return result/N;
	}
}
