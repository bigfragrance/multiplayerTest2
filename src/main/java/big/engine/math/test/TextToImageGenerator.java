package big.engine.math.test;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
public class TextToImageGenerator extends JFrame {

    private final JTextArea textArea = new JTextArea(8, 32);
    private final JTextField widthField = new JTextField("1024", 6);
    private final JTextField heightField = new JTextField("1024", 6);
    private final JCheckBox keepSquare = new JCheckBox("square", true);


    private final JTextField strokeRatioField = new JTextField("0.04", 4);
    private final JTextField lineGapRatioField = new JTextField("0.05", 4);

    private final JLabel previewLabel = new JLabel("", SwingConstants.CENTER);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TextToImageGenerator app = new TextToImageGenerator();
            app.setVisible(true);
        });
    }

    public TextToImageGenerator() {
        super("txt2img");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(950, 750);
        setLocationRelativeTo(null);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel ioPanel = new JPanel(new GridBagLayout());
        ioPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;


        gc.gridx = 0; gc.gridy = 0;
        ioPanel.add(new JLabel("input"), gc);
        gc.gridx = 1; gc.gridy = 0; gc.gridwidth = 3; gc.weightx = 1.0;
        ioPanel.add(new JScrollPane(textArea), gc);
        gc.gridwidth = 1; gc.weightx = 0;


        gc.gridx = 0; gc.gridy = 1;
        ioPanel.add(new JLabel("w(px)"), gc);
        gc.gridx = 1; ioPanel.add(widthField, gc);
        gc.gridx = 2; ioPanel.add(new JLabel("h(px)"), gc);
        gc.gridx = 3; ioPanel.add(heightField, gc);

        gc.gridx = 1; gc.gridy = 2; gc.gridwidth = 3;
        ioPanel.add(keepSquare, gc);
        gc.gridwidth = 1;

        gc.gridx = 0; gc.gridy = 3;
        ioPanel.add(new JLabel("line ratio"), gc);
        gc.gridx = 1; ioPanel.add(strokeRatioField, gc);

        gc.gridx = 2;
        ioPanel.add(new JLabel("line gap ratio"), gc);
        gc.gridx = 3; ioPanel.add(lineGapRatioField, gc);


        JButton generateBtn = new JButton("copy");
        generateBtn.addActionListener(this::onGenerate);
        JButton previewBtn = new JButton("preview");
        previewBtn.addActionListener(e -> updatePreview(false));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btns.add(generateBtn);
        btns.add(previewBtn);

        gc.gridx = 1; gc.gridy = 4; gc.gridwidth = 3;
        ioPanel.add(btns, gc);


        previewLabel.setBorder(BorderFactory.createTitledBorder("preview"));
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.WHITE);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                ioPanel,
                new JScrollPane(previewLabel)
        );
        split.setResizeWeight(0.45);

        setContentPane(split);
    }

    private void onGenerate(ActionEvent e) {
        BufferedImage img = updatePreview(true);
        if (img != null) {
            copyImageToClipboard(img);
            JOptionPane.showMessageDialog(this, "copied", "success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private BufferedImage updatePreview(boolean copyOriginal) {
        String text = textArea.getText();
        if (text == null) text = "";
        int w, h;
        try {
            w = Math.max(64, Integer.parseInt(widthField.getText().trim()));
            h = Math.max(64, Integer.parseInt(heightField.getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "int only", "err",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (keepSquare.isSelected()) h = w;

        double strokeRatio = 0.04, lineGapRatio = 0.05;
        try {
            strokeRatio = Math.max(0.01, Math.min(0.1,
                    Double.parseDouble(strokeRatioField.getText().trim())));
            lineGapRatio = Math.max(0.0, Math.min(0.3,
                    Double.parseDouble(lineGapRatioField.getText().trim())));
        } catch (NumberFormatException ignore) {}

        BufferedImage img = renderTextImage(text, w, h, strokeRatio, lineGapRatio);
        if (img == null) return null;

        int maxPreview = 520;
        int pw = w, ph = h;
        double scale = 1.0;
        if (Math.max(w, h) > maxPreview) {
            scale = maxPreview / (double) Math.max(w, h);
            pw = (int) Math.round(w * scale);
            ph = (int) Math.round(h * scale);
        }
        Image scaled = img.getScaledInstance(pw, ph, Image.SCALE_SMOOTH);
        previewLabel.setIcon(new ImageIcon(scaled));

        return copyOriginal ? img : null;
    }

    private BufferedImage renderTextImage(String raw, int width, int height,
                                          double strokeRatio, double lineGapRatio) {
        Color bg = new Color(255, 69, 0);
        Color fillColor = Color.YELLOW;
        Color strokeColor = Color.RED;


        int[] cps = raw.codePoints().toArray();
        int n = cps.length;
        int perLine = Math.max(1, (int) Math.ceil(Math.sqrt(Math.max(1, n))));
        List<String> lines = wrapByCodePoints(cps, perLine);


        if (lines.size() > 1) {
            int avg = n / lines.size();
            String last = lines.get(lines.size() - 1);
            if (last.length() < avg / 3d) {
                lines.set(lines.size() - 2, lines.get(lines.size() - 2) + last);
                lines.remove(lines.size() - 1);
            }
        }

        int marginX = (int) (width * 0.07);
        int marginY = (int) (height * 0.07);

        int low = 8, high = Math.min(width, height);
        Font bestFont = null;
        float bestStroke = 1f;
        FontRenderContext frc = new FontRenderContext(null, true, true);

        while (low <= high) {
            int mid = (low + high) / 2;
            Font f = new Font("SansSerif", Font.BOLD, mid);
            float stroke = Math.max(1f, (float) (mid * strokeRatio));

            Dimension sz = measureBlock(lines, f, frc, stroke, lineGapRatio);
            if (sz.width + 2 * marginX <= width && sz.height + 2 * marginY <= height) {
                bestFont = f;
                bestStroke = stroke;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        if (bestFont == null) {
            bestFont = new Font("SansSerif", Font.BOLD, 10);
            bestStroke = 1f;
        }

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(bg);
        g.fillRect(0, 0, width, height);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setFont(bestFont);
        FontMetrics fm = g.getFontMetrics();
        int ascent = fm.getAscent();
        int lineHeight = fm.getHeight();
        int lineGap = Math.max(0, (int) Math.round(lineHeight * lineGapRatio));
        int lineAdvance = lineHeight + lineGap;

        Dimension block = measureBlock(lines, bestFont, g.getFontRenderContext(), bestStroke, lineGapRatio);
        int startX = (width - block.width) / 2;
        int startY = (height - block.height) / 2 + ascent;

        BasicStroke outline = new BasicStroke(bestStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        for (int i = 0; i < lines.size(); i++) {
            GlyphVector gv = bestFont.createGlyphVector(g.getFontRenderContext(), lines.get(i));
            Shape shape = gv.getOutline(startX, startY + i * lineAdvance);

            g.setColor(fillColor);
            g.fill(shape);
            g.setStroke(outline);
            g.setColor(strokeColor);
            g.draw(shape);
        }

        g.dispose();
        return img;
    }

    private static List<String> wrapByCodePoints(int[] cps, int perLine) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < cps.length; i += perLine) {
            int end = Math.min(i + perLine, cps.length);
            lines.add(new String(cps, i, end - i));
        }
        return lines;
    }

    private static Dimension measureBlock(List<String> lines, Font font, FontRenderContext frc,
                                          float stroke, double lineGapRatio) {
        int width = 0;
        int lineHeight = (int) Math.round(font.getSize() * 1.05);
        int lineGap = (int) Math.round(font.getSize() * lineGapRatio);

        for (String line : lines) {
            GlyphVector gv = font.createGlyphVector(frc, line);
            Rectangle2D b = gv.getVisualBounds();
            int lineW = (int) Math.ceil(b.getWidth() + stroke * 2);
            width = Math.max(width, lineW);
        }
        int height = lines.size() * (lineHeight + lineGap) - lineGap;
        return new Dimension(width, height);
    }

    private static void copyImageToClipboard(Image img) {
        Transferable t = new ImageSelection(img);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
    }

    private static class ImageSelection implements Transferable {
        private final Image image;
        public ImageSelection(Image image) { this.image = image; }
        @Override public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{DataFlavor.imageFlavor}; }
        @Override public boolean isDataFlavorSupported(DataFlavor flavor) { return DataFlavor.imageFlavor.equals(flavor); }
        @Override public Object getTransferData(DataFlavor flavor) { return image; }
    }
}
