package juuxel.projector;

import java.util.Objects;

public final class Mat3 {
    public final double
        a00, a01, a02,
        a10, a11, a12,
        a20, a21, a22;

    public Mat3(double a00, double a01, double a02,
                double a10, double a11, double a12,
                double a20, double a21, double a22) {
        this.a00 = a00;
        this.a01 = a01;
        this.a02 = a02;
        this.a10 = a10;
        this.a11 = a11;
        this.a12 = a12;
        this.a20 = a20;
        this.a21 = a21;
        this.a22 = a22;
    }

    public Mat3 multiply(Mat3 other) {
        return new Mat3(
            a00 * other.a00 + a01 * other.a10 + a02 * other.a20,
            a00 * other.a01 + a01 * other.a11 + a02 * other.a21,
            a00 * other.a02 + a01 * other.a12 + a02 * other.a22,
            a10 * other.a00 + a11 * other.a10 + a12 * other.a20,
            a10 * other.a01 + a11 * other.a11 + a12 * other.a21,
            a10 * other.a02 + a11 * other.a12 + a12 * other.a22,
            a20 * other.a00 + a21 * other.a10 + a22 * other.a20,
            a20 * other.a01 + a21 * other.a11 + a22 * other.a21,
            a20 * other.a02 + a21 * other.a12 + a22 * other.a22
        );
    }

    public Vec3 multiply(Vec3 vec) {
        return new Vec3(
            a00 * vec.x + a01 * vec.y + a02 * vec.z,
            a10 * vec.x + a11 * vec.y + a12 * vec.z,
            a20 * vec.x + a21 * vec.y + a22 * vec.z
        );
    }

    public Mat3 transpose() {
        return new Mat3(
            a00, a10, a20,
            a01, a11, a21,
            a02, a12, a22
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mat3 mat3 = (Mat3) o;
        return Double.compare(mat3.a00, a00) == 0
            && Double.compare(mat3.a01, a01) == 0
            && Double.compare(mat3.a02, a02) == 0
            && Double.compare(mat3.a10, a10) == 0
            && Double.compare(mat3.a11, a11) == 0
            && Double.compare(mat3.a12, a12) == 0
            && Double.compare(mat3.a20, a20) == 0
            && Double.compare(mat3.a21, a21) == 0
            && Double.compare(mat3.a22, a22) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a00, a01, a02, a10, a11, a12, a20, a21, a22);
    }
}
