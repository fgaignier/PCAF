package solvers;

import java.util.Set;
import java.util.Iterator;

import generators.HardestRootCompletionGenerator;
import model.CArgument;
import model.ControlAF;
import model.WeightedArgumentFramework;
import util.Util;

public class CSP_CAF_Solver {
	ControlAF CAF;
	
	public CSP_CAF_Solver(ControlAF CAF) {
		this.CAF = CAF;
	}
	
	public Set<StableControlConfiguration> getCredulousControlConfigurations() {
		Set<CArgument> targets = CAF.getTarget();
		Iterator<CArgument> iter = targets.iterator();
		HardestRootCompletionGenerator hrcg = new HardestRootCompletionGenerator(CAF);
		Set<StableControlConfiguration> credulous = null;
		
		while(iter.hasNext()) {
			CArgument target = iter.next();
			WeightedArgumentFramework waf = hrcg.getHardestRootCompletionWRT(target);
			System.out.println("printout of the hardest af to control wrt " + target.getName());
			System.out.println(waf.toString());
		
			CSP_Completion_Solver csp = new CSP_Completion_Solver(CAF, waf);
			if(credulous == null) {
				credulous = csp.getCredulousControlConfigurations();
			} else {
				credulous = Util.intersect(credulous, csp.getCredulousControlConfigurations());
			}
		}
		
		return credulous;
	}
	
	public Set<StableControlConfiguration> getSkepticalControlConfigurations() {
		Set<CArgument> targets = CAF.getTarget();
		Iterator<CArgument> iter = targets.iterator();
		HardestRootCompletionGenerator hrcg = new HardestRootCompletionGenerator(CAF);
		Set<StableControlConfiguration> skeptical = null;
		
		while(iter.hasNext()) {
			CArgument target = iter.next();
			WeightedArgumentFramework waf = hrcg.getHardestRootCompletionWRT(target);
			//System.out.println("printout of the hardest af to control wrt " + target.getName());
			//System.out.println(waf.toString());
		
			CSP_Completion_Solver csp = new CSP_Completion_Solver(CAF, waf);
			if(skeptical == null) {
				skeptical = csp.getSkepticalControlConfigurations();
			} else {
				skeptical = Util.intersect(skeptical, csp.getCredulousControlConfigurations());
			}
		}
		
		return skeptical;
	}
}
