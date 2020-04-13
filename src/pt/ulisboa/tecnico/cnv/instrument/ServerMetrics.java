package pt.ulisboa.tecnico.cnv.instrument;

import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMetrics {
    private Map<Long, SolverMetrics> threadMetrics = new ConcurrentHashMap<>();

    private ServerMetrics() { }
    private static ServerMetrics INSTANCE;

    public static synchronized ServerMetrics getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerMetrics();
        }
        return INSTANCE;
    }

    public void increment(String className, Long threadId) {
        System.out.println(className + " invoking increment");
        SolverMetrics sm = threadMetrics.get(threadId);
        if (sm != null) {
            sm.incrementMethodCount();
            System.out.println(sm.getDynamicMethodCount() + " method calls.");
        } else {
            System.out.println("attempt to increment thread not in HashMap. ThreadId=" + threadId);
        }
    }

    public void sendMetricsToDynamoDB(Long threadId) {
        //TODO
        System.out.println("printing result of this computation below");
        System.out.println(threadMetrics.get(threadId));
    }

    public void add(SolverArgumentParser ap) {
        threadMetrics.put(Thread.currentThread().getId(), SolverMetrics.fromParser(ap));
    }
}
