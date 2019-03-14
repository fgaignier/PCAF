package generators;

import logic.qbf.QBFFormula;
import model.ControlAF;

public abstract class ControllabilityEncoder {
	protected ControlAF instance ;
	
	public abstract QBFFormula encode();
}
