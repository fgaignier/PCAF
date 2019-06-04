package solvers;

import model.ControlAF;
import model.StableControlConfiguration;
import model.ArgumentFramework;
import model.CArgument;
import model.Argument;
import model.UnknownArgumentError;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.constraints.*;

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


	public void setCAF(ControlAF cAF) {
		CAF = cAF;
	}

	/**
	 * returns true if cc skeptically controls the CAF
	 * according to the protected element list
	 */
	public boolean isSkepticalControlConfigurations(StableControlConfiguration cc) {
		boolean isCredulousCC = this.isCredulousControlConfigurations(cc);
		if(isScepticallyAccepted(cc) && isCredulousCC) {
			return true;
		}
		return false;
	}

	/**
	 * remove from attackers the arguments that are not used as a variable
	 * present is therefore the keySet of accVar
	 * @param attackers
	 * @param present
	 * @return
	 */
	private Set<Argument> removeUnused(Set<Argument> attackers, Set<String> present) {
		Set<Argument> result = new HashSet<Argument>();
		for(Argument arg : attackers) {
			if(present.contains(arg.getName())) {
				result.add(arg);
			}
		}
		return result;
	}

	/**
	 * returns true if cc credulously controls the CAF
	 * according to the protected element list
	 */
	public boolean isCredulousControlConfigurations(StableControlConfiguration cc) {

		// variables must be stored in a structure for later access
		// acc variables and on variables
		Map<String, IntVar> accVar = new HashMap<String, IntVar>();
		Map<String, IntVar> onVar = new HashMap<String, IntVar>();

		// 1. Create the CSP Model
		Model model = new Model("Credulous cc verifyer");

		// 2. Create variables
		// one for each argument in the completion (root completion)
		// accepted or not
		Set<Argument> args = this.completion.getAllArguments();
		for(Argument arg : args) {
			String argName = arg.getName();
			IntVar acc = model.intVar("acc_" + argName, new int[]{0,1});
			accVar.put(argName,  acc);
			//System.out.println("adding variable " + acc.getName());
		}

		// two for each control argument in the cc
		// accepted (acc) and on 
		Set<CArgument> controlArgs = cc.getOnControl();
		for(CArgument carg : controlArgs) {
			String cargName = carg.getName();
			IntVar acc = model.intVar("acc_" + cargName,new int[]{0,1});
			IntVar on = model.intVar("on_" + cargName, new int[]{0,1});
			accVar.put(cargName, acc);
			onVar.put(cargName, on);
			//System.out.println("adding variable " + acc.getName());
			//System.out.println("adding variable " + on.getName());
		}

		/*
		 * Constraints
		 */
		// arguments to be protected are accepted (by definition)
		Set<CArgument> T = CAF.getTarget();
		for(CArgument carg : T) {
			IntVar acc = accVar.get(carg.getName());
			model.arithm(acc, "=", 1).post();
			//System.out.println("adding constraint: " + acc.getName() + "=1");
		}

		// elements of cc must be on and accepted
		for(CArgument carg : controlArgs) {
			String argName = carg.getName();
			IntVar onIt = onVar.get(argName);
			IntVar accIt = accVar.get(argName);
			model.arithm(onIt,"=",1).post();
			model.arithm(accIt,"=",1).post();
		}

		// no two arguments attacking each other in the solution
		// if an argument is accepted (from the root completion or control), all its attackers are rejected
		// else at least one attacker is accepted
		// first we iterate through the root Completion arguments
		for(Argument arg : args) {
			IntVar accCurrent = accVar.get(arg.getName());
			// all its attackers (including AC)
			Set<Argument> allAttackers = CAF.getArgumentAttackers(completion, arg);
			Set<Argument> attackers = this.removeUnused(allAttackers, accVar.keySet());
			IntVar[] sum = new IntVar[attackers.size()];
			int i = 0;
			for(Argument attacker : attackers) {
				IntVar accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, accCurrent=1
			if(attackers.size()==0) {
				model.arithm(accCurrent, "=", 1).post();
			} else {
				// here add or(and(sum=0, accCurrent=1), (sum!=0 and xi=0))
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint sumNotNull = model.sum(sum, ">", 0);
				Constraint andNull = model.and(sumNull, model.arithm(accCurrent, "=",1));
				Constraint andNotNull = model.and(sumNotNull, model.arithm(accCurrent, "=",0));
				model.or(andNull, andNotNull).post();
			}

		}

		// same thing but on the control arguments
		for(CArgument carg : controlArgs) {
			IntVar accCurrent = accVar.get(carg.getName());
			Set<Argument> allAttackers = CAF.getControlAttackers(carg, this.completion);
			Set<Argument> attackers = this.removeUnused(allAttackers, accVar.keySet());
			IntVar[] sum = new IntVar[attackers.size()];
			int i = 0;
			for(Argument attacker : attackers) {
				IntVar accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, adding no constraint
			if(attackers.size()==0) {
				//System.out.println("adding no constraint for : " + accCurrent.getName());
			} else {
				// here add or(and(sum=0, accCurrent=1), (sum!=0 and xi=0))
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint sumNotNull = model.sum(sum, ">", 0);
				Constraint andNull = model.and(sumNull, model.arithm(accCurrent, "=",1));
				Constraint andNotNull = model.and(sumNotNull, model.arithm(accCurrent, "=",0));
				model.or(andNull, andNotNull).post();
				//System.out.println("adding constraint: [sum(" + this.fromTabToString(sum) + ") = 0 and " + accCurrent.getName() + 
				//					"=1] or [sum(" + this.fromTabToString(sum) + ") !=0  and " + accCurrent.getName() + "=0]");
			}
		}

		// 4. Solve the problem and return the set of solutions
		if(model.getSolver().solve()) {
			return true;
		} 
		return false;
	}

	/**
	 * once a stable control configuration is calculated
	 * it implies credulous acceptance of protected arguments
	 * We need to perform this check to test if it is skeptically accepted
	 * We test if, with the solution, it is possible to have not all protected arguments accepted
	 * if yes, return false, else return true
	 */
	protected  boolean isScepticallyAccepted(StableControlConfiguration cc) {

		// variables must be stored in a structure for later access
		// acc variables and on variables
		Map<String, IntVar> accVar = new HashMap<String, IntVar>();
		Map<String, IntVar> onVar = new HashMap<String, IntVar>();

		// 1. Create the CSP Model
		Model model = new Model("Skeptical cc verifyer");

		// 2. Create variables
		// one for each argument in the completion (root completion)
		// accepted or not
		Set<Argument> args = this.completion.getAllArguments();
		for(Argument arg : args) {
			String argName = arg.getName();
			IntVar acc = model.intVar("acc_" + argName, new int[]{0,1});
			accVar.put(argName,  acc);
			//System.out.println("adding variable " + acc.getName());
		}

		// two for each control argument in the cc
		// accepted (acc) and on 
		Set<CArgument> controlArgs = cc.getOnControl();
		for(CArgument carg : controlArgs) {
			String cargName = carg.getName();
			IntVar acc = model.intVar("acc_" + cargName,new int[]{0,1});
			IntVar on = model.intVar("on_" + cargName, new int[]{0,1});
			accVar.put(cargName, acc);
			onVar.put(cargName, on);
			//System.out.println("adding variable " + acc.getName());
			//System.out.println("adding variable " + on.getName());
		}

		/*
		 * Constraints
		 */
		// arguments to be protected are not all accepted (i.e. the solution is credulous)
		// Sum(acc < n) with n number of protected arguments
		Set<CArgument> T = CAF.getTarget();
		IntVar[] protectedSum = new IntVar[T.size()];
		int i = 0;
		for(CArgument carg : T) {
			IntVar acc = accVar.get(carg.getName());
			protectedSum[i] = acc;
			i++;
		}
		model.sum(protectedSum, "<", T.size()).post();

		// we impose the same control configuration (for on, but no acceptance obligation) 
		for(CArgument carg : controlArgs) {
			String argName = carg.getName();
			IntVar onIt = onVar.get(argName);
			model.arithm(onIt,"=",1).post();
		}
		

		// no two arguments attacking each other in the solution
		// if an argument is accepted (from the root completion or control), all its attackers are rejected
		// else at least one attacker is accepted
		// first we iterate through the root Completion arguments
		for(Argument arg : args) {
			IntVar accCurrent = accVar.get(arg.getName());
			// all its attackers (including AC)
			Set<Argument> allAttackers = CAF.getArgumentAttackers(completion, arg);
			Set<Argument> attackers = this.removeUnused(allAttackers, accVar.keySet());
			IntVar[] sum = new IntVar[attackers.size()];
			i = 0;
			for(Argument attacker : attackers) {
				IntVar accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, accCurrent=1
			if(attackers.size()==0) {
				model.arithm(accCurrent, "=", 1).post();
			} else {
				// here add or(and(sum=0, accCurrent=1), (sum!=0 and xi=0))
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint sumNotNull = model.sum(sum, ">", 0);
				Constraint andNull = model.and(sumNull, model.arithm(accCurrent, "=",1));
				Constraint andNotNull = model.and(sumNotNull, model.arithm(accCurrent, "=",0));
				model.or(andNull, andNotNull).post();
			}

		}

		// same thing but on the control arguments
		for(CArgument carg : controlArgs) {
			IntVar accCurrent = accVar.get(carg.getName());
			Set<Argument> allAttackers = CAF.getControlAttackers(carg, this.completion);
			Set<Argument> attackers = this.removeUnused(allAttackers, accVar.keySet());
			IntVar[] sum = new IntVar[attackers.size()];
			i = 0;
			for(Argument attacker : attackers) {
				IntVar accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, adding no constraint
			if(attackers.size()==0) {
				//System.out.println("adding no constraint for : " + accCurrent.getName());
			} else {
				// here add or(and(sum=0, accCurrent=1), (sum!=0 and xi=0))
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint sumNotNull = model.sum(sum, ">", 0);
				Constraint andNull = model.and(sumNull, model.arithm(accCurrent, "=",1));
				Constraint andNotNull = model.and(sumNotNull, model.arithm(accCurrent, "=",0));
				model.or(andNull, andNotNull).post();
				//System.out.println("adding constraint: [sum(" + this.fromTabToString(sum) + ") = 0 and " + accCurrent.getName() + 
				//					"=1] or [sum(" + this.fromTabToString(sum) + ") !=0  and " + accCurrent.getName() + "=0]");
			}
		}
		
		// 4. Solve the problem and return the set of solutions

		if(model.getSolver().solve()) {
			return false;
		}  else {
			return true;
		}

	}


	/**
	 * protected internal use only
	 * toString method for an array of IntVar
	 */
	protected String fromTabToString(IntVar[] tab) {
		String result = new String();
		for(int i = 0; i<tab.length; i++) {
			result = result + tab[i].getName() + " , ";
		}

		return result;
	}

	/**
	 * builds the stable control configuration result of the CSP solution
	 * for internal use only
	 */
	protected StableControlConfiguration buildResultStable(Map<String, IntVar> accVar) {
		StableControlConfiguration scc = new StableControlConfiguration();
		for(String argName : accVar.keySet()) {
			IntVar accCurrent = accVar.get(argName);
			// all arguments must be accepted or rejected
			if(accCurrent.getValue() ==1) {
				CArgument currentArg = CAF.getArgumentByName(argName);
				if(currentArg == null) {
					throw new UnknownArgumentError("weird, argument " + argName + " is accepted but cannot be found in the CAF");
				} else {
					// here check if control argument and add on (since accepted and on are equal for control arguments)
					if(currentArg.getType() == CArgument.Type.CONTROL) {
						scc.addOnControl(currentArg);
					}
				}
			}
		}		
		return scc;
	}

}
