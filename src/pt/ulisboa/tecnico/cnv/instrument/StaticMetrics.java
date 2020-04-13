package pt.ulisboa.tecnico.cnv.instrument;

public class StaticMetrics {
    private int methodCount = 0;
    private int bbCount = 0;
    private int instrCount = 0;
    private String instrumentedClass;

    public StaticMetrics(String instrumentedClass, int methodCount, int bbCount, int instrCount) {
        this.methodCount = methodCount;
        this.bbCount = bbCount;
        this.instrCount = instrCount;
        this.instrumentedClass = instrumentedClass;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public int getBbCount() {
        return bbCount;
    }

    public int getInstrCount() {
        return instrCount;
    }

    public String getInstrumentedClass() {
        return instrumentedClass;
    }

    public float getAverageInstrPerBb() {
        return instrCount / (float) bbCount;
    }

    public float getAverageInstrPerMethod() {
        return instrCount / (float) methodCount;
    }

    public float getAverageBbPerMethod() {
        return bbCount / (float) methodCount;
    }

    public void merge(StaticMetrics sm2) {
        this.methodCount += sm2.methodCount;
        this.bbCount += sm2.bbCount;
        this.instrCount += sm2.instrCount;
    }
}
