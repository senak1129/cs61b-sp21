package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
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
        timeAListConstruction();
    }

    public static void add(int Count , AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts){
        AList<Integer> test = new AList<>();
        Stopwatch sw = new Stopwatch();
        for(int i=1;i<=Count;i++){
            test.addLast(i);
        }
        double timeInSeconds = sw.elapsedTime();
        Ns.addLast(Count);
        times.addLast(timeInSeconds);
        opCounts.addLast(Count);
    }
    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        add(1000, Ns, times, opCounts);
        add(2000, Ns, times, opCounts);
        add(4000, Ns, times, opCounts);
        add(8000, Ns, times, opCounts);
        add(16000, Ns, times, opCounts);
        add(32000, Ns, times, opCounts);
        add(64000, Ns, times, opCounts);
        add(128000, Ns, times, opCounts);
        printTimingTable(Ns, times, opCounts);


    }
}
