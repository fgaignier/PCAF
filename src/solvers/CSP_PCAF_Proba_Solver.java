package solvers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.IntVar;

import model.Argument;
import model.ArgumentFramework;
import model.CArgument;
import model.CAttack;
import model.PControlAF;

/**
 * Find completions over a given limit of probability
 * Find the most probable completion
 * for this we have a much better solution implemented in MostProbableRootCompletionGenerator
 * This is just for comparison purposes
 * @author Fabrice
 *
 */
public class CSP_PCAF_Proba_Solver {
	
	protected PControlAF PCAF;
	// need to convert the value to integer
	public static double precision = 0.01;
	
	public CSP_PCAF_Proba_Solver(PControlAF PCAF) {
		this.PCAF = PCAF;
	}
	
	/**
	 * need to convert double logarithms to int values (for variables domains)
	 * @param d
	 * @return
	 */
	public int getLnIntValue(double d) {
		double temp = Math.log(d)*precision;
		return (int) temp;
	}
	
	public Set<ArgumentFramework> getCompletionsOverLimit(double limit) {
		
		// variables must be stored in a structure for later access
		// variables for arguments (uncertain and fixed) and attacks (uncertain and undirected)
		Map<String, IntVar> argVar = new HashMap<String, IntVar>();
		Map<String, IntVar> attackVar = new HashMap<String, IntVar>();
		
		// 1. Create the CSP Model
		Model model = new Model("Over Limit Completion Solver");
		
        // 2. Create variables
		Set<CArgument> uncertainArgs = PCAF.getArgumentsByType(CArgument.Type.UNCERTAIN);
		Set<CArgument> fixedArgs = PCAF.getArgumentsByType(CArgument.Type.FIXED);
		// uncertain arguments
		for(CArgument arg : uncertainArgs) {
			String argName = arg.getName();
			double proba = PCAF.getUargProba(arg);
			IntVar arg_var = model.intVar(argName, new int[]{this.getLnIntValue(proba),this.getLnIntValue(1-proba)});
			argVar.put(argName,  arg_var);
		}
		// fixed arguments
		for(CArgument arg : fixedArgs) {
			String argName = arg.getName();
			IntVar arg_var = model.intVar(argName, 0);
			argVar.put(argName,  arg_var);
		}

		Set<CAttack> uncertainAtts = PCAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Set<CAttack> undirectedAtts = PCAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		//uncertain attacks
		for(CAttack att : uncertainAtts) {
			double proba = PCAF.getUattProba(att);
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar att_var = model.intVar(attName, new int[]{this.getLnIntValue(proba),this.getLnIntValue(1-proba), 0});
			attackVar.put(attName,  att_var);
		}

		//undirected attacks
		for(CAttack att : uncertainAtts) {
			double pd1 = PCAF.getUDAttFromToProba(att);
			double pd2 = PCAF.getUDAttToFromProba(att);
			double pd12 = 1 - pd1 -pd2;
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar att_var = model.intVar(attName, new int[]{this.getLnIntValue(pd1),this.getLnIntValue(pd2), this.getLnIntValue(pd12), 0});
			attackVar.put(attName,  att_var);
		}

		
		/*
		 * Constraints
		 */
		/*
		 * val(dij) < 0 iff val(ai) = ln(p1(ai)) and val(aj)=ln(p1(aj))
		 */
		for(CAttack att : uncertainAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			CArgument argFrom = PCAF.getArgumentByName(att.getFrom().getName());
			CArgument argTo = PCAF.getArgumentByName(att.getTo().getName());
			IntVar dij = attackVar.get(attName);
			IntVar ai = argVar.get(att.getFrom().getName());
			IntVar aj = argVar.get(att.getTo().getName());
			int valai = this.getLnIntValue(PCAF.getUargProba(argFrom));
			int valaj = this.getLnIntValue(PCAF.getUargProba(argTo));
			int total = valai + valaj;
			
			IntVar[] sum = new IntVar[3];
			sum[0] = dij;
			sum[1] = ai;
			sum[2] = aj;
			Constraint modelSum = model.sum(sum, "!=", total);
		}
		
		for(CAttack att : undirectedAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			CArgument argFrom = PCAF.getArgumentByName(att.getFrom().getName());
			CArgument argTo = PCAF.getArgumentByName(att.getTo().getName());
			IntVar dij = attackVar.get(attName);
			IntVar ai = argVar.get(att.getFrom().getName());
			IntVar aj = argVar.get(att.getTo().getName());
			int valai = this.getLnIntValue(PCAF.getUargProba(argFrom));
			int valaj = this.getLnIntValue(PCAF.getUargProba(argTo));
			int total = valai + valaj;
			
			IntVar[] sum = new IntVar[3];
			sum[0] = dij;
			sum[1] = ai;
			sum[2] = aj;
			// add the constraint to the model
			model.sum(sum, "!=", total).post();
		}
	
		// global constraint
		// sum(val(ai)) + sum(val(dij)) >= ln(k) 
		// argument part
		IntVar[] sumaidij = new IntVar[uncertainArgs.size() + uncertainAtts.size() + undirectedAtts.size()];
		int i =0;
		for(CArgument arg : uncertainArgs) {
			IntVar ai = argVar.get(arg.getName());
			sumaidij[i] = ai;
			i++;
		}

		// uncertain attack part
		for(CAttack att : uncertainAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar dij = attackVar.get(attName);
			sumaidij[i] = dij;
			i++;
		}

		// undirected attack part
		for(CAttack att : undirectedAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar dij = attackVar.get(attName);
			sumaidij[i] = dij;
			i++;
		}

		model.sum(sumaidij, ">=", getLnIntValue(limit)).post();
		
		// 4. Solve the problem and return the set of solutions
		Set<ArgumentFramework> result = new HashSet<ArgumentFramework>();
		while(model.getSolver().solve()) {
			ArgumentFramework solution = this.buildAF(argVar, attackVar);
			result.add(solution);
		} 
		return null;
	}
	
