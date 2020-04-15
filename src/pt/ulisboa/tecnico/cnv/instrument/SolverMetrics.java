package pt.ulisboa.tecnico.cnv.instrument;

import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;

/**
 * Metrics for a thread
 */
public class SolverMetrics {
    // incremented every time a method is called on Solver class
    private int dynamicMethodCount;
    // count of "newarray" operations - single dimension array of primitive types
    private int newArrayCount;
    // count of "anewarray" operations - single dimension array of reference types
    private int newReferenceArrayCount;
    // count of "multianewarray" - multi dimensional array of reference types
    private int newMultiDimensionalArrayCount;
    // count of "new" - new objects
    private int newObjectCount;

    // Solver Type (DLX, CP, BFS)
    private SolverFactory.SolverType solver;
    // Unassigned Entries in Sudoku grid
    private int unassignedEntries;

    private int nColumns;
    private int nLines;
    // Map Name (Example: SUDOKU_PUZZLE_9x9_104, SUDOKU_PUZZLE_16x16_01)
    private String mapName;

    public SolverMetrics(SolverFactory.SolverType solver, int unassignedEntries, int nColumns, int nLines, String mapName) {
        this.solver = solver;
        this.unassignedEntries = unassignedEntries;
        this.nColumns = nColumns;
        this.nLines = nLines;
        this.mapName = mapName;
    }

    public SolverMetrics(String solver, int unassignedEntries, int nColumns, int nLines, String mapName) {
        this.solver = SolverFactory.SolverType.valueOf(solver);
        this.unassignedEntries = unassignedEntries;
        this.nColumns = nColumns;
        this.nLines = nLines;
        this.mapName = mapName;
    }

    public SolverMetrics() {}

    public void incrementMethodCount() {
        dynamicMethodCount++;
    }
    public void incrementNewArrayCount() {
        newArrayCount++;
    }
    public void incrementNewReferenceArrayCount() {
        newReferenceArrayCount++;
    }
    public void incrementNewMultiDimArrayCount() {
        newMultiDimensionalArrayCount++;
    }
    public void incrementNewObjectCount() {
        newObjectCount++;
    }

    public int getDynamicMethodCount() {
        return dynamicMethodCount;
    }
    
    public int getNewArrayCount(){
        return newArrayCount;   
    }
    
    public int getNewReferenceArrayCount(){
        return newReferenceArrayCount;   
    }
    
    public int getNewMultiDimensionalArrayCount(){
        return newMultiDimensionalArrayCount;   
    }
    
    public int getNewObjectCount(){
        return newObjectCount;   
    }

    public SolverFactory.SolverType getSolver() {
        return solver;
    }

    public int getUnassignedEntries() {
        return unassignedEntries;
    }

    public int getnColumns() {
        return nColumns;
    }

    public int getnLines() {
        return nLines;
    }

    public String getMapName() {
        return mapName;
    }

    public static SolverMetrics fromParser(SolverArgumentParser ap) {
        return new SolverMetrics(ap.getSolverStrategy(), ap.getUn(), ap.getN1(), ap.getN2(), ap.getInputBoard());
    }

    public void fill(SolverArgumentParser ap) {
        this.solver = ap.getSolverStrategy();
        this.unassignedEntries = ap.getUn();
        this.nColumns = ap.getN1();
        this.nLines = ap.getN2();
        this.mapName = ap.getInputBoard();
    }


    @Override
    public String toString() {
        return "SolverMetrics{" +
                "dynamicMethodCount=" + dynamicMethodCount +
                ", newArrayCount=" + newArrayCount +
                ", newReferenceArrayCount=" + newReferenceArrayCount +
                ", newMultiDimensionalArrayCount=" + newMultiDimensionalArrayCount +
                ", newObjectCount=" + newObjectCount +
                ", solver=" + solver +
                ", unassignedEntries=" + unassignedEntries +
                ", nColumns=" + nColumns +
                ", nLines=" + nLines +
                ", mapName='" + mapName + '\'' +
                '}';
    }
}
