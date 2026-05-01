public class SimulationState {
    private static final double DEFAULT_SUN_VALUE = 1.0;
    private static final double MIN_SUN_VALUE = 0.0;
    private static final double MAX_SUN_VALUE = 1.5;

    private double sunValue;
    private int tickCount;
    private int nextGeneratedSpeciesNumber;

    public SimulationState() {
        this.sunValue = DEFAULT_SUN_VALUE;
        this.tickCount = 0;
        this.nextGeneratedSpeciesNumber = 1;
    }

    public void advanceTick() {
        tickCount++;
    }

    public int getTickCount() {
        return tickCount;
    }

    public double getSunValue() {
        return sunValue;
    }

    public boolean setSunValue(double sunValue) {
        double clampedValue = clamp(sunValue, MIN_SUN_VALUE, MAX_SUN_VALUE);

        if (this.sunValue == clampedValue) {
            return false;
        }

        this.sunValue = clampedValue;
        return true;
    }

    public String createGeneratedSpeciesName() {
        return "Species-" + nextGeneratedSpeciesNumber++;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}