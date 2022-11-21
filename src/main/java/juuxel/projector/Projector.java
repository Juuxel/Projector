package juuxel.projector;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public final class Projector extends JComponent {
    private static final Vec3[] POINTS = new Vec3[] {
        new Vec3(-1, -1, -1),
        new Vec3(1, -1, -1),
        new Vec3(-1, 1, -1),
        new Vec3(-1, -1, 1),
        new Vec3(1, 1, -1),
        new Vec3(1, -1, 1),
        new Vec3(-1, 1, 1),
        new Vec3(1, 1, 1),
    };

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
        transformedPoints.sort(Comparator.comparingDouble(vec -> vec.z));
        for (Vec3 transformed : transformedPoints) {
            float brightness = (float) Mth.map(transformed.z, -3, 3, 0, 1);
            h.setColor(new Color(0, brightness, 0));
            h.fill(new Ellipse2D.Double(transformed.x - 0.1, transformed.y - 0.1, 0.2, 0.2));
        }
        h.dispose();
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
}
