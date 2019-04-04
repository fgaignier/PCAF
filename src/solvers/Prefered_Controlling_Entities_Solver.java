package solvers;

import java.util.ArrayList;
import java.util.HashSet;
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
	protected Set<CArgument> originalTarget;
	
	public Prefered_Controlling_Entities_Solver(PControlAF PCAF, List<Set<CArgument>> preference) {
		this.PCAF = PCAF;
		this.preference = this.clonePreference(preference);
		this.originalTarget = this.PCAF.getTarget();
	}
	
	/**
	 * need to clone the preference list since it is modified in 
	 * calculation (the highest preference is removed
	 * @param preference
	 * @return
	 */
	private List<Set<CArgument>> clonePreference(List<Set<CArgument>> preference) {
		List<Set<CArgument>> result = new ArrayList<Set<CArgument>>();
		for(Set<CArgument> sub : preference) {
			result.add(sub);
		}
		return result;
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
		 Set<StableControlConfiguration> CEi = null;
		 if(type == ControllabilityEncoder.CREDULOUS) {
			 CEi = solver.getCredulousControlConfigurations(N);
		 } else {
			 CEi = solver.getSkepticalControlConfigurations(N);
		 }
		 for(Set<CArgument> T : this.preference) {
			 CEi = this.getMaximumWRT(N, type, CEi, T);
		 }
		 
		 this.PCAF.setTarget(this.originalTarget);
		 
		 return CEi;
	 }
	 
	 /*
	 private void printConfs(Set<StableControlConfiguration> confs) {
		 for(StableControlConfiguration scc : confs) {
			 System.out.println(scc.toString());
			 System.out.println("------");
		 }
	 }
	 */
	 
	 /**
	  * returns the next subset of StableControlConfigurations in taking 
	  * the ones with the highest controlling power wrt Ti
	  */
	 private Set<StableControlConfiguration> getMaximumWRT(int N, int type, Set<StableControlConfiguration> CEi, Set<CArgument> subTarget) {
		 Controlling_Power_Solver cpSolver = new Controlling_Power_Solver(this.PCAF);
		 Set<StableControlConfiguration> result = new HashSet<StableControlConfiguration>();
		 double max = -1;
		 for(StableControlConfiguration ce : CEi) {
			 double current = -1;
			 if(type == ControllabilityEncoder.CREDULOUS) {
				 current = cpSolver.getCredulousControllingPower(N, ce, subTarget);
			 } else {
				 current = cpSolver.getSkepticalControllingPower(N, ce, subTarget);
			 }
			 if(current > max) {
				 result.clear();
				 result.add(ce);
				 max = current;
			 } else if(current == max) {
				 result.add(ce);
			 }
		 }
		 return result;
	 }
}
