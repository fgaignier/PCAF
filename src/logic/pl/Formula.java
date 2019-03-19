package logic.pl;

import java.util.Set;

import generators.QDIMACSBuilder;

public abstract class Formula {
	protected String name ;
	
	public String getName() {
		return name ;
	}
	
	public abstract Set<Atom> getVariables();
	
	public abstract String toQCir();
	
	public abstract String toQDIMACS(QDIMACSBuilder build);
	
}
