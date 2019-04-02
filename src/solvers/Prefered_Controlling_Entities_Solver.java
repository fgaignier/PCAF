package solvers;

import java.util.List;
import java.util.Set;

import generators.ControllabilityEncoder;
import model.CArgument;
import model.PControlAF;

/**
 * according to the paper, calculates the preferred controlling entities
 * for a PCAF according to a preference relation (total pre-order) on the Target
 * the preference relation is modeled with a list of sets.
 * The first element is the most preferred targets (all indifferent).....
 * @author Fabrice
 *
 */
public class Prefered_Controlling_Entities_Solver {
	protected PControlAF PCAF;
	protected List<Set<CArgument>> preference;
	
	public Prefered_Controlling_Entities_Solver(PControlAF PCAF, List<Set<CArgument>> preference) {
		this.PCAF = PCAF;
		this.preference = preference;
	}
	
	/**
	 * returns the set of credulous control configurations
	 * @param N
	 * @return
	 */
	public Set<StableControlConfiguration> getPreferedCredulousCE(int N) {
		return this.getPreferedCE(N, ControllabilityEncoder.CREDULOUS);
	}
	
	/**
	 * returns the set of skeptical control configurations
	 * @param N
	 * @return
	 */
	public Set<StableControlConfiguration> getPreferedSkepticalCE(int N) {
		return this.getPreferedCE(N, ControllabilityEncoder.SKEPTICAL);
	}
	
	/**
	 * returns the controlling power of a given Control Entity (modeled with a StableControlConfiguration)
	 * with regard to a given target
	 * use of a CSP_Completion_Verifier to test Credulous/Skeptical acceptance of ce
	 * @param N, number of simulations
	 * @param type, ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	 * @return
	 */
	 private Set<StableControlConfiguration> getPreferedCE(int N, int type) {
		 // T0 is the first element of the preference list
		 Set<CArgument> T0 = this.preference.remove(0);
		 this.PCAF.setTarget(T0);
		 Most_Probable_Controlling_Entities_Solver solver = new Most_Probable_Controlling_Entities_Solver(this.PCAF);
		 Set<StableControlConfiguration> CE0 = null;
		 if(type == ControllabilityEncoder.CREDULOUS) {
			 CE0 = solver.getCredulousControlConfigurations(N);
		 } else {
			 CE0 = solver.getSkepticalControlConfigurations(N);
		 }
		 for(Set<CArgument> T : this.preference) {
			 
		 }
		 
		 return null;
	 }
	 
	 /**
	  * returns the next subset of StableControlConfigurations in taking 
	  * the ones with the highest controlling power wrt Ti
	  */
	 private Set<StableControlConfiguration> getMaximumWRT(Set<StableControlConfiguration> CEi, Set<CArgument> subTarget) {
		 return null;
	 }
}
