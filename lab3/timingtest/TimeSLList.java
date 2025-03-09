package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void add(int N ,int M, AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        SLList<Integer> list = new SLList<>();
        for(int i=1;i<=N;i++){
            list.addLast(i);
        }Stopwatch sw = new Stopwatch();
        for(int i=1;i<=M;i++){
            list.getLast();
        }
        double timeInSeconds = sw.elapsedTime();
        Ns.addLast(N);
        times.addLast(timeInSeconds);
        opCounts.addLast(M);
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        add(1000,10000,Ns, times, opCounts);
        add(2000,10000,Ns, times, opCounts);
        add(4000,10000,Ns, times, opCounts);
        add(8000,10000,Ns, times, opCounts);
        add(16000,10000,Ns, times, opCounts);
        add(32000,10000,Ns, times, opCounts);
        add(64000,10000,Ns, times, opCounts);
        add(128000,10000,Ns, times, opCounts);
        printTimingTable(Ns, times, opCounts);
    }

}
