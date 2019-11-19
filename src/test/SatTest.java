package test;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import org.sat4j.tools.ModelIterator;
import org.sat4j.core.VecInt;

public class SatTest {

	public static void main(String[] args) {
		final int MAXVAR = 2;
		final int NBCLAUSES = 2;

		ISolver solver = SolverFactory.newDefault();

		// prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
		solver.newVar(MAXVAR);
		solver.setExpectedNumberOfClauses(NBCLAUSES);
		// CNF => each clause is a OR
		// all clauses are linked by a AND
		int [] clause1 = {1, 2};
		int [] clause2 = {-1,2};

		try {
			solver.addClause(new VecInt(clause1));
			solver.addClause(new VecInt(clause2));

			// need an iterator to scope the models of the solver.
			ISolver solveriter = new ModelIterator(solver);
			while (solveriter.isSatisfiable()) {
				System.out.println("There are solutions");
				int [] model = solveriter.model();
				System.out.println(model[0]);
				System.out.println(model[1]);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
