package test;

import model.ArgumentFramework;
import model.ControlAF;
import tests.test_CAF;
import generators.RandomCAFRootCompletionGenerator;

public class SatToQDIMACSTest {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("usage: filename");
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
		
		caftest.saveSATQDIMACSToFile(path + "satencoding.txt", instance);
		
	}
}