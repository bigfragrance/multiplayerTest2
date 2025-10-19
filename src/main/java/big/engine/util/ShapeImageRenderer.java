package big.engine.util;

import big.engine.math.Vec2d;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;


public class ShapeImageRenderer {

    private BufferedImage image;
    private List<Vec2d> shapePoints;
    private int width, height;

    public ShapeImageRenderer(BufferedImage image, List<Vec2d> shapePoints) {
        this.image = image;
        this.shapePoints = shapePoints;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public void render(Graphics2D g2d, double x, double y, double angleDeg, double scale) {
        g2d = (Graphics2D) g2d.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.rotate(Math.toRadians(angleDeg));
        transform.scale(scale, scale);


        Path2D.Double path = new Path2D.Double();
        if (!shapePoints.isEmpty()) {
            Vec2d start = shapePoints.get(0);
            path.moveTo(start.x, start.y);
            for (int i = 1; i < shapePoints.size(); i++) {
                Vec2d p = shapePoints.get(i);
                path.lineTo(p.x, p.y);
            }
            path.closePath();
        }


        Shape transformedShape = transform.createTransformedShape(path);

        g2d.setClip(transformedShape);

        g2d.drawImage(image, 
            (int) (x - width * scale / 2),
            (int) (y - height * scale / 2),
            (int) (width * scale),
            (int) (height * scale),
            null
        );
        g2d.setClip(null);
    }

    public void renderOutline(Graphics2D g2d, double x, double y, double angleDeg, double scale, Color color) {
        g2d = (Graphics2D) g2d.create();

        AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.rotate(Math.toRadians(angleDeg));
        transform.scale(scale, scale);

        Path2D.Double path = new Path2D.Double();
        if (!shapePoints.isEmpty()) {
            Vec2d start = shapePoints.get(0);
            path.moveTo(start.x, start.y);
            for (int i = 1; i < shapePoints.size(); i++) {
                Vec2d p = shapePoints.get(i);
                path.lineTo(p.x, p.y);
            }
            path.closePath();
        }

        Shape transformedShape = transform.createTransformedShape(path);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2f));
        g2d.draw(transformedShape);
        g2d.dispose();
    }
}
