package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import generators.ControllabilityEncoder;
import model.ArgumentFramework;
import model.ControlAF;
import model.PControlAF;
import model.StableControlConfiguration;
import model.StableExtension;
import parser.AFParser;
import parser.CAFParser;
import solvers.CSP_AF_Solver;
import solvers.Monte_Carlo_CAF_Solver;
import solvers.Most_Probable_Controlling_Entities_Solver;

/**
 * must indicate the error level
 * must indicate CREDULOUS vs SKEPTICAL
 * @author Fabrice
 *
 */
public class benchmark_CAF_Cred_Controllability {
	public static String stats = "benchmark.txt";
//	public static int acceptance_type = ControllabilityEncoder.CREDULOUS;
	public static double error = 0.01;

	public static void claculate_mpce(String path) {
		StringBuffer log = new StringBuffer();
		util.Timer timer = new util.Timer();
		try {
			Stream<Path> names = Files.list(Paths.get(path))
					.filter(Files::isRegularFile);
			names.forEach(n-> {
				System.out.println(n.toString());
				log.append(n.toString());
				log.append(System.getProperty("line.separator"));
				ControlAF caf = CAFParser.parse(n.toString());
				// direct transformation to pcaf to get the mpce if no cc available
				PControlAF pcaf = new PControlAF(caf);
				Most_Probable_Controlling_Entities_Solver solver = new Most_Probable_Controlling_Entities_Solver(pcaf);
				timer.start();
				Set<StableControlConfiguration> mpces = solver.getCredulousControlConfigurations(error);
				log.append("total time: " + timer.stop() + " miliseconds");
				log.append(System.getProperty("line.separator"));
				log.append("controlling power = " + solver.getControllingPower());
				log.append(System.getProperty("line.separator"));
				System.out.println("controlling power = " + solver.getControllingPower());
				for(StableControlConfiguration mpce : mpces) {
					log.append(mpce.toString());
					log.append(System.getProperty("line.separator"));
					log.append("####################");
					log.append(System.getProperty("line.separator"));
				}
			});
			util.Util.saveToFile(log.toString(), path + "\\" + stats);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("must give the target directory as parameter");
			System.exit(1);
		}
		claculate_mpce(args[0]);
		
	}
}
