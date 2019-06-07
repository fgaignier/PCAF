package solvers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import model.Argument;
import model.ArgumentFramework;
import model.StableExtension;
import model.UnknownArgumentError;

/**
 * This class enables the determination of stables extensions
 * of the Argument Framework passed at construction.
 * @author Fabrice
 *
 */
public class CSP_AF_Solver {
	
	protected ArgumentFramework af;
	
	public CSP_AF_Solver(ArgumentFramework af) {
		this.af = af;
	}

	public ArgumentFramework getAF() {
		return af;
	}


	public void setAF(ArgumentFramework af) {
		this.af = af;
	}
	
	/**
	 * returns the set of solutions (stable sets) for the AF
	 * solved with a Constraint Satisfaction Constraints solver
	 * one variable per argument (accepted or not)
	 * Constraints:
	 * all attackers of an argument x have accepted=0 and accepted(x) = 1
	 * or
	 *  at least on atacker of x has accepted = 1 and accepted(x) = 0
	 */
	public Set<StableExtension> getStableSets() {

		// variables must be stored in a structure for later access
		// acc variables
		Map<String, IntVar> accVar = new HashMap<String, IntVar>();
		
		// 1. Create the CSP Model
		Model model = new Model("Credulous AF Solver");
		
        // 2. Create variables
		// one for each argument in the completion (root completion)
		// accepted or not
		Set<Argument> args = this.af.getAllArguments();
		Iterator<Argument> iter = args.iterator();
		while(iter.hasNext()) {
			Argument arg = iter.next();
			String argName = arg.getName();
			IntVar acc = model.intVar("acc_" + argName, new int[]{0,1});
			accVar.put(argName,  acc);
			//System.out.println("adding variable " + acc);
		}
				
		/*
		 * Constraints
		 */
		
		// no two arguments attacking each other in the solution
		// if an argument is accepted, all its attackers are rejected
		// else at least one attacker is accepted
		iter = args.iterator();
		while(iter.hasNext()) {
			// the considered argument
			Argument current = iter.next();
			// corresponding variable
			IntVar accCurrent = accVar.get(current.getName());
			// all its attackers (including AC)
			Set<Argument> attackers = af.getAttackingArguments(current);
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
				/*
				System.out.println(sumNull);
				System.out.println(sumNotNull);
				System.out.println(andNull);
				System.out.println(andNotNull);
				System.out.println(constraint);
				*/
			}
			
		}
		
		// 4. Solve the problem and return the set of solutions
		Set<StableExtension> result = new HashSet<StableExtension>();
		while(model.getSolver().solve()) {
			StableExtension solution = this.buildResultStable(accVar);
			result.add(solution);
		} 

        return result;
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
	protected StableExtension buildResultStable(Map<String, IntVar> accVar) {
		StableExtension scc = new StableExtension();
		Set<String> argNames = accVar.keySet();
		Iterator<String> iterNames = argNames.iterator();
		while(iterNames.hasNext()) {
			String argName = iterNames.next();
			//System.out.println(argName);
			IntVar accCurrent = accVar.get(argName);
			// all arguments must be accepted or rejected
			if(accCurrent.getValue() ==1) {
				Argument currentArg = af.getArgumentByName(argName);
				if(currentArg == null) {
					throw new UnknownArgumentError("weird, argument " + argName + " is accepted but cannot be found in the CAF");
				} 
				scc.addAccepted(currentArg);
			}
		}
		return scc;
	}

}
