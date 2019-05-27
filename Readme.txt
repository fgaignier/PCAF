Full Framework to work on:

Argument Frameworks
Control Argument Frameworks
Probabilistic Control Argument Frameworks
Weighted Argument Frameworks (h_cathegorizer implemented as unique semantic)

##################################################
QBF Formulas generation for controllability solving
##################################################
Use a StrongQBFEncoder object (constructed with a CAF object)
Call encode method (with type ControllabilityEncoder.CREDULOUS or ControllabilityEncoder.SKEPTICAL)
to get a QBFFormula object.
This object can be transformed into QCIR calling toQCIR() method.
For QDIMACS it is more complex:
build a QDIMACSConverter object with your QBFFormula
call toQDimacs() method.

Main shows an example.

################################################
Monte Carlo Simulation for: 
controllability solving
mpce solving
#################################################
use class Monte_Carlo_CAF_Solver in package solvers