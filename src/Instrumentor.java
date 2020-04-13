import BIT.highBIT.*;
import pt.ulisboa.tecnico.cnv.instrument.ServerMetrics;
import pt.ulisboa.tecnico.cnv.instrument.StaticMetrics;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Instrumentor {

    private static final String CLASS_NAME = Instrumentor.class.getName();
    // dynamic
    private int dynMethodCount = 0;

    //static


    public static void main(String[] args) {
        if (args.length != 1) executeOnSolvers();
        else {
            String path = args[0];
            File f = new File(path);
            if (f.isDirectory()) {
                File[] classes = f.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".class");
                    }
                });
                for (File classFile : classes) {
                    instrument(classFile);
                }
            } else System.out.println("Not a directory");
        }
    }

    public static void executeOnSolvers() {
        final StaticMetrics smCP =
                instrument(new File("out/production/cnv-project/pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP.class"));
        final StaticMetrics smBFS =
                instrument(new File("out/production/cnv-project/pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS.class"));
        final StaticMetrics smDLX =
            instrument(new File("out/production/cnv-project/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX.class"));

        StaticMetrics smDLXInner =
            instrument(new File("out/production/cnv-project/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver.class"));
        smDLX.merge(smDLXInner);


        Map<SolverFactory.SolverType, StaticMetrics> staticMetricsMap = new HashMap<>();

        staticMetricsMap.put(SolverFactory.SolverType.BFS, smBFS);
        staticMetricsMap.put(SolverFactory.SolverType.CP, smCP);
        staticMetricsMap.put(SolverFactory.SolverType.DLX, smDLX);

        // write this on dynamodb..
    }

    public static StaticMetrics instrument(File classFile) {
        String fileName = classFile.getAbsolutePath();
        StaticMetrics sm = staticInstrument(fileName);
        dynamicInstrument(fileName);
        return sm;
    }

    public static StaticMetrics staticInstrument(String path) {
        ClassInfo classInfo = new ClassInfo(path);
        Vector<?> routines = classInfo.getRoutines();

        int staticMethodCount = routines.size();
        int staticBbCount = 0;
        int staticInstrCount = 0;

        for (Enumeration<?> e = routines.elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            BasicBlockArray bba = routine.getBasicBlocks();
            staticBbCount += bba.size();
            InstructionArray ia = routine.getInstructionArray();
            staticInstrCount += ia.size();
        }
        System.out.println(classInfo.getClassName());
        System.out.println("Static information summary [" + extractMainClassName(classInfo) +"]:");
        System.out.println("Number of methods:      " + staticMethodCount);
        System.out.println("Number of basic blocks: " + staticBbCount);
        System.out.println("Number of instructions: " + staticInstrCount);

        if (staticMethodCount == 0) {
            return null;
        }

        float instr_per_bb = (float) staticInstrCount / (float) staticBbCount;
        float instr_per_method = (float) staticInstrCount / (float) staticMethodCount;
        float bb_per_method = (float) staticBbCount / (float) staticMethodCount;

        System.out.println("Average number of instructions per basic block: " + instr_per_bb);
        System.out.println("Average number of instructions per method:      " + instr_per_method);
        System.out.println("Average number of basic blocks per method:      " + bb_per_method);

        return new StaticMetrics(extractMainClassName(classInfo),
                staticMethodCount, staticBbCount, staticInstrCount);
    }

    public static void dynamicInstrument(String path) {
        ClassInfo classInfo = new ClassInfo(path);
        for (Enumeration e = classInfo.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            routine.addBefore(CLASS_NAME, "incrementMethodCount", "null");
        }
        classInfo.addAfter(CLASS_NAME, "sendMetrics", "null");
        classInfo.write(path);
    }

    public static void incrementMethodCount(String placeholder) {
        ServerMetrics.getInstance().increment(Thread.currentThread().getId());
    }

    public static void sendMetrics(String placeholder) {
        ServerMetrics.getInstance().sendMetricsToDynamoDB(Thread.currentThread().getId());
    }


    public static boolean renameFile(String path) {
        Path srcPath = Paths.get(path);
        Path srcDir = srcPath.getParent();
        String filename = srcPath.getFileName().toString().replaceFirst("[.][^.]+$", "");
        try {
            String notInstrumentedPath = filename + "_NI.class";
            Files.deleteIfExists(srcDir.resolve(notInstrumentedPath));
            Files.move(srcPath, srcDir.resolve(notInstrumentedPath));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String extractMainClassName(ClassInfo classInfo) {
        // example:
        // pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver
        // extract SudokuSolverDLX$AlgorithmXSolver
        // then, extract SudokuSolverDLX (outer class)

        String fqn = classInfo.getClassName();
        boolean containsSlash = fqn.contains("/");
        String className = fqn;
        if (containsSlash) {
            String[] tokens = className.split("/");
            className = tokens[tokens.length - 1].split("\\$")[0];
        }
        return className;
    }

}
