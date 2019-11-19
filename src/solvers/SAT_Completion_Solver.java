package solvers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import model.Argument;
import model.ArgumentFramework;
import model.CArgument;
import model.ControlAF;
import model.StableControlConfiguration;
import model.StableExtension;

public class SAT_Completion_Solver {

	protected ControlAF CAF;
	protected ArgumentFramework completion;

	public SAT_Completion_Solver(ControlAF CAF, ArgumentFramework completion) {
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
	 * returns a Map of control configurations that credulously control the CAF together
	 * with all the extensions that correspond to each control configuration
	 * @throws ContradictionException 
	 */
	/*
	public Map<StableControlConfiguration, Set<StableExtension>> getCredulousControlConfigurations() throws ContradictionException {
		// variables must be assigned an Integer
		// acc variables and on variables
		Map<String, Integer> accVar = new HashMap<String, Integer>();
		Map<String, Integer> onVar = new HashMap<String, Integer>();

		// 2. Create variables
		// one for each argument in the completion (root completion)
		// accepted or not
		Set<Argument> args = this.completion.getAllArguments();
		int accCounter =1;
		for(Argument arg : args) {
			String argName = arg.getName();
			accVar.put(argName, new Integer(accCounter));
			accCounter ++;
		}

		// two for each control argument
		// accepted (acc) and on 
		Set<CArgument> controlArgs = CAF.getArgumentsByType(CArgument.Type.CONTROL);
		int onCounter = 1;
		for(Argument carg : controlArgs) {
			String cargName = carg.getName();
			accVar.put(cargName, new Integer(accCounter));
			onVar.put(cargName, new Integer(onCounter));
			accCounter ++;
			onCounter++;
		}

		// gets a new solver
		ISolver solver = SolverFactory.newDefault();
		
		// building the CNF formula
		// all arguments in the target have acc = True
		Set<CArgument> T = CAF.getTarget();
		for(CArgument t : T) {
			Integer acc = accVar.get(t.getName());
			int[] clause = {acc};
			solver.addClause(new VecInt(clause));
		}
		
		// all control arguments have acc <=> on
		// acc <=> on transforms in
		// (not(acc) or on) and (not(on) or acc)
		Set<String> on = onVar.keySet();
		for(String argName : on) {
			Integer onA = onVar.get(argName);
			Integer accA = accVar.get(argName);
			int[] clause1 = {-1*accA.intValue(),onA.intValue()};
			int[] clause2 = {accA.intValue(),-1*onA.intValue()};
			solver.addClause(new VecInt(clause1));
			solver.addClause(new VecInt(clause2));
		}
		
		
	} */

}
