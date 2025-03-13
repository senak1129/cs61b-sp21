package gh2;
 import deque.ArrayDeque;
 import deque.Deque;

public class GuitarString {
    private static final int SR = 44100;
    private static final double DECAY = .996;
    private Deque<Double> buffer;

    public GuitarString(double frequency) {
        int capasity = (int) Math.round(SR / frequency);
        buffer = new ArrayDeque<>();
        for (int i = 0; i < capasity; i++) {
            buffer.addLast((Double) 0.0);
        }
    }

    public void pluck() {
        for (int i = 0; i < buffer.size(); i++) {
            buffer.removeFirst();
            buffer.addLast((Math.random() - 0.5)) ;
        }
    }

    public void tic() {
        double first = buffer.removeFirst();
        double second = buffer.get(0);
        double average = (first + second) * 0.5;
        buffer.addLast(average * DECAY);
    }

    public double sample() {
        return buffer.get(0);
    }
}
