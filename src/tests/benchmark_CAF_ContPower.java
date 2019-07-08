package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import generators.ControllabilityEncoder;
import model.ControlAF;
import model.PControlAF;
import model.StableControlConfiguration;
import parser.CAFParser;
import solvers.Most_Probable_Controlling_Entities_Solver;

/**
 * must indicate the error level
 * must indicate CREDULOUS vs SKEPTICAL
 * @author Fabrice Gaignier
 *
 */
public class benchmark_CAF_ContPower {
	public static String log_file = "controllingpower.txt";
	public static String stats_file = "controllingpower_stats.csv";
	public static String prefixCred = "CRE";
	public static String prefixSkep = "SKE";
	public static double error = 0.01;
	public static String csv_sep = ";";

	public static void claculate_mpce(String path, int type) {
		StringBuffer log = new StringBuffer();
		StringBuffer stats_csv = new StringBuffer();
		stats_csv.append(getHeader());
		util.Timer timer = new util.Timer();
		try {
			Stream<Path> names = Files.list(Paths.get(path))
					.filter(Files::isRegularFile);
			names.forEach(n-> {
				System.out.println(n.toString());
				log.append(n.toString());
				log.append(System.getProperty("line.separator"));
				stats_csv.append(n.toString());
				stats_csv.append(csv_sep);
				ControlAF caf = CAFParser.parse(n.toString());
				// direct transformation to pcaf to get the mpce if no cc available
				PControlAF pcaf = new PControlAF(caf);
				Most_Probable_Controlling_Entities_Solver solver = new Most_Probable_Controlling_Entities_Solver(pcaf);
				
				Set<StableControlConfiguration> mpces = null;
				//STARTS THE TIMER JUST BEFORE CALCULATION
				timer.start();
				if(type == ControllabilityEncoder.CREDULOUS) {
					mpces = solver.getCredulousControlConfigurations(error);
				} else {
					mpces = solver.getSkepticalControlConfigurations(error);
				}
				// STOPS THE TIMER AND GET CALCULATION TIME
				long time = timer.stop();
				
				// log
				log.append("total time: " + time + " miliseconds");
				log.append(System.getProperty("line.separator"));
				log.append("controlling power = " + solver.getControllingPower());
				log.append(System.getProperty("line.separator"));
				log.append("total number of simulations = " + solver.getNumberSimu());
				System.out.println("controlling power = " + solver.getControllingPower());
				log.append(System.getProperty("line.separator"));

				// stats
				stats_csv.append(caf.getTargetSize());
				stats_csv.append(csv_sep);
				stats_csv.append(solver.getControllingPower());
				stats_csv.append(csv_sep);
				stats_csv.append(solver.getNumberSimu());
				stats_csv.append(csv_sep);
				stats_csv.append(time);
				stats_csv.append(csv_sep);
				
				int i=1;
				for(StableControlConfiguration mpce : mpces) {
					log.append("######### mpce " + i + " ###########");
					log.append(System.getProperty("line.separator"));
					log.append(mpce.toString());
					log.append(System.getProperty("line.separator"));
					
					stats_csv.append(mpce.toString());
					stats_csv.append(csv_sep);
					
					i++;
				}
				log.append("####################");
				log.append(System.getProperty("line.separator"));
				
				stats_csv.append(System.getProperty("line.separator"));
				
			});
			
			if(type == ControllabilityEncoder.CREDULOUS) {
				util.Util.saveToFile(log.toString(), path + "\\result\\" + prefixCred + log_file);
				util.Util.saveToFile(stats_csv.toString(), path + "\\result\\" + prefixCred + stats_file);
			} else {
				util.Util.saveToFile(log.toString(), path + "\\result\\" + prefixSkep + log_file);
				util.Util.saveToFile(stats_csv.toString(), path + "\\result\\" + prefixSkep + stats_file);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getHeader() {
		StringBuffer result = new StringBuffer();
		result.append("file");
		result.append(csv_sep);
		result.append("target size");
		result.append(csv_sep);
		result.append("power");
		result.append(csv_sep);
		result.append("simu");
		result.append(csv_sep);
		result.append("time");
		result.append(System.getProperty("line.separator"));
		return result.toString();
	}
	public static void main(String[] args) {
		if(args.length < 2) {
			System.out.println("parameters: directory type (0=CREDULOUS, 1=SKEPTICAL)");
			System.exit(1);
		}
		claculate_mpce(args[0], Integer.parseInt(args[1]));
		
	}
}
