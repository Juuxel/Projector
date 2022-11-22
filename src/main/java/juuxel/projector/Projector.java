package juuxel.projector;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public final class Projector extends JComponent {
    private static final Vec3[] POINTS = {
        new Vec3(-1, -1, -1),
        new Vec3(1, -1, -1),
        new Vec3(-1, 1, -1),
        new Vec3(-1, -1, 1),
        new Vec3(1, 1, -1),
        new Vec3(1, -1, 1),
        new Vec3(-1, 1, 1),
        new Vec3(1, 1, 1),
    };
    private static final Edge[] EDGES = {
        // Back half
        new Edge(0, 1),
        new Edge(1, 4),
        new Edge(4, 2),
        new Edge(2, 0),
        // Front half
        new Edge(3, 5),
        new Edge(5, 7),
        new Edge(7, 6),
        new Edge(6, 3),
        // Back to front
        new Edge(0, 3),
        new Edge(1, 5),
        new Edge(2, 6),
        new Edge(4, 7),
    };
    private static final Quad[] QUADS = {
        new Quad(0, 1, 4, 2),
        new Quad(3, 5, 7, 6),
        new Quad(0, 1, 5, 3),
        new Quad(2, 4, 7, 6),
        new Quad(0, 2, 6, 3),
        new Quad(1, 4, 7, 5),
    };
    private static final Comparator<Pair<Integer, Vec3>> Z_COMPARATOR =
        Comparator.comparingDouble((Pair<Integer, Vec3> pair) -> pair.second.z)
            .thenComparingInt(pair -> pair.first);

    private double angleX;
    private double angleY;
    private double angleZ;

    @Override
    protected void paintComponent(Graphics g) {
        // Background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Points
        Graphics2D h = (Graphics2D) g.create();
        h.translate(getWidth() / 2, getHeight() / 2);
        h.scale(50, 50);
        Mat3 transform = Rotation.rotationX(angleX)
            .multiply(Rotation.rotationY(angleY))
            .multiply(Rotation.rotationZ(angleZ));
        List<Pair<Integer, Vec3>> transformedPoints = new ArrayList<>(POINTS.length);
        int i = 0;
        for (Vec3 point : POINTS) {
            transformedPoints.add(new Pair<>(i++, transform.multiply(point)));
        }
        List<Pair<Integer, Vec3>> sortedTransformedPoints = new ArrayList<>(transformedPoints);
        sortedTransformedPoints.sort(Z_COMPARATOR);
        SortedMap<Pair<Integer, Vec3>, List<Edge>> edgesByBackmostPoint = new TreeMap<>(Z_COMPARATOR);
        for (Edge edge : EDGES) {
            Pair<Integer, Vec3> first = transformedPoints.get(edge.first);
            Pair<Integer, Vec3> second = transformedPoints.get(edge.second);
            int comparison = Integer.compare(sortedTransformedPoints.indexOf(first), sortedTransformedPoints.indexOf(second));
            Pair<Integer, Vec3> backmost = comparison > 0 ? second : first;
            edgesByBackmostPoint.computeIfAbsent(backmost, v -> new ArrayList<>()).add(edge);
        }
        SortedMap<Pair<Integer, Vec3>, List<Quad>> quadsByBackmostPoint = new TreeMap<>(Z_COMPARATOR);
        for (Quad quad : QUADS) {
            Pair<Integer, Vec3> backmost = Stream.of(quad.a, quad.b, quad.c, quad.d)
                .map(transformedPoints::get)
                .sorted(Comparator.comparing(sortedTransformedPoints::indexOf))
                .findFirst().get();
            quadsByBackmostPoint.computeIfAbsent(backmost, v -> new ArrayList<>()).add(quad);
        }
        for (Pair<Integer, Vec3> transformedPair : sortedTransformedPoints) {
            Vec3 transformed = transformedPair.second;
            float brightness = computeBrightness(transformed.z);
            Color c = new Color(0, brightness, 0);

            for (Quad quad : quadsByBackmostPoint.getOrDefault(transformedPair, Collections.emptyList())) {
                Vec3 frontmost = Stream.of(quad.a, quad.b, quad.c, quad.d)
                    .map(transformedPoints::get)
                    .sorted(Comparator.comparing(sortedTransformedPoints::indexOf).reversed())
                    .findFirst().get().second;
                Color c2 = new Color(0, computeBrightness(frontmost.z), 0, 0.2f);
                h.setPaint(new GradientPaint(
                    (float) transformed.x, (float) transformed.y, c,
                    (float) frontmost.x, (float) frontmost.y, c2
                ));
                Path2D.Double path = new Path2D.Double();
                Vec3 va = transformedPoints.get(quad.a).second;
                Vec3 vb = transformedPoints.get(quad.b).second;
                Vec3 vc = transformedPoints.get(quad.c).second;
                Vec3 vd = transformedPoints.get(quad.d).second;
                path.moveTo(va.x, va.y);
                path.lineTo(vb.x, vb.y);
                path.lineTo(vc.x, vc.y);
                path.lineTo(vd.x, vd.y);
                path.lineTo(va.x, va.y);
                h.fill(path);
            }

            for (Edge edge : edgesByBackmostPoint.getOrDefault(transformedPair, Collections.emptyList())) {
                int otherIndex = transformedPair.first == edge.first ? edge.second : edge.first;
                Vec3 other = transformedPoints.get(otherIndex).second;
                Color c2 = new Color(0, computeBrightness(other.z), 0);
                h.setPaint(new GradientPaint(
                    (float) transformed.x, (float) transformed.y, c,
                    (float) other.x, (float) other.y, c2)
                );
                h.setStroke(new BasicStroke(0.1f));
                h.draw(new Line2D.Double(transformed.x, transformed.y, other.x, other.y));
            }

            h.setColor(c);
            h.fill(new Ellipse2D.Double(transformed.x - 0.1, transformed.y - 0.1, 0.2, 0.2));
        }
        h.dispose();
    }

    private static float computeBrightness(double z) {
        return (float) Mth.map(z, -3, 3, 0, 1);
    }

    private static JSlider createAngleSlider() {
        JSlider slider = new JSlider(0, 360);
        Dictionary<Integer, JComponent> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0\u00B0"));
        labelTable.put(360, new JLabel("360\u00B0"));
        slider.setPaintLabels(true);
        slider.setLabelTable(labelTable);
        return slider;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            Projector projector = new Projector();

            JSlider sliderX = createAngleSlider();
            JSlider sliderY = createAngleSlider();
            JSlider sliderZ = createAngleSlider();
            sliderX.addChangeListener(e -> { projector.angleX = Math.toRadians(sliderX.getValue()); projector.repaint(); });
            sliderY.addChangeListener(e -> { projector.angleY = Math.toRadians(sliderY.getValue()); projector.repaint(); });
            sliderZ.addChangeListener(e -> { projector.angleZ = Math.toRadians(sliderZ.getValue()); projector.repaint(); });

            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(0, 2));
            controlPanel.add(new JLabel("X Rotation"));
            controlPanel.add(sliderX);
            controlPanel.add(new JLabel("Y Rotation"));
            controlPanel.add(sliderY);
            controlPanel.add(new JLabel("Z Rotation"));
            controlPanel.add(sliderZ);
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projector, controlPanel);

            // Set up frame
            JFrame frame = new JFrame("Projector");
            frame.setSize(640, 480);
            frame.setContentPane(split);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
            split.setDividerLocation(0.5);
        });
    }

    private static final class Edge {
        final int first;
        final int second;

        Edge(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    private static final class Quad {
        final int a, b, c, d;
        Quad(int a, int b, int c, int d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }
    }
}
