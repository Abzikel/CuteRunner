package com.abzikel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Menu extends JFrame {
    private final List<Image> idleSprites;
    private final Image background;
    private int currentSpriteIndex = 0;
    private int sawCount = 0;

    public Menu() {
        // Window configuration
        setTitle("Cute Runner - Menu");
        setSize(800, 600);
        setResizable(false);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load sprites and background image
        idleSprites = loadIdleSprites();
        background = loadImage();

        // Main panel to adjust to the window
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Draw background image
                if (background != null)
                    g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

                // Draw current sprite
                if (!idleSprites.isEmpty())
                    g.drawImage(idleSprites.get(currentSpriteIndex), 100, 100, this);

                // Draw shapes
                drawShapes(g);
            }
        };

        // Grid Bag Layout to center the buttons
        mainPanel.setLayout(new GridBagLayout());

        // Create button and add them to the panel
        JButton playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 24));
        playButton.addActionListener(e -> {
            // Dispose window and show GameWindow
            dispose();
            new GameWindow(sawCount);
        });

        JButton rulesButton = new JButton("Rules");
        rulesButton.setFont(new Font("Arial", Font.BOLD, 24));
        rulesButton.addActionListener(e -> showRules());

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 24));
        exitButton.addActionListener(e -> System.exit(0));

        // Find the maximum width among the buttons
        int maxWidth = Math.max(
                Math.max(playButton.getPreferredSize().width, rulesButton.getPreferredSize().width),
                exitButton.getPreferredSize().width
        );

        // Set all buttons to the maximum width
        Dimension buttonSize = new Dimension(maxWidth, playButton.getPreferredSize().height);
        playButton.setPreferredSize(buttonSize);
        rulesButton.setPreferredSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);

        // Use GridBagConstraints to center the buttons
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        // Add Play Button
        gbc.gridy = 0;
        mainPanel.add(playButton, gbc);

        // Add Rules Button
        gbc.gridy = 1;
        mainPanel.add(rulesButton, gbc);

        // Add Exit Button
        gbc.gridy = 2;
        mainPanel.add(exitButton, gbc);

        // Start idle animation
        startAnimation(mainPanel);

        // Add main panel to the window
        getContentPane().add(mainPanel);
        setVisible(true);
    }

    private List<Image> loadIdleSprites() {
        // Create temporal List<Image>
        List<Image> sprites = new ArrayList<>();
        for (int index = 1; index <= 16; index++) {
            // Get path and add the ImageIcon to the List
            String path = String.format("/sprites/Idle (%d).png", index);
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(path)));
            sprites.add(icon.getImage());
        }

        return sprites;
    }

    private Image loadImage() {
        // Load image from a specific path
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/menu_background.png"))).getImage();
    }

    private void showRules() {
        while (true) {
            // Create the String input for the JOptionPane
            String input = JOptionPane.showInputDialog(
                    this,
                    """
                            To win, avoid 'x' number of saws.
                            Enter 0 for infinite mode.
                            
                            Enter the number of saws:""",
                    "Game Rules",
                    JOptionPane.QUESTION_MESSAGE
            );

            try {
                // Verify if the value is a number
                int value = Integer.parseInt(input);
                if (value >= 0) {
                    sawCount = value; // Store the valid number of saws
                    break;
                } else {
                    // Show error message
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a number greater than or equal to 0.",
                            "Invalid Input",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            } catch (NumberFormatException e) {
                // Show error message
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid number.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    private void drawShapes(Graphics g) {
        // Define Graphics2D
        Graphics2D g2d = (Graphics2D) g;

        // Variables para el tamaño y la ubicación
        int size = 20;
        int padding = 10;
        int startX = padding;
        int startY = padding;

        // Dibujar líneas con diferentes patrones y colores
        drawPatternedLine(g2d, startX, startY, startX + 100, startY, Color.RED, new float[]{1.0f}); // Sólido
        drawPatternedLine(g2d, startX, startY + 30, startX + 100, startY + 30, Color.BLUE, new float[]{5.0f, 5.0f}); // Punteada
        drawPatternedLine(g2d, startX, startY + 60, startX + 100, startY + 60, Color.GREEN, new float[]{10.0f, 10.0f}); // Discontinua

        // Variables para dibujar las figuras existentes a la derecha
        int x = getWidth() - size - padding - 20;

        // Definición de colores
        Color[] colors = {
                new Color(0xBD3B1B),
                new Color(0xD8A800),
                new Color(0xB9D870),
                new Color(0xB6C61A),
                new Color(0x006344)
        };

        // Dibujar cuadrado
        g2d.setColor(colors[0]);
        g2d.fillRect(x, padding, size, size);

        // Dibujar rectángulo
        g2d.setColor(colors[1]);
        g2d.fillRect(x, padding + size + padding, size * 2, size);

        // Dibujar círculo
        g2d.setColor(colors[2]);
        g2d.fillOval(x, padding + (size + padding) * 2, size, size);

        // Dibujar óvalo
        g2d.setColor(colors[3]);
        g2d.fillOval(x, padding + (size + padding) * 3, size * 2, size);

        // Dibujar triángulo
        int y = padding + (size + padding) * 4 + size;
        g2d.setColor(colors[4]);
        Polygon triangle = new Polygon();
        triangle.addPoint(x + size / 2, padding + (size + padding) * 4);
        triangle.addPoint(x, y);
        triangle.addPoint(x + size, y);
        g2d.fillPolygon(triangle);
    }

    private void drawPatternedLine(Graphics2D g2d, int x1, int y1, int x2, int y2, Color color, float[] dashPattern) {
        // Establecer color y patrón de línea
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dashPattern, 0));

        // Dibujar la línea
        g2d.drawLine(x1, y1, x2, y2);
    }


    private void setCustomCursor() {
        // Load the cursor image
        String cursorPath = "/images/cursor.png";
        ImageIcon cursorIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(cursorPath)));
        Image cursorImage = cursorIcon.getImage();

        // Create a new cursor with the loaded image
        Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImage, new Point(0, 0), "Cute Cursor"
        );

        // Set the custom cursor for the window
        setCursor(customCursor);
    }

    private void startAnimation(JPanel panel) {
        // Cycle of sprites
        Timer animationTimer = new Timer(100, e -> {
            // Cycle of sprites
            currentSpriteIndex = (currentSpriteIndex + 1) % idleSprites.size();
            panel.repaint();
        });

        // Start time
        animationTimer.start();
    }

}
