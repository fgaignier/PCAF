package solvers;

import model.ControlAF;
import model.StableControlConfiguration;
import model.StableExtension;
import model.ArgumentFramework;
import model.CArgument;


import java.util.Set;

/**
 * This class enables to check if a control entity is in control of a given completion
 * This is useful in two cases:
 * 1) solve the controllability of a CAF. Since a cc is the intersection of all cc for all completions
 * 2) For section 6.3 of the report
 */
public class CSP_Completion_Verifier {

	protected ControlAF CAF;
	protected ArgumentFramework completion;


	public CSP_Completion_Verifier(ControlAF CAF, ArgumentFramework completion) {
		this.CAF = CAF;
		this.completion = completion;
	}

	public ControlAF getCAF() {
		return CAF;
	}


	public void setCAF(ControlAF CAF) {
		this.CAF = CAF;
	}
	
	/**
	 * builds the AF from the root completion and adding all the 
	 * control arguments in cc plus all the possible control attacks (the ones
	 * for which both arguments are present
	 * @param cc
	 * @return
	 */
	/** DEPRECATED. MOVED TO THE CLASS ControlAF
	protected ArgumentFramework buildAF(StableControlConfiguration cc) {
		//clone the completion
		ArgumentFramework result = this.completion.clone();
		// add the control arguments
		for(CArgument c : cc.getOnControl()) {
			result.addArgument(c);
		}
		// add all the possible control attacks
		// if one argument is missing, we ignore the attack
		Set<CAttack> control = this.CAF.getAttacksByType(CAttack.Type.CONTROL);
		for(CAttack att : control) {
			try {
				result.addAttack(att);
			} catch (UnknownArgumentError e) {
			//	System.out.println(e.getMessage());
			}
		}
		return result;
	}
	**/

	/**
	 * checks if cc is a control configuration for credulous acceptance
	 * just needs to build the corresponding AF, finding all the stable extensions
	 * if the Target belongs to one stable extension returns true. Else false
	 * @param cc
	 * @return
	 */
	public boolean isCredulousControlConfigurations(StableControlConfiguration cc) {
		ArgumentFramework af = this.CAF.buildAF(this.completion, cc);
		CSP_AF_Solver solver = new CSP_AF_Solver(af);
		Set<StableExtension> extensions = solver.getStableSets();
		Set<CArgument> T = this.CAF.getTarget();
		for(StableExtension ext : extensions) {
			// we check that we find at least one extension that contains T and cc
			if(ext.contains(T) && ext.contains(cc.getOnControl())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * checks if cc is a control configuration for credulous acceptance
	 * just needs to build the corresponding AF, finding all the stable extensions
	 * if the Target belongs to ALL stable extension returns true. Else false
	 * @param cc
	 * @return
	 */
	public boolean isSkepticalControlConfigurations(StableControlConfiguration cc) {
		ArgumentFramework af = this.CAF.buildAF(this.completion, cc);
		CSP_AF_Solver solver = new CSP_AF_Solver(af);
		Set<StableExtension> extensions = solver.getStableSets();
		Set<CArgument> T = this.CAF.getTarget();
		for(StableExtension ext : extensions) {
			// we check that all extensions contain T and cc
			if(!( ext.contains(T) && ext.contains(cc.getOnControl()) )) {
				return false;
			}
		}
		return true;
	}
	
}
