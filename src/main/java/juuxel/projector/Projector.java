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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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
    private static final Comparator<Vec3> Z_COMPARATOR = Comparator.comparingDouble(vec -> vec.z);

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
        List<Vec3> transformedPoints = new ArrayList<>(POINTS.length);
        for (Vec3 point : POINTS) {
            transformedPoints.add(transform.multiply(point));
        }
        List<Vec3> sortedTransformedPoints = new ArrayList<>(transformedPoints);
        sortedTransformedPoints.sort(Z_COMPARATOR);
        SortedMap<Vec3, List<Edge>> edgesByBackmostPoint = new TreeMap<>(Z_COMPARATOR);
        for (Edge edge : EDGES) {
            Vec3 first = transformedPoints.get(edge.first);
            Vec3 second = transformedPoints.get(edge.second);
            int comparison = Integer.compare(sortedTransformedPoints.indexOf(first), sortedTransformedPoints.indexOf(second));
            Vec3 backmost = comparison > 0 ? second : first;
            edgesByBackmostPoint.computeIfAbsent(backmost, v -> new ArrayList<>()).add(edge);
        }
        for (Vec3 transformed : sortedTransformedPoints) {
            float brightness = computeBrightness(transformed.z);
            Color c = new Color(0, brightness, 0);

            for (Edge edge : edgesByBackmostPoint.getOrDefault(transformed, Collections.emptyList())) {
                int otherIndex = transformedPoints.indexOf(transformed) == edge.first ? edge.second : edge.first;
                Vec3 other = transformedPoints.get(otherIndex);
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
}
