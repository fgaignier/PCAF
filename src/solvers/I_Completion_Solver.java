package solvers;

import java.util.Map;
import java.util.Set;

import model.ArgumentFramework;
import model.ControlAF;
import model.StableControlConfiguration;
import model.StableExtension;

public interface I_Completion_Solver {

	public ControlAF getCAF();
	
	public void setCAF(ControlAF CAF);
	
	public ArgumentFramework getCompletion();
	
	public void setCompletion(ArgumentFramework completion);
	
	public Map<StableControlConfiguration, Set<StableExtension>> getSkepticalControlConfigurations();
	
	public Map<StableControlConfiguration, Set<StableExtension>> getCredulousControlConfigurations();
}
