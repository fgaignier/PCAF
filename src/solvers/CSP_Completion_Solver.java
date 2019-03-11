package solvers;

import model.ControlAF;
import model.ArgumentFramework;
import model.CArgument;
import model.Argument;
import model.UnknownArgumentError;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.constraints.*;

/*
 * Class to find a control configuration via a solver
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
	
	public Set<StableControlConfiguration> getSkepticalControlConfigurations() {
		Set<StableControlConfiguration> solutions = this.getCredulousControlConfigurations();
		Set<StableControlConfiguration> result = new HashSet<StableControlConfiguration>();
		Iterator<StableControlConfiguration> solIter = solutions.iterator();
		while(solIter.hasNext()) {
			StableControlConfiguration current = solIter.next(); 
			if(isScepticallyAccepted(current)) {
				result.add(current);
			}
		}
		return result;
	}
	
	/*
	 * returns the set of solutions (control configurations) that credulously control the CAF
	 * according to the protected element list
	 */
	public Set<StableControlConfiguration> getCredulousControlConfigurations() {

		// variables must be stored in a structure for later access
		// acc variables and on variables
		Map<String, IntVar> accVar = new HashMap<String, IntVar>();
		Map<String, IntVar> onVar = new HashMap<String, IntVar>();
		
		// 1. Create the CSP Model
		Model model = new Model("Stable Solver");
		
        // 2. Create variables
		// one for each argument in the completion (root completion)
		// accepted or not
		Set<Argument> args = this.completion.getAllArguments();
		Iterator<Argument> iter = args.iterator();
		while(iter.hasNext()) {
			Argument arg = iter.next();
			String argName = arg.getName();
			IntVar acc = model.intVar("acc_" + argName, new int[]{0,1});
			accVar.put(argName,  acc);
			//System.out.println("adding variable " + acc.getName());
		}
		
		// two for each control argument
		// accepted (acc) and on 
		Set<CArgument> controlArgs = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		Iterator<CArgument> iterC = controlArgs.iterator();
		while(iterC.hasNext()) {
			CArgument carg = iterC.next();
			String cargName = carg.getName();
			String onCarg = cargName;
			IntVar acc = model.intVar("acc_" + cargName,new int[]{0,1});
			IntVar on = model.intVar("on_" + onCarg, new int[]{0,1});
			accVar.put(cargName, acc);
			onVar.put(cargName, on);
			//System.out.println("adding variable " + acc.getName());
			//System.out.println("adding variable " + on.getName());
		}
		
		/*
		 * Constraints
		 */
		// arguments to be protected are accepted (by definition)
		Set<CArgument> T = CAF.getArgumentsToProtect();
		Iterator<CArgument> toP = T.iterator();
		while(toP.hasNext()) {
			CArgument carg = toP.next();
			IntVar acc = accVar.get(carg.getName());
			model.arithm(acc, "=", 1).post();
			//System.out.println("adding constraint: " + acc.getName() + "=1");
		}
		
		
		// accepted control arguments must be on and vice versa
		Set<String> on = onVar.keySet();
		Iterator<String> onArg = on.iterator();
		while(onArg.hasNext()) {
			String argName = onArg.next();
			IntVar onIt = onVar.get(argName);
			IntVar accIt = accVar.get(argName);
			model.arithm(onIt, "-", accIt, "=",0).post();
			//System.out.println("adding constraint: " + onIt.getName() + " - " + accIt.getName() + " =0");
		}
		
		// no two arguments attacking each other in the solution
		// if an argument is accepted (from the root completion or control), all its attackers are rejected
		// else at least one attacker is accepted
		// first we iterate through the root Completion arguments
		Set<Argument> compArgs = this.completion.getAllArguments();
		Iterator<Argument> compArgsIter = compArgs.iterator();
		while(compArgsIter.hasNext()) {
			// the considered argument
			Argument current = compArgsIter.next();
			// corresponding variable
			IntVar accCurrent = accVar.get(current.getName());
			// all its attackers (including AC)
			Set<Argument> attackers = CAF.getArgumentAttackers(this.completion, current);
			Iterator<Argument> attackersIter = attackers.iterator();
			Argument attacker = null;
			IntVar accAtt = null;
			IntVar[] sum = new IntVar[attackers.size()];
			int i = 0;
			//System.out.println("for argument : " + current.getName() + " attackers: " + attackers.size());
			while(attackersIter.hasNext()) {
				// attacker and its corresponding variable
				attacker = attackersIter.next();
				accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, accCurrent=1
			if(attackers.size()==0) {
				model.arithm(accCurrent, "=", 1).post();
				//System.out.println("adding constraint : " + accCurrent.getName() + "= 1");
				
			} else {
				// here add or(and(sum=0, accCurrent=1), (sum!=0 and xi=0))
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint sumNotNull = model.sum(sum, ">", 0);
				Constraint andNull = model.and(sumNull, model.arithm(accCurrent, "=",1));
				Constraint andNotNull = model.and(sumNotNull, model.arithm(accCurrent, "=",0));
				model.or(andNull, andNotNull).post();
				//System.out.println("adding constraint: [sum(" + this.fromTabToString(sum) + ") = 0 and " + accCurrent.getName() + 
				//		"=1] or [sum(" + this.fromTabToString(sum) + ") !=0 and " + accCurrent.getName() + "=0]");
			}
			
		}
		
		// same thing but on the control arguments
		Set<CArgument> controlArguments = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		Iterator<CArgument> controlIter = controlArguments.iterator();
		while(controlIter.hasNext()) {
			// the considered argument
			Argument current = controlIter.next();
			// corresponding variables (need acc and on)
			IntVar accCurrent = accVar.get(current.getName());
			// all its attackers (including AC)
			Set<Argument> attackers = CAF.getControlAttackers(current);
			Iterator<Argument> attackersIter = attackers.iterator();
			Argument attacker = null;
			IntVar accAtt = null;
			IntVar[] sum = new IntVar[attackers.size()];
			int i = 0;
			while(attackersIter.hasNext()) {
				// attacker and its corresponding variable
				attacker = attackersIter.next();
				accAtt = accVar.get(attacker.getName());
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
		Set<StableControlConfiguration> result = new HashSet<StableControlConfiguration>();
		while(model.getSolver().solve()) {
			StableControlConfiguration solution = this.buildResultStable(accVar, onVar);
			result.add(solution);
		} 

        return result;
	}

	/*
	 * once a stable control configuration is calculated
	 * it implies credulous acceptance of protected arguments
	 * We need to perform this check to test if it is skeptically accepted
	 * We test if with the solution it is possible to have not all protected arguments accepted
	 * if yes, return false, else return true
	 */
	protected  boolean isScepticallyAccepted(StableControlConfiguration solution) {

		// variables must be stored in a structure for later access
		// acc variables and on variables
		Map<String, IntVar> accVar = new HashMap<String, IntVar>();
		Map<String, IntVar> onVar = new HashMap<String, IntVar>();
		
		// 1. Create the CSP Model
		Model model = new Model("Skeptical Solver");
		
        // 2. Create variables
		// one for each argument in the completion (root completion)
		// accepted or not
		Set<Argument> args = this.completion.getAllArguments();
		Iterator<Argument> iter = args.iterator();
		while(iter.hasNext()) {
			Argument arg = iter.next();
			String argName = arg.getName();
			IntVar acc = model.intVar("acc_" + argName, new int[]{0,1});
			accVar.put(argName,  acc);
			//System.out.println("adding variable " + acc.getName());
		}
		
		// two for each control argument
		// accepted (acc) and on 
		Set<CArgument> controlArgs = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		Iterator<CArgument> iterC = controlArgs.iterator();
		while(iterC.hasNext()) {
			CArgument carg = iterC.next();
			String cargName = carg.getName();
			String onCarg = cargName;
			IntVar acc = model.intVar("acc_" + cargName,new int[]{0,1});
			IntVar on = model.intVar("on_" + onCarg, new int[]{0,1});
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
		Set<CArgument> T = CAF.getArgumentsToProtect();
		Iterator<CArgument> toP = T.iterator();
		IntVar[] protectedSum = new IntVar[T.size()];
		int i = 0;
		while(toP.hasNext()) {
			CArgument carg = toP.next();
			IntVar acc = accVar.get(carg.getName());
			protectedSum[i] = acc;
			i++;
		}
		model.sum(protectedSum, "<", T.size()).post();
		//System.out.println("adding constraint: " + this.fromTabToString(protectedSum) + "< " + T.size());

		
		// accepted control arguments must be on and vice versa
		Set<String> on = onVar.keySet();
		Iterator<String> onArg = on.iterator();
		while(onArg.hasNext()) {
			String argName = onArg.next();
			IntVar onIt = onVar.get(argName);
			IntVar accIt = accVar.get(argName);
			model.arithm(onIt, "-", accIt, "=",0).post();
			//System.out.println("adding constraint: " + onIt.getName() + " - " + accIt.getName() + " =0");
		}

		// on control arguments of solution remain on
		Set<CArgument> onArgs = solution.getOnControl();
		Iterator<CArgument> onArgsIter = onArgs.iterator();
		while(onArgsIter.hasNext()) {
			String argName = onArgsIter.next().getName();
			IntVar onIt = onVar.get(argName);
			model.arithm(onIt, "=",1).post();
			//System.out.println("adding constraint: " + onIt.getName() +  " =1");
		}
		// no two arguments attacking each other in the solution
		// if an argument is accepted (from the root completion or control), all its attackers are rejected
		// else at least one attacker is accepted
		// first we iterate through the root Completion arguments
		Set<Argument> compArgs = this.completion.getAllArguments();
		Iterator<Argument> compArgsIter = compArgs.iterator();
		while(compArgsIter.hasNext()) {
			// the considered argument
			Argument current = compArgsIter.next();
			// corresponding variable
			IntVar accCurrent = accVar.get(current.getName());
			// all its attackers (including AC)
			Set<Argument> attackers = CAF.getArgumentAttackers(this.completion, current);
			Iterator<Argument> attackersIter = attackers.iterator();
			Argument attacker = null;
			IntVar accAtt = null;
			IntVar[] sum = new IntVar[attackers.size()];
			i = 0;
			//System.out.println("for argument : " + current.getName() + " attackers: " + attackers.size());
			while(attackersIter.hasNext()) {
				// attacker and its corresponding variable
				attacker = attackersIter.next();
				accAtt = accVar.get(attacker.getName());
				sum[i] = accAtt;
				i++;
			}
			// if there are no attackers, accCurrent=1
			if(attackers.size()==0) {
				model.arithm(accCurrent, "=", 1).post();
				//System.out.println("adding constraint : " + accCurrent.getName() + "= 1");
				
			} else {
				// here add or(and(sum=0, accCurrent=1), (sum!=0 and xi=0))
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint sumNotNull = model.sum(sum, ">", 0);
				Constraint andNull = model.and(sumNull, model.arithm(accCurrent, "=",1));
				Constraint andNotNull = model.and(sumNotNull, model.arithm(accCurrent, "=",0));
				model.or(andNull, andNotNull).post();
				//System.out.println("adding constraint: [sum(" + this.fromTabToString(sum) + ") = 0 and " + accCurrent.getName() + 
				//		"=1] or [sum(" + this.fromTabToString(sum) + ") !=0 and " + accCurrent.getName() + "=0]");
			}
			
		}
		
		// same thing but on the control arguments
		Set<CArgument> controlArguments = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		Iterator<CArgument> controlIter = controlArguments.iterator();
		while(controlIter.hasNext()) {
			// the considered argument
			Argument current = controlIter.next();
			// corresponding variables (need acc and on)
			IntVar accCurrent = accVar.get(current.getName());
			// all its attackers (including AC)
			Set<Argument> attackers = CAF.getControlAttackers(current);
			Iterator<Argument> attackersIter = attackers.iterator();
			Argument attacker = null;
			IntVar accAtt = null;
			IntVar[] sum = new IntVar[attackers.size()];
			i = 0;
			while(attackersIter.hasNext()) {
				// attacker and its corresponding variable
				attacker = attackersIter.next();
				accAtt = accVar.get(attacker.getName());
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
				//					"=1] or [sum(" + this.fromTabToString(sum) + ") !=0 and " + accCurrent.getName() + "=0]");
			}
		}
		
		// 4. Solve the problem and return the set of solutions
		
		if(model.getSolver().solve()) {
			return false;
		}  else {
			return true;
		}

	}

	
	/*
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
	
	/*
	 * builds the stable control configuration result of the CSP solution
	 * for internal use only
	 */
	protected StableControlConfiguration buildResultStable(Map<String, IntVar> accVar, Map<String, IntVar> onVar) {
		StableControlConfiguration scc = new StableControlConfiguration();
		Set<String> argNames = accVar.keySet();
		Iterator<String> iterNames = argNames.iterator();
		while(iterNames.hasNext()) {
			String argName = iterNames.next();
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
					// add the argument as accepted
					scc.addAccepted(currentArg);
				}
			}
		}
		
		return scc;
	}
	
	/*
	 * deprecated. StableControlConfiguration.toString() to be used instead
	 */
	protected void printSolution(Map<String, IntVar> accVar, Map<String, IntVar> onVar, int i) {
		Collection<IntVar> stable = accVar.values();
		Iterator<IntVar> iterS = stable.iterator();
		System.out.println("----- solution: " + i + " has been found--------");
		while(iterS.hasNext()) {
			IntVar current = iterS.next();
			if(current.getValue() == 1) {
				System.out.println("accepted " + current.getName() + "=1");
			}
		}
		Collection<IntVar> cont = onVar.values();
		Iterator<IntVar> iterCont = cont.iterator();
		while(iterCont.hasNext()) {
			IntVar current = iterCont.next();
			if(current.getValue() == 1) {
				System.out.println("on control " + current.getName() + "=1");
			}
		}
		System.out.println("----- end solution --------");
	}

	
}
