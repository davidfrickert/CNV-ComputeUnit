. classpath.sh
/bin/bash compile-web-server.sh
/bin/bash instrument-solvers.sh 
java -XX:-UseSplitVerifier pt.ulisboa.tecnico.cnv.server.WebServer
