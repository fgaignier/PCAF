package solvers;

import model.ControlAF;
import model.StableControlConfiguration;
import model.StableExtension;
import model.ArgumentFramework;
import model.CArgument;
import model.Argument;
import model.UnknownArgumentError;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import javafx.util.Pair;

import org.chocosolver.solver.constraints.*;

/**
 * Class to find all control configurations for a given completion of a CAF
 * Both credulous and skeptical acceptance
 */
public class CSP_Completion_Solver {

	protected ControlAF CAF;
	protected ArgumentFramework completion;

	public CSP_Completion_Solver(ControlAF CAF, ArgumentFramework completion) {
		this.CAF = CAF;
		this.completion = completion;
	}

	public ControlAF getCAF() {
		return CAF;
	}


	public void setCAF(ControlAF cAF) {
		CAF = cAF;
	}


	public ArgumentFramework getCompletion() {
		return completion;
	}


	public void setCompletion(ArgumentFramework completion) {
		this.completion = completion;
	}

	/**
	 * returns a Map of control configurations that skeptically control the CAF together
	 * with all the extensions that correspond to each control configuration
	 * First the credulous control confs are calculated. 
	 * If a control conf does not control skeptically, it is removed.
	 * @return
	 */
	public Map<StableControlConfiguration, Set<StableExtension>> getSkepticalControlConfigurations() {
		Map<StableControlConfiguration, Set<StableExtension>> solutions = this.getCredulousControlConfigurations();
		Map<StableControlConfiguration, Set<StableExtension>> result = new HashMap<StableControlConfiguration, Set<StableExtension>>();
		for(StableControlConfiguration scc : solutions.keySet()) {
			if(isScepticallyAccepted(scc)) {
				result.put(scc, solutions.get(scc));
			}
		}
		return result;
	}

