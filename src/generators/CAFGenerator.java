package generators;

import model.ArgumentFramework;
import model.ControlAF;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import model.Argument;
import model.CArgument;
import model.Attack;
import model.CAttack;
import util.RandomGen;

/**
 * Given an argument framework, this class will generate 
 * CAFs according to the proportion of uncertain and undirected elements given.
 * Proportion of control arguments = 1 - pArgF - pArgU
 * Proportion of undirected attacks = 1 - pAttF - pAttU
 *  
 * @author Fabrice
 *
 */
public class CAFGenerator {
	private ArgumentFramework af;
	/**
	 * Probability that an argument from af is fixed
	 */
	private int pArgF;
	/**
	 * Probability that an argument from af is uncertain
	 */
	private int pArgU;
	/**
	 * Probability that an attack from af is fixed
	 */
	private int pAttF ;
	/**
	 * Probability that an attack from af is uncertain
	 */
	private int pAttU ;

	/**
	 * ALL IN % INTEGER VALUES
	 * Control arguments and undirected attacks will be deduced from the other probabilities 
	 * @param af the original argument framework
	 * @param pArgF proportion of fixed argument
	 * @param pArgU proportion of uncertain argument
	 * @param pAttF proportion of fixed attacks
	 * @param pAttU proportion of uncertain attacks
	 */
	public CAFGenerator(ArgumentFramework af, int pArgF, int pArgU, int pAttF, int pAttU) {
		this.af = af;
		this.pArgF = pArgF;
		this.pArgU = pArgU;
		if (pArgF < 0) {
			throw new IllegalArgumentException("The probability " + pArgF + " is negative.");
		}
		if (pArgU < 0) {
			throw new IllegalArgumentException("The probability " + pArgU + " is negative.");
		}
		if (pArgF + pArgU > 100) {
			throw new IllegalArgumentException("The sum of " + pArgF + " and " + pArgU + " is greater than 100.");
		}
		this.pAttF = pAttF ;
		this.pAttU = pAttU ;
		if (pAttF < 0) {
			throw new IllegalArgumentException("The probability " + pAttF + " is negative.");
		}
		if (pAttU < 0) {
			throw new IllegalArgumentException("The probability " + pAttU + " is negative.");
		}
		if (pAttF + pAttU > 100) {
			throw new IllegalArgumentException("The sum of " + pAttF + " and " + pAttU + " is greater than 100.");
		}
	}

	/**
	 * generate randomly a CAF according to the proportions given in the constructor
	 * if it is not possible to build a target of the right size, returns null
	 * @param target_size size of the target
	 * @return CAF if target size can be met. null else
	 */
	public ControlAF generate(int target_size) {
		ControlAF caf = new ControlAF();

		for(Argument arg : af.getAllArguments()) {
			int val = RandomGen.getProba();
			CArgument carg = null;
			if(val <= pArgF) {
				carg = new CArgument(arg.getName(),CArgument.Type.FIXED);
			}else if(val <= pArgF + pArgU) {
				carg = new CArgument(arg.getName(),CArgument.Type.UNCERTAIN);
			}else {
				carg = new CArgument(arg.getName(),CArgument.Type.CONTROL);
			}
			caf.addArgument(carg);
		}
		for(Attack att : af.getAllAttacks()) {
			CArgument from = caf.getArgumentByName(att.getFrom().getName());
			CArgument to = caf.getArgumentByName(att.getTo().getName());
			CAttack  catt = null;
			if(from.getType() == CArgument.Type.CONTROL || to.getType() == CArgument.Type.CONTROL) {
				catt = new CAttack(from, to, CAttack.Type.CONTROL);
			}else {
				int val = RandomGen.getProba();
				if(val <= pAttF) {
					catt = new CAttack(from, to, CAttack.Type.CERTAIN);
				}else if(val <= pAttF + pAttU) {
					catt =new CAttack(from, to, CAttack.Type.UNCERTAIN);
				}else {
					catt = new CAttack(from, to, CAttack.Type.UNDIRECTED);
				}
			}
			caf.addAttack(catt);
		}
		if(target_size == 1) {
			CArgument target = this.getUniqueTraget(caf);
			if(target != null) {
				caf.addTarget(target);
			} else {return null;}
		} else {
			Set<CArgument> target = this.buildTarget(caf, target_size);
			if(target != null) {
				caf.setTarget(target);
			} else {return null;}
		}
		return caf;
	}

