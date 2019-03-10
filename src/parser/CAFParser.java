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
	private String filename;

	public CAFParser(String string) {
		filename = string;
	}

	/*
	 * This method will load a CAF from a file and return an instance of CAF
	 * The syntax of the file is described below.
	 * all ARGUMENTS must be defined first, else the attacks cannot be constructed
	 */
	public ControlAF parse() {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			ControlAF instance = new ControlAF();
			String currentLine;
			CArgument carg;
			Map<String, CArgument> loadedArguments = new HashMap<String, CArgument>();
			
			while ((currentLine = br.readLine()) != null) {
				if (currentLine.startsWith("f_arg")) {
					String tmp = currentLine.split("\\(")[1];
					String arg = tmp.split("\\)")[0];
					carg = new CArgument(arg, CArgument.Type.FIXED);
					instance.addArgument(carg);
					loadedArguments.put(arg, carg);
				} else if (currentLine.startsWith("u_arg")) {
					String tmp = currentLine.split("\\(")[1];
					String arg = tmp.split("\\)")[0];
					carg = new CArgument(arg, CArgument.Type.UNCERTAIN);
					instance.addArgument(carg);
					loadedArguments.put(arg, carg);
				} else if (currentLine.startsWith("c_arg")) {
					String tmp = currentLine.split("\\(")[1];
					String arg = tmp.split("\\)")[0];
					carg = new CArgument(arg, CArgument.Type.CONTROL);
					instance.addArgument(carg);
					loadedArguments.put(arg, carg);
				} else if (currentLine.startsWith("att")) {
					String tmp = currentLine.split("\\(")[1];
					String tmp2 = tmp.split("\\)")[0];
					String[] tab = tmp2.split(",");
					CArgument argFrom = loadedArguments.get(tab[0]);
					CArgument argTo = loadedArguments.get(tab[1]);
					if(argFrom == null | argTo == null) {
						throw new UnknownArgumentError("cannot find argument from or to in this attack: (" + tab[0] + "," + tab[1] + ")");
					}
					instance.addAttack(new CAttack(argFrom, argTo, CAttack.Type.CERTAIN));
				} else if (currentLine.startsWith("u_att")) {
					String tmp = currentLine.split("\\(")[1];
					String tmp2 = tmp.split("\\)")[0];
					String[] tab = tmp2.split(",");
					CArgument argFrom = loadedArguments.get(tab[0]);
					CArgument argTo = loadedArguments.get(tab[1]);
					if(argFrom == null | argTo == null) {
						throw new UnknownArgumentError("cannot find argument from or to in this attack: (" + tab[0] + "," + tab[1] + ")");
					}
					instance.addAttack(new CAttack(argFrom, argTo, CAttack.Type.UNCERTAIN));
				} else if (currentLine.startsWith("ud_att")) {
					String tmp = currentLine.split("\\(")[1];
					String tmp2 = tmp.split("\\)")[0];
					String[] tab = tmp2.split(",");
					CArgument argFrom = loadedArguments.get(tab[0]);
					CArgument argTo = loadedArguments.get(tab[1]);
					if(argFrom == null | argTo == null) {
						throw new UnknownArgumentError("cannot find argument from or to in this attack: (" + tab[0] + "," + tab[1] + ")");
					}
					instance.addAttack(new CAttack(argFrom, argTo, CAttack.Type.UNDIRECTED));
				} else if (currentLine.startsWith("c_att")) {
					String tmp = currentLine.split("\\(")[1];
					String tmp2 = tmp.split("\\)")[0];
					String[] tab = tmp2.split(",");
					CArgument argFrom = loadedArguments.get(tab[0]);
					CArgument argTo = loadedArguments.get(tab[1]);
					if(argFrom == null | argTo == null) {
						throw new UnknownArgumentError("cannot find argument from or to in this attack: (" + tab[0] + "," + tab[1] + ")");
					}
					instance.addAttack(new CAttack(argFrom, argTo, CAttack.Type.CONTROL));
				}else if(currentLine.startsWith("target")) {
					String tmp = currentLine.split("\\(")[1];
					String arg = tmp.split("\\)")[0];
					carg = new CArgument(arg, CArgument.Type.FIXED);
					instance.addProtectedArgument(carg);
				}
			}

			return instance;
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new UnsupportedOperationException();
	}

}
