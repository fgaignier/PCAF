package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import model.ArgumentFramework;
import model.StableExtension;
import parser.AFParser;
import solvers.CSP_AF_Solver;

public class benchmark_AF_Stable_Ext {

	public static String stats = "calculation.txt";

	public static void claculate_stable_extensions(String path) {
		StringBuffer log = new StringBuffer();
		util.Timer timer = new util.Timer();
		try {
			Stream<Path> names = Files.list(Paths.get(path))
					.filter(Files::isRegularFile);
			names.forEach(n-> {
				log.append(n.toString());
				log.append(System.getProperty("line.separator"));
				ArgumentFramework af = AFParser.parse(n.toString());
				CSP_AF_Solver solver = new CSP_AF_Solver(af);
				timer.start();
				Set<StableExtension> exts = solver.getStableSets();
				log.append("total time: " + timer.stop() + " miliseconds");
				log.append(System.getProperty("line.separator"));
				for(StableExtension se : exts) {
					log.append(se.toString());
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
		claculate_stable_extensions(args[0]);
		
	}
}
