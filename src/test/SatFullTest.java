package test;

import model.ArgumentFramework;
import model.ControlAF;
import tests.test_CAF;
import generators.RandomCAFRootCompletionGenerator;
import generators.SATQDIMACSConverter;
import generators.StrongSATEncoder;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.*;
import org.sat4j.tools.ModelIterator;

import java.util.List;

import org.sat4j.core.VecInt;

public class SatFullTest {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("usage: path, filename");
			System.exit(1);
		}
		String path = args[0];
		String file_name = args[1];
		
		test_CAF caftest = new test_CAF();
		caftest.load_CAF_from_file(path+file_name);
		ControlAF CAF = caftest.getCAF();
		
		// need an instance of this CAF
		RandomCAFRootCompletionGenerator generator = new RandomCAFRootCompletionGenerator(CAF);
		ArgumentFramework instance = generator.getMaxRootCompletion();
		
		StrongSATEncoder encoder = new StrongSATEncoder(instance, CAF);
		SATQDIMACSConverter converter = new SATQDIMACSConverter(encoder.encode(0));
		
		List<int[]> converted = converter.convertToListOfClauses();
		int MAXVAR = converter.getNbVar();
		int NBCLAUSES = converter.getNbClause();

		ISolver solver = SolverFactory.newDefault();

		// prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
		solver.newVar(MAXVAR);
		solver.setExpectedNumberOfClauses(NBCLAUSES);
				
		try {
			for(int[] clause : converted) {
				solver.addClause(new VecInt(clause));
			}

			// need an iterator to scope the models of the solver.
			ISolver solveriter = new ModelIterator(solver);
			while (solveriter.isSatisfiable()) {
				System.out.println("There are solutions");
				int [] model = solveriter.model();
				List<String> decoded = converter.decodeModel(model);
				
				for(String s : decoded) {
					System.out.println(s);
				}
				/*
				for(int i=0; i< model.length; i++) {
					System.out.println(model[i]);
				} */
				
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
