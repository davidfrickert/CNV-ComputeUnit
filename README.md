This folder contains our extensions to the provided Sudoku Solver WebServer.
Our additions are on src\pt\ulisboa\tecnico\cnv\instrument, some on the (...)\WebServer.java, and some scripts + Instrumentor.java on the root folder (src).
We opted to bundle all dependencies on a folder for separation of code. Those are on lib folder. 
- BIT.jar - BIT
- org.jar - JSON and Apache Commons
- aws-java-sdk-1.11.762.jar - AWS SDK
The script classpath.sh will set the classpath if you source it - built for Amazon Linux 2 machines. (. classpath.sh / source classpath.sh)

Please check the file Server-metrics.xlsx to view example metrics exported from a DynamoDB table.

To run the Sudoku Solver WebServer:

Run the script run-server.sh in your linux environment (ideally AWS Amazon Linux 2 machine with only java 7, since this script doesn't check for which java version is being used).
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

There is also a systemd service defined on the systemd folder that we are using on our AWS machines to run the server as a service.