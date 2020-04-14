. source-this-before-compiling.sh
echo "compiling Instrumentor.java"
javac Instrumentor.java
echo "compiling solvers"
javac pt/ulisboa/tecnico/cnv/solver/*.java
echo "instrumenting solvers"
java -XX:-UseSplitVerifier Instrumentor pt/ulisboa/tecnico/cnv/solver