	private ArgumentFramework buildAF(Map<String, IntVar> argVar, Map<String, IntVar> attackVar) {
		ArgumentFramework af = new ArgumentFramework();
		// build the structure (fixed part)
		af.addAllCArguments(PCAF.getArgumentsByType(CArgument.Type.FIXED));
		af.addAllCAttacks(PCAF.getAttacksByType(CAttack.Type.CERTAIN));
		
		// add uncertain arguments if it is on
		for(String name : argVar.keySet()) {
			CArgument arg = PCAF.getArgumentByName(name);
			IntVar var = argVar.get(name);
			if(arg.getType() == CArgument.Type.UNCERTAIN) {
				int onValue = this.getLnIntValue(PCAF.getUargProba(arg));
				if(var.getValue() == onValue) {
					af.addArgument(arg);
				}
			}
		}
		
		Set<CAttack> uncertainAtts = PCAF.getAttacksByType(CAttack.Type.UNCERTAIN);
		Set<CAttack> undirectedAtts = PCAF.getAttacksByType(CAttack.Type.UNDIRECTED);
		
		for(CAttack att: uncertainAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar var = attackVar.get(attName);
			int onValue = this.getLnIntValue(PCAF.getUattProba(att));
			if(var.getValue() == onValue) {
				af.addAttack(att);
			}
		}

		for(CAttack att: undirectedAtts) {
			String attName = att.getFrom().getName() + "_" + att.getTo().getName();
			IntVar var = attackVar.get(attName);
			int dir1Value = this.getLnIntValue(PCAF.getUDattProba(att).getKey().doubleValue());
			int dir2Value = this.getLnIntValue(PCAF.getUDattProba(att).getValue().doubleValue());
			CAttack reverse = new CAttack(att.getTo(), att.getFrom(), CAttack.Type.UNDIRECTED);
			if(var.getValue() == dir1Value) {
				af.addAttack(att);
			} else if(var.getValue() == dir2Value) {
				af.addAttack(reverse);
			} else {
				af.addAttack(att);
				af.addAttack(reverse);
			}
		}

		return af;
	}
	
}
