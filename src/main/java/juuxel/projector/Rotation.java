package juuxel.projector;

public final class Rotation {
    public static Mat3 asMatrix(double pitch, double yaw, double roll) {
        double c1 = Math.cos(pitch), s1 = Math.sin(pitch);
        double c2 = Math.cos(yaw), s2 = Math.sin(yaw);
        double c3 = Math.cos(roll), s3 = Math.sin(roll);
        return new Mat3(
            c2 * c3, -c2 * s3, s2,
            c1 * s3 + c3 * s1 * s2, c1 * c3 - s1 * s2 * s3, -c2 * s1,
            s1 * s3 - c1 * c3 * s2, c3 * s1 + c1 * s2 * s3, c1 * c2
        );
    }
}
