package generators;

import logic.qbf.QBFFormula;
import model.ControlAF;

public abstract class ControllabilityEncoder {
	public final static int CREDULOUS = 0;
	public final static int SKEPTICAL = 1;
	
	protected ControlAF instance ;
	
	public abstract QBFFormula encode(int type);
}
