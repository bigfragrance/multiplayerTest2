package big.game.weapon;

import big.engine.math.Vec2d;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShapeRotationViewer{


    private BufferedImage image;
    private List<Vec2d> shapePoints;
    private double angle = 0;

    public ShapeRotationViewer(BufferedImage image, List<Vec2d> shapePoints) {
        this.image = image;
        this.shapePoints = shapePoints;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void render(Graphics g) {
        if (image == null || shapePoints == null || shapePoints.isEmpty()) return;
        Graphics2D g2d = (Graphics2D) g.create();
        int width = image.getWidth();
        int height = image.getHeight();
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        Path2D.Double path = new Path2D.Double();
        Vec2d start = shapePoints.get(0);
        path.moveTo(start.x, start.y);
        for (int i = 1; i < shapePoints.size(); i++) {
            Vec2d p = shapePoints.get(i);
            path.lineTo(p.x, p.y);
        }
        path.closePath();
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(angle), centerX, centerY);
        Shape oldShape=g2d.getClip();
        Shape rotatedShape = transform.createTransformedShape(path);
        g2d.setClip(rotatedShape);
        g2d.drawImage(image, transform, null);
        g2d.setClip(oldShape);
    }
}
