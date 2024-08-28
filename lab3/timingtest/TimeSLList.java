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

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        int testSllistLength = 500;
        for(int i = 0; i <= 6; i++) {
            testSllistLength *= 2;
            Ns.addLast(testSllistLength);
            SLList<Integer> testSLlist = new SLList<>();
            for (int j = 0; j < testSllistLength; j++) {
                testSLlist.addLast(1);
            }
            Stopwatch sw = new Stopwatch();
            for (int j = 0; j < 1000; j++) {
                testSLlist.getLast();
            }
            double timeInSecond = sw.elapsedTime();
            times.addLast(timeInSecond);
            opCounts.addLast(1000);
        }
        printTimingTable(Ns, times, opCounts);

    }

}
