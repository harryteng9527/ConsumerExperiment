import java.time.Duration;

public class DownTime {
    private int generationId;
    private Duration rebalanceTime;

    public DownTime(Duration rebalanceTime, int generationId) {
        this.rebalanceTime = rebalanceTime;
        this.generationId = generationId;
    }

    @Override
    public String toString() {
        return "DownTime{" +
                "generationId=" + generationId +
                ", rebalanceTime=" + rebalanceTime.toMillis() + "ms" +
                '}';
    }
}