	/**
	 * returns a Map of control configurations that credulously control the CAF together
	 * with all the extensions that correspond to each control configuration
	 */
	public Map<StableControlConfiguration, Set<StableExtension>> getCredulousControlConfigurations() {

		// variables must be stored in a structure for later access
		// acc variables and on variables
		Map<String, IntVar> accVar = new HashMap<String, IntVar>();
		Map<String, IntVar> onVar = new HashMap<String, IntVar>();

		// 1. Create the CSP Model
		Model model = new Model("Credulous CAF Solver");

		// 2. Create variables
		// one for each argument in the completion (root completion)
		// accepted or not
		Set<Argument> args = this.completion.getAllArguments();
		for(Argument arg : args) {
			String argName = arg.getName();
			IntVar acc = model.intVar("acc_" + argName, new int[]{0,1});
			accVar.put(argName,  acc);
			//System.out.println("adding variable " + acc);
		}

		// two for each control argument
		// accepted (acc) and on 

		Set<CArgument> controlArgs = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		for(Argument carg : controlArgs) {
			String cargName = carg.getName();
			String onCarg = cargName;
			IntVar acc = model.intVar("acc_" + cargName,new int[]{0,1});
			IntVar on = model.intVar("on_" + onCarg, new int[]{0,1});
			accVar.put(cargName, acc);
			onVar.put(cargName, on);
			//System.out.println("adding variable " + acc);
			//System.out.println("adding variable " + on);
		}

		/*
		 * Constraints
		 */
		// arguments to be protected are accepted (by definition)
		Set<CArgument> T = CAF.getTarget();
		for(CArgument t : T) {
			IntVar acc = accVar.get(t.getName());
			Constraint constraint = model.arithm(acc, "=", 1);
			constraint.post();
			//model.arithm(acc, "=", 1).post();
			//System.out.println(constraint);
		}


		// accepted control arguments must be on and vice versa
		Set<String> on = onVar.keySet();
		for(String argName : on) {
			IntVar onIt = onVar.get(argName);
			IntVar accIt = accVar.get(argName);
			Constraint constraint = model.arithm(onIt, "-", accIt, "=",0);
			constraint.post();
			//System.out.println(constraint);
		}

		// no two arguments attacking each other in the solution
		// if an argument is accepted (from the root completion or control), all its attackers are rejected
		// else at least one attacker is accepted
		// first we iterate through the root Completion arguments
		for(Argument arg : args) {
			IntVar accCurrent = accVar.get(arg.getName());
			// all its attackers (including AC) : AF, AU, AC
			Set<Argument> attackers = CAF.getArgumentAttackers(this.completion, arg);
			IntVar accAtt = null;
			IntVar[] sum = new IntVar[attackers.size()];
			int i = 0;
			for(Argument attacker : attackers) {
				accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, accCurrent=1
			if(attackers.size()==0) {
				//System.out.println("for argument " + accCurrent + " size of attackers = " + attackers.size());
				Constraint constraint = model.arithm(accCurrent, "=", 1);
				constraint.post();
				//System.out.println(constraint);
			} else {
				// here add or(and(sum=0, accCurrent=1), (sum!=0 and accCurrent=0))
				//System.out.println("for argument " + accCurrent + " size of attackers = " + attackers.size());
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint sumNotNull = model.sum(sum, ">", 0);
				Constraint andNull = model.and(sumNull, model.arithm(accCurrent, "=",1));
				Constraint andNotNull = model.and(sumNotNull, model.arithm(accCurrent, "=",0));
				Constraint constraint = model.or(andNull, andNotNull);
				constraint.post();
				//System.out.println(sumNull);
				//System.out.println(sumNotNull);
				//System.out.println(andNull);
				//System.out.println(andNotNull);
				//System.out.println(constraint);

			}

		}

		// same thing but on the control arguments
		for(CArgument arg : controlArgs) {
			// corresponding variables (need acc and on)
			IntVar accCurrent = accVar.get(arg.getName());
			// all its attackers (including AC)
			// ARGUMENTS IN AU, AF, AC
			Set<Argument> attackers = CAF.getControlAttackers(arg, this.completion);
			IntVar accAtt = null;
			IntVar[] sum = new IntVar[attackers.size()];
			int i = 0;
			for(Argument attacker : attackers) {
				accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, adding no constraint
			if(attackers.size()==0) {
				//System.out.println("adding no constraint for : " + accCurrent.getName());
			} else {
				// sumNotNull => accCurrent = 0 <=> sumNull or accCurrent = 0
				//System.out.println("for argument " + accCurrent + " size of attackers = " + attackers.size());
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint setNull = model.arithm(accCurrent, "=",0);
				Constraint constraint = model.or(sumNull, setNull);
				constraint.post();
				//System.out.println(sumNull);
				//System.out.println(setNull);
				//System.out.println(constraint);
			}
		}

		// 4. Solve the problem and return the set of solutions
		Map<StableControlConfiguration, Set<StableExtension>> result = new HashMap<StableControlConfiguration, Set<StableExtension>>();
		Set<StableExtension> extensions = null;
		/*
		StringBuffer temp = new StringBuffer();
		temp.append("##########################################");
		temp.append(System.getProperty("line.separator"));
		temp.append("TARGET:");
		for(CArgument t : T) {
			temp.append(t.getName());
		}
		temp.append(System.getProperty("line.separator"));
		temp.append("for completion: "+ completion.toString());
		temp.append(System.getProperty("line.separator"));
		*/
		while(model.getSolver().solve()) {
			Pair<StableControlConfiguration, StableExtension> solution = this.buildStableExtension(accVar);
			StableControlConfiguration scc = util.Util.find(result.keySet(), solution.getKey());
			/*
			temp.append("solution found");
			temp.append(System.getProperty("line.separator"));
			temp.append(solution.getKey().toString());
			temp.append(System.getProperty("line.separator"));
			*/
			if( scc == null) {
				extensions = new HashSet<StableExtension>();
				extensions.add(solution.getValue());
				result.put(solution.getKey(), extensions);
			} else {
				extensions = result.get(scc);
				extensions.add(solution.getValue());
			}

		} 
		//System.out.println(temp.toString());
		return result;
	}

	/**
	 * once a stable control configuration is calculated
	 * it implies credulous acceptance of protected arguments
	 * We need to perform this check to test if it is skeptically accepted
	 * We test if, with the solution, it is possible to have not all protected arguments accepted
	 * if yes, return false, else return true
	 */
	protected  boolean isScepticallyAccepted(StableControlConfiguration solution) {

		// variables must be stored in a structure for later access
		// acc variables and on variables
		Map<String, IntVar> accVar = new HashMap<String, IntVar>();
		Map<String, IntVar> onVar = new HashMap<String, IntVar>();

		// 1. Create the CSP Model
		Model model = new Model("Skeptical CAF Solver");

		// 2. Create variables
		// one for each argument in the completion (root completion)
		// accepted or not
		Set<Argument> args = this.completion.getAllArguments();
		for(Argument arg : args) {
			String argName = arg.getName();
			IntVar acc = model.intVar("acc_" + argName, new int[]{0,1});
			accVar.put(argName,  acc);
			//System.out.println("adding variable " + acc);
		}

		// two for each control argument
		// accepted (acc) and on 
		Set<CArgument> controlArgs = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		for(CArgument carg : controlArgs) {
			String cargName = carg.getName();
			String onCarg = cargName;
			IntVar acc = model.intVar("acc_" + cargName,new int[]{0,1});
			IntVar on = model.intVar("on_" + onCarg, new int[]{0,1});
			accVar.put(cargName, acc);
			onVar.put(cargName, on);
			//System.out.println("adding variable " + acc);
			//System.out.println("adding variable " + on);

		}

		/*
		 * Constraints
		 */
		// arguments to be protected are not all accepted (i.e. the solution is credulous)
		// Sum(acc < n) with n number of protected arguments
		Set<CArgument> T = CAF.getTarget();
		IntVar[] protectedSum = new IntVar[T.size()];
		int i = 0;
		for(CArgument t : T) {
			IntVar acc = accVar.get(t.getName());
			protectedSum[i] = acc;
			i++;
		}
		model.sum(protectedSum, "<", T.size()).post();

		//on control arguments of solution remain on
		// off control arguments of solution remain off
		// BUT WE DO NOT IMPOSE THAT on control arguments are accepted
		// since we are looking for alternative solutions
		controlArgs = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		Set<CArgument> onArgs = solution.getOnControl();
		for(CArgument arg : controlArgs) {
			String argName = arg.getName();
			IntVar onIt = onVar.get(argName);
			if(onArgs.contains(arg)) {
				model.arithm(onIt, "=",1).post();
			} else {
				model.arithm(onIt, "=",0).post();
			}
		}

		// no two arguments attacking each other in the solution
		// if an argument is accepted (from the root completion or control), all its attackers are rejected
		// else at least one attacker is accepted
		// first we iterate through the root Completion arguments
		for(Argument arg : args) {
			IntVar accCurrent = accVar.get(arg.getName());
			// all its attackers (including AC) : AF, AU, AC
			Set<Argument> attackers = CAF.getArgumentAttackers(this.completion, arg);
			IntVar accAtt = null;
			IntVar[] sum = new IntVar[attackers.size()];
			i = 0;
			for(Argument attacker : attackers) {
				accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, accCurrent=1
			if(attackers.size()==0) {
				//System.out.println("for argument " + accCurrent + " size of attackers = " + attackers.size());
				Constraint constraint = model.arithm(accCurrent, "=", 1);
				constraint.post();
				//System.out.println(constraint);
			} else {
				// here add or(and(sum=0, accCurrent=1), (sum!=0 and accCurrent=0))
				System.out.println("for argument " + accCurrent + " size of attackers = " + attackers.size());
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint sumNotNull = model.sum(sum, ">", 0);
				Constraint andNull = model.and(sumNull, model.arithm(accCurrent, "=",1));
				Constraint andNotNull = model.and(sumNotNull, model.arithm(accCurrent, "=",0));
				Constraint constraint = model.or(andNull, andNotNull);
				constraint.post();
				/*
				System.out.println(sumNull);
				System.out.println(sumNotNull);
				System.out.println(andNull);
				System.out.println(andNotNull);
				System.out.println(constraint);
				*/
			}

		}

		// same thing but on the control arguments
		for(CArgument arg : controlArgs) {
			// corresponding variables (need acc and on)
			IntVar accCurrent = accVar.get(arg.getName());
			// all its attackers (including AC)
			// ARGUMENTS IN AU, AF, AC
			Set<Argument> attackers = CAF.getControlAttackers(arg, this.completion);
			IntVar accAtt = null;
			IntVar[] sum = new IntVar[attackers.size()];
			i = 0;
			for(Argument attacker : attackers) {
				accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, adding no constraint
			if(attackers.size()==0) {
				//System.out.println("adding no constraint for : " + accCurrent.getName());
			} else {
				// sumNotNull => accCurrent = 0 <=> sumNull or accCurrent = 0
				//System.out.println("for argument " + accCurrent + " size of attackers = " + attackers.size());
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint setNull = model.arithm(accCurrent, "=",0);
				Constraint constraint = model.or(sumNull, setNull);
				constraint.post();
				//System.out.println(sumNull);
				//System.out.println(setNull);
				//System.out.println(constraint);
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
	 * builds from the CSP solution (no need of on variables since on is equivalent to accepted)
	 *  a couple <ControlConfiguration, StableExtension> 
	 * Stable extensions will be unique, but control configurations are not.
	 */
	protected Pair<StableControlConfiguration, StableExtension> buildStableExtension(Map<String, IntVar> accVar) {
		StableControlConfiguration scc = new StableControlConfiguration();
		StableExtension se = new StableExtension();
		for(String argName : accVar.keySet()) {
			IntVar accCurrent = accVar.get(argName);
			// all arguments must be accepted or rejected
			if(accCurrent.getValue() ==1) {
				CArgument currentArg = CAF.getArgumentByName(argName);
				if(currentArg == null) {
					throw new UnknownArgumentError("weird, argument " + argName + " is accepted but cannot be found in the CAF");
				} else {
					se.addAccepted(currentArg);
					// here check if control argument and add on (since accepted and on are equal for control arguments)
					if(currentArg.getType() == CArgument.Type.CONTROL) {
						scc.addOnControl(currentArg);
					}
				}
			}
		}
		return new Pair<StableControlConfiguration, StableExtension>(scc,se);
	}

}
