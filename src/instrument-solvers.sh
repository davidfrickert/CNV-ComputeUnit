. classpath.sh
echo "compiling Instrumentor.java"
javac Instrumentor.java
echo "compiling solvers"
javac pt/ulisboa/tecnico/cnv/solver/*.java
echo "instrumenting solvers"
java -XX:-UseSplitVerifier Instrumentor pt/ulisboa/tecnico/cnv/solver 'SudokuSolverDLX$AlgorithmXSolver$Node.class' 'SudokuSolverDLX$AlgorithmXSolver$ColumnNode.class' 'SudokuSolverDLX$AlgorithmXSolver$ColumnID.class'
