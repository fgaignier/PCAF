package solvers;

import java.util.Map;
import java.util.Set;

import model.StableControlConfiguration;
import model.SupportingPowerRecorder;

public interface I_Monte_Carlo_Solver {

	/**
	 * defines the type of solver used
	 */
	public static int CSP_SOLVER = 0;
	public static int SAT_SOLVER = 1;
	
	public int getNumberSimu();
	
	public double getControllingPower();
	
	public Map<StableControlConfiguration, SupportingPowerRecorder> getSupportingPowerRecorders();
	
	public Set<StableControlConfiguration> getCredulousControlConfigurations(int N);
	
	public Set<StableControlConfiguration> getSkepticalControlConfigurations(int N);
	
	public Set<StableControlConfiguration> getCredulousControlConfigurations(double error);
	
	public Set<StableControlConfiguration> getSkepticalControlConfigurations(double error);
	
}
