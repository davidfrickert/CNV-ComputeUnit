import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;
import pt.ulisboa.tecnico.cnv.instrument.ServerMetrics;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Enumeration;

public class Instrumentor {

    private static final String CLASS_NAME = Instrumentor.class.getName();
    // dynamic
    private int dynMethodCount = 0;

    //static


    public static void main(String[] args) {
        if (args.length != 1) System.out.println("Usage: java Instrumentor <directory of .class files to instrument>");
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
            } else {
                System.out.println("You didn't supply a directory. Nothing done.");
                System.out.println("Usage: java Instrumentor <directory of .class files to instrument>");}
        }
    }

    public static void instrument(File classFile) {
        String fileName = classFile.getAbsolutePath();
        dynamicInstrument(fileName);
    }

    public static void dynamicInstrument(String path) {
        ClassInfo classInfo = new ClassInfo(path);
        for (Enumeration<?> e = classInfo.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            routine.addBefore(CLASS_NAME, "incrementMethodCount", "null");
        }
        classInfo.addAfter(CLASS_NAME, "sendMetrics", classInfo.getClassName());
        classInfo.write(path);
    }

    public static void incrementMethodCount(String placeholder) {
        ServerMetrics.getInstance().increment(Thread.currentThread().getId());
    }

    public static void sendMetrics(String className) {
        ServerMetrics.getInstance().sendMetricsToDynamoDB(className, Thread.currentThread().getId());
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
