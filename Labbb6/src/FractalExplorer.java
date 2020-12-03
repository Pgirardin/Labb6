import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

class FractalExplorer {
    private int dimensionDisplay;
    private JImageDisplay image;
    private FractalGenerator generator;
    private Rectangle2D.Double complexRange;
    private int rowsRemaining;
    private JComboBox comboBox;
    private JButton resetImageButton;
    private JButton saveImageButton;

    public static void main(String args[]) {
        FractalExplorer start = new FractalExplorer(800);
        start.createAndShowGUI();
        start.drawFractal();
    }

    public FractalExplorer(int dimension) {
        dimensionDisplay = dimension;
        complexRange = new Rectangle2D.Double();
        generator = new Mandelbrot();
        generator.getInitialRange(complexRange);
        image = new JImageDisplay(dimensionDisplay, dimensionDisplay);
        return;
    }

    public void createAndShowGUI() {
        image.setLayout(new BorderLayout());

        JFrame frame = new JFrame("Fractals");
        frame.add(image, BorderLayout.CENTER);

        Mouse click = new Mouse();
        image.addMouseListener(click);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ButtonHandler handler = new ButtonHandler();

        comboBox = new JComboBox();
        comboBox.addItem( new Mandelbrot() );
        comboBox.addItem( new Tricorn() );
        comboBox.addItem( new BurningShip() );
        comboBox.addActionListener(handler);
        JLabel labelDescription = new JLabel("Fractal:");
        JPanel panel = new JPanel();
        panel.add(labelDescription);
        panel.add(comboBox);
        frame.add(panel, BorderLayout.NORTH);

        resetImageButton = new JButton("reset");
        resetImageButton.addActionListener(handler);
        saveImageButton = new JButton("save");
        saveImageButton.addActionListener(handler);

        JPanel panelDowner = new JPanel();
        panelDowner.add(resetImageButton);
        panelDowner.add(saveImageButton);
        frame.add(panelDowner, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public void enableUI(boolean swit) {
        comboBox.setEnabled(swit);
        resetImageButton.setEnabled(swit);
        saveImageButton.setEnabled(swit);
    }

    private void drawFractal() {
        enableUI(false);
        rowsRemaining = dimensionDisplay;

        for (int y = 0; y < dimensionDisplay; y += 1){
            FractalWorker draw = new FractalWorker(y);
            draw.execute();
        }
    }

    private class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("save")) {
                JFileChooser finder = new JFileChooser();
                JFileChooser chooser = new JFileChooser();
                FileFilter filter = new FileNameExtensionFilter("PNG Images", "png");

                chooser.setFileFilter(filter);
                chooser.setAcceptAllFileFilterUsed(false);
                int result = finder.showSaveDialog(image);

                if (result == finder.APPROVE_OPTION) {
                    File file = finder.getSelectedFile();
                    try {
                        ImageIO.write(image.getImage(), "png", file);

                    } catch(Exception exception) {
                        JOptionPane.showMessageDialog(image, exception.getMessage(),
                        "Cannot Save Image", JOptionPane.ERROR_MESSAGE);
                    }
                }
                return;
            }
            if (e.getSource() instanceof JComboBox) {
                JComboBox mySource = (JComboBox)e.getSource();
                generator = (FractalGenerator)mySource.getSelectedItem();
            }
            generator.getInitialRange(complexRange);
            drawFractal();
        }
    }

    private class Mouse extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (rowsRemaining != 0) return;

            int x = e.getX();
            double xCoord = FractalGenerator.getCoord(complexRange.x, complexRange.x 
            + complexRange.width, dimensionDisplay, x);
            int y = e.getY();
            double yCoord = FractalGenerator.getCoord(complexRange.y, complexRange.y 
            + complexRange.height, dimensionDisplay, y);
            generator.recenterAndZoomRange(complexRange, xCoord, yCoord, 0.5);

            drawFractal();
        }
    }

    private class FractalWorker extends SwingWorker<Object, Object> {
        private int y;
        int[] rgb;
        public FractalWorker(int yCoord) {
            y = yCoord;
        }
        public Object doInBackground() {
            rgb = new int[dimensionDisplay];
            for (int x = 0; x < dimensionDisplay; x += 1){
                double xCoord = FractalGenerator.getCoord(complexRange.x, complexRange.x
                + complexRange.width, dimensionDisplay, x);
                double yCoord = FractalGenerator.getCoord(complexRange.y, complexRange.y + complexRange.height,
                        dimensionDisplay, y);
                int numIters = generator.numIterations(xCoord, yCoord);

                if (numIters == -1) {
                    rgb[x] = 0;
                } else {
                    float hue = 0.7f + (float) numIters / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                    rgb[x] = rgbColor;
                }

            }

            return null;
        }
        public void done() {
            for (int i = 0; i < dimensionDisplay; i += 1) {
                image.drawPixel(i, y, rgb[i]);
            }
            image.repaint(0, 0, y, dimensionDisplay, 1);

            rowsRemaining--;
            if (rowsRemaining == 0) {
                enableUI(true);
            }
        }
    }

}