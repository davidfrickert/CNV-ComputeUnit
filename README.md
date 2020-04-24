To run the Sudoku Solver WebServer:

Run the script run-server.sh in your linux environment (ideally AWS linux machine with only java 7, since this script doesn't check for which java version is being used).
/bin/bash ~/CNV-ComputeUnit/run-server.sh

This script will:
1. Call the script compile-web-server.sh
	- Compiles metrics classes defined by us in pt.ulisboa.tecnico.cnv.instrument package
	- Compiles WebServer.java 
2. Call the script instrument-solvers.sh
	- Compiles the Instrumentor java class developed by us to instrument the solver classes 
	- Compiles all classes in pt.ulisboa.tecnico.cnv.solver [solver classes]
	- Instruments solvers using Instrumentor class
3. Run the web-server

There is also a systemd service defined on the systemd folder that we are using on our AWS machines.