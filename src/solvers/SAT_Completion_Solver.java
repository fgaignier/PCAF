package solvers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

import generators.ControllabilityEncoder;
import generators.SATQDIMACSConverter;
import generators.StrongSATEncoder;
import javafx.util.Pair;
import model.ArgumentFramework;
import model.CArgument;
import model.ControlAF;
import model.StableControlConfiguration;
import model.StableExtension;
import model.UnknownArgumentError;

public class SAT_Completion_Solver implements I_Completion_Solver {

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

	public Map<StableControlConfiguration, Set<StableExtension>> getCredulousControlConfigurations()  {
		try {
			return this.getControlConfigurations(ControllabilityEncoder.CREDULOUS);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Map<StableControlConfiguration, Set<StableExtension>> getSkepticalControlConfigurations()  {
		try {
			return this.getControlConfigurations(ControllabilityEncoder.SKEPTICAL);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * returns a Map of control configurations that credulously/skeptically controls the CAF together
	 * with all the extensions that correspond to each control configuration
	 * type should be ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL
	 * @throws ContradictionException 
	 */
	public Map<StableControlConfiguration, Set<StableExtension>> getControlConfigurations(int type) throws ContradictionException, TimeoutException {

		StrongSATEncoder encoder = new StrongSATEncoder(this.completion, this.CAF);
		SATQDIMACSConverter converter = new SATQDIMACSConverter(encoder.encode(type));
	
		List<int[]> converted = converter.convertToListOfClauses();
		int MAXVAR = converter.getNbVar();
		int NBCLAUSES = converter.getNbClause();

		ISolver solver = SolverFactory.newDefault();

		// prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
		solver.newVar(MAXVAR);
		solver.setExpectedNumberOfClauses(NBCLAUSES);
			
	
		for(int[] clause : converted) {
			solver.addClause(new VecInt(clause));
		}

		// structures to store the solution
		Map<StableControlConfiguration, Set<StableExtension>> result = new HashMap<StableControlConfiguration, Set<StableExtension>>();
		Set<StableExtension> extensions = null;
		// need an iterator to scope the models of the solver.
		ISolver solveriter = new ModelIterator(solver);
		while (solveriter.isSatisfiable()) {
			int [] model = solveriter.model();
			List<String> decoded = converter.decodeModel(model);
			Pair<StableControlConfiguration, StableExtension> solution = this.buildStableExtension(decoded);
			StableControlConfiguration scc = util.Util.find(result.keySet(), solution.getKey());
			if( scc == null) {
				extensions = new HashSet<StableExtension>();
				extensions.add(solution.getValue());
				result.put(solution.getKey(), extensions);
			} else {
				extensions = result.get(scc);
				extensions.add(solution.getValue());
			}	
		}
		return result;
	}
	
	protected Pair<StableControlConfiguration, StableExtension> buildStableExtension(List<String> accepted) {
		StableControlConfiguration scc = new StableControlConfiguration();
		StableExtension se = new StableExtension();
		// stable control configuration scc => variables starting with on_
		// extension => variables starting with acc_
		for(String variable : accepted) {
			if(variable.startsWith("on_")) {
				String argName = variable.replace("on_", "");
				CArgument currentArg = CAF.getArgumentByName(argName);
				if(currentArg == null) {
					throw new UnknownArgumentError("weird, argument " + argName + " is on but cannot be found in the CAF");
				}
				scc.addOnControl(currentArg);
			}
			if(variable.startsWith("acc_")) {
				String argName = variable.replace("acc_", "");
				CArgument currentArg = CAF.getArgumentByName(argName);
				if(currentArg == null) {
					throw new UnknownArgumentError("weird, argument " + argName + " is accepted but cannot be found in the CAF");
				}
				se.addAccepted(currentArg);
			}
		}
		return new Pair<StableControlConfiguration, StableExtension>(scc,se);
	}

}