	/**
	 * selects randomly one fixed argument as a target
	 * it is more complex if we want to add several arguments as target since they must be conflict free 
	 */
	private CArgument getUniqueTraget(ControlAF caf) {
		Set<CArgument> fixed = caf.getArgumentsByType(CArgument.Type.FIXED);
		CArgument[] array = fixed.toArray(new CArgument[0]);
		int max = array.length;
		if(max == 0) {
			return null;
		}
		int random_pos = util.RandomGen.randInt(0, max-1);
		return array[random_pos];
	}

	/**
	 * use of a CSP to build a target of size > 1
	 * since we want to return only a set of arguments that are never conflicting
	 * so we want to avoid any kind of atack between the arguments of this set
	 * @param caf the caf
	 * @param target_size target size
	 * @return the target with non conflicting arguments of the right size or null if not possible
	 */
	private Set<CArgument> buildTarget(ControlAF caf, int target_size) {
		
		// variables must be stored in a structure for later access
		// acc variables and on variables
		Map<String, IntVar> onVar = new HashMap<String, IntVar>();

		// 1. Create the CSP Model
		Model model = new Model("Target builder");

		// 2. Create variables
		// one for each fixed argument in the caf
		Set<CArgument> fixed = caf.getArgumentsByType(CArgument.Type.FIXED);
		// if there is not enough fixed arguments, no need to search
		if(fixed.size() < target_size) {
			return null;
		}
		
		for(CArgument arg : fixed) {
			String argName = arg.getName();
			IntVar on = model.intVar("on_" + argName, new int[]{0,1});
			onVar.put(argName,  on);
			//System.out.println("adding variable " + on.getName());
		}

		/*
		 * Constraints:
		 * There are two constraints
		 * 1) for each argument, if it is on then all its attackers are not on. Else it is not on
		 * 2) the sum of on variables >= target_size 
		 * we will then take a random subset of the solution
		 */
		// acceptability
		for(CArgument arg : fixed) {
			IntVar onArg = onVar.get(arg.getName());
			Set<CArgument> fixed_attackers = caf.getPotentialFixedAttackers(arg);
			IntVar[] sum = new IntVar[fixed_attackers.size()];
			if(fixed_attackers.size() == 0) {
				model.arithm(onArg, "=", 1).post();
			} else {
				int i=0;
				for(CArgument attacker : fixed_attackers) {
					sum[i] = onVar.get(attacker.getName());
					i++;
				}
				Constraint sumNull = model.sum(sum, "=", 0);
				Constraint sumNotNull = model.sum(sum, ">", 0);
				Constraint andNull = model.and(sumNull, model.arithm(onArg, "=",1));
				Constraint andNotNull = model.and(sumNotNull, model.arithm(onArg, "=",0));
				model.or(andNull, andNotNull).post();
			}
		}
		// total
		IntVar[] totalsum = new IntVar[fixed.size()];
		int i=0;
		for(CArgument arg : fixed) {
			IntVar onArg = onVar.get(arg.getName());
			totalsum[i] = onArg;
			i++;
		}
		model.sum(totalsum, ">=", target_size).post();

		// take the first possible solution
		if(model.getSolver().solve()) {
			return makeTarget(onVar, caf, target_size);
		} 
		return null;
	}

	private Set<CArgument> makeTarget(Map<String, IntVar> onVar, ControlAF caf, int target_size) {
		Set<CArgument> result = new HashSet<CArgument>();
		int i = 0;
		for(String arg_name : onVar.keySet()) {
			IntVar on = onVar.get(arg_name);
			if(on.getValue() ==1) {
				result.add(caf.getArgumentByName(arg_name));
				i++;
				if(i == target_size) {
					return result;
				}
			}
		}
		return result;
	}
}
