Full Framework to work on:

1) Argument Frameworks
2) Control Argument Frameworks
3) Probabilistic Control Argument Frameworks
4) Weighted Argument Frameworks (h_cathegorizer implemented as unique semantic)

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

##############################################
Generation of problems
###############################################
The package generators enables as well to generate problems
Via the class CAFGenerator from a regular Argument framework.

##############################################
Inputs/outputs
###############################################
The package parser enables to read:
Argument Frameworks from file
CAFs from file
PCAFs from file

################################################
Monte Carlo Simulation for: 
controllability solving
mpce solving
Use package solver
#################################################
use class Monte_Carlo_CAF_Solver in package solvers
for mpce use class Most_Probable_Controlling_Entities_Solver class
This class returns as well the supporting power of all arguments.
Any argument not in the list has a supporting power of 0

######################################
test package
#####################################
offers a set of functions to run the various algorithms.
