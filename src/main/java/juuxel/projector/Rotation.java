package juuxel.projector;

public final class Rotation {
    public static Mat3 rotationX(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Mat3(
            1, 0, 0,
            0, cos, -sin,
            0, sin, cos
        );
    }

    public static Mat3 rotationY(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Mat3(
            cos, 0, sin,
            0, 1, 0,
            -sin, 0, cos
        );
    }

    public static Mat3 rotationZ(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Mat3(
            cos, -sin, 0,
            sin, cos, 0,
            0, 0, 1
        );
    }
}
