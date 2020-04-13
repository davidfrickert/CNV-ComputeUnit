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

    public void increment(Long threadId) {
        SolverMetrics sm = threadMetrics.get(threadId);
        if (sm != null) sm.incrementMethodCount();
    }

    public void sendMetricsToDynamoDB(String className, Long threadId) {
        //TODO
        System.out.println(className + " invoking sendMetricsToDynamoDB");
        System.out.println(threadMetrics.get(threadId));
    }

    public void add(SolverArgumentParser ap) {
        threadMetrics.put(Thread.currentThread().getId(), SolverMetrics.fromParser(ap));
    }
}
