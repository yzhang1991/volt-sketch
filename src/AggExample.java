
public class AggExample {

    int sum;
    int cnt;

    public void init() {
        sum = 0;
        cnt = 0;
    }

    public void merge(AggExample other) {
        sum += other.sum;
        cnt += other.cnt;
    }

    public void accumulate(int value) {
        sum += value;
        cnt++;
    }

    public int terminate() {
        return sum / cnt;
    }
}
