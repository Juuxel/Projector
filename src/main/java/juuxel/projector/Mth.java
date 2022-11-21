package juuxel.projector;

public final class Mth {
    public static double map(double x, double fromA, double fromB, double toA, double toB) {
        return (x - fromA) / (fromB - fromA) * (toB - toA) + toA;
    }
}
