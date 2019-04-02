package solvers;

import java.util.Set;

import generators.ControllabilityEncoder;
import model.CArgument;
import model.PControlAF;

public class Prefered_Controlling_Entities_Solver {
	protected PControlAF PCAF;
	
	public Prefered_Controlling_Entities_Solver(PControlAF PCAF) {
		this.PCAF = PCAF;
	}
	
	/**
	 * returns the set of credulous control configurations
	 * @param N
	 * @return
	 */
	public Set<StableControlConfiguration> getPreferedCredulousCE(int N, StableControlConfiguration ce, Set<CArgument> target) {
		return this.getPreferedCE(N, ControllabilityEncoder.CREDULOUS, ce, target);
	}
	
	/**
	 * returns the set of skeptical control configurations
	 * @param N
	 * @return
	 */
	public Set<StableControlConfiguration> getPreferedSkepticalCE(int N, StableControlConfiguration ce, Set<CArgument> target) {
		return this.getPreferedCE(N, ControllabilityEncoder.SKEPTICAL, ce, target);
	}
	
	/**
	 * returns the controlling power of a given Control Entity (modeled with a StableControlConfiguration)
	 * with regard to a given target
	 * use of a CSP_Completion_Verifier to test Credulous/Skeptical acceptance of ce
	 * @param N, number of simulations
	 * @param type, ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	 * @return
	 */
	 private Set<StableControlConfiguration> getPreferedCE(int N, int type, StableControlConfiguration ce, Set<CArgument> target) {
		 return null;
	 }
}
