package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

import model.CArgument;
import model.ControlAF;
import model.CAttack;
import model.UnknownArgumentError;

public class CAFParser {
	
	public static String FIXED_ARG = "f_arg";
	public static String UNCERTAIN_ARG = "u_arg";
	public static String CONTROL_ARG = "c_arg";
	public static String FIXED_ATT = "att";
	public static String UNCERTAIN_ATT = "u_att";
	public static String UNDIRECTED_ATT = "ud_att";
	public static String CONTROL_ATT = "att";
	public static String TARGET = "target";
	
	/*
	private String filename;

	public CAFParser(String string) {
		filename = string;
	}
	*/
	
	/**
	 * This method will load a CAF from a file and return an instance of CAF
	 * The syntax of the file is described below.
	 * all ARGUMENTS must be defined first, else the attacks cannot be constructed
	 */
	public static ControlAF parse(String filename) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			ControlAF instance = new ControlAF();
			String currentLine;
			CArgument carg;
			Map<String, CArgument> loadedArguments = new HashMap<String, CArgument>();
			
			while ((currentLine = br.readLine()) != null) {
				if (currentLine.startsWith(CAFParser.FIXED_ARG)) {
					String tmp = currentLine.split("\\(")[1];
					String arg = tmp.split("\\)")[0].trim();
					carg = new CArgument(arg, CArgument.Type.FIXED);
					instance.addArgument(carg);
					loadedArguments.put(arg, carg);
				} else if (currentLine.startsWith(CAFParser.UNCERTAIN_ARG)) {
					String tmp = currentLine.split("\\(")[1];
					String arg = tmp.split("\\)")[0].trim();
					carg = new CArgument(arg, CArgument.Type.UNCERTAIN);
					instance.addArgument(carg);
					loadedArguments.put(arg, carg);
				} else if (currentLine.startsWith(CAFParser.CONTROL_ARG)) {
					String tmp = currentLine.split("\\(")[1];
					String arg = tmp.split("\\)")[0].trim();
					carg = new CArgument(arg, CArgument.Type.CONTROL);
					instance.addArgument(carg);
					loadedArguments.put(arg, carg);
				} else if (currentLine.startsWith(CAFParser.FIXED_ATT)) {
					String tmp = currentLine.split("\\(")[1];
					String tmp2 = tmp.split("\\)")[0];
					String[] tab = tmp2.split(",");
					CArgument argFrom = loadedArguments.get(tab[0].trim());
					CArgument argTo = loadedArguments.get(tab[1].trim());
					if(argFrom == null | argTo == null) {
						throw new UnknownArgumentError("cannot find argument from or to in this attack: (" + tab[0] + "," + tab[1] + ")");
					}
					if(argFrom.getType() == CArgument.Type.CONTROL || argTo.getType() == CArgument.Type.CONTROL) {
						instance.addAttack(new CAttack(argFrom, argTo, CAttack.Type.CONTROL));
					} else {
						instance.addAttack(new CAttack(argFrom, argTo, CAttack.Type.CERTAIN));
					}
				} else if (currentLine.startsWith(CAFParser.UNCERTAIN_ATT)) {
					String tmp = currentLine.split("\\(")[1];
					String tmp2 = tmp.split("\\)")[0];
					String[] tab = tmp2.split(",");
					CArgument argFrom = loadedArguments.get(tab[0].trim());
					CArgument argTo = loadedArguments.get(tab[1].trim());
					if(argFrom == null | argTo == null) {
						throw new UnknownArgumentError("cannot find argument from or to in this attack: (" + tab[0] + "," + tab[1] + ")");
					}
					instance.addAttack(new CAttack(argFrom, argTo, CAttack.Type.UNCERTAIN));
				} else if (currentLine.startsWith(CAFParser.UNDIRECTED_ATT)) {
					String tmp = currentLine.split("\\(")[1];
					String tmp2 = tmp.split("\\)")[0];
					String[] tab = tmp2.split(",");
					CArgument argFrom = loadedArguments.get(tab[0].trim());
					CArgument argTo = loadedArguments.get(tab[1].trim());
					if(argFrom == null | argTo == null) {
						throw new UnknownArgumentError("cannot find argument from or to in this attack: (" + tab[0] + "," + tab[1] + ")");
					}
					instance.addAttack(new CAttack(argFrom, argTo, CAttack.Type.UNDIRECTED));
				}else if(currentLine.startsWith(CAFParser.TARGET)) {
					String tmp = currentLine.split("\\(")[1];
					String arg = tmp.split("\\)")[0].trim();
					//carg = new CArgument(arg, CArgument.Type.FIXED);
					carg = loadedArguments.get(arg);
					instance.addTarget(carg);
				}
			}

			return instance;
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new UnsupportedOperationException();
	}

}
