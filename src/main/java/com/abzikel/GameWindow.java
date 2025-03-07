package com.abzikel;

import com.abzikel.pojos.Cloud;
import com.abzikel.pojos.Obstacle;
import com.abzikel.utils.CursorUtil;
import com.abzikel.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameWindow extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int GROUND = 512;
    private static final int FEET = 400;
    private final List<Image> runSprites = new ArrayList<>();
    private final List<Image> jumpSprites = new ArrayList<>();
    private final List<Image> deathSprites = new ArrayList<>();
    private final List<Cloud> clouds = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final Image background, obstacleImage;
    private TexturePaint texturePaint;
    private final int obstacleCount;
    private int backgroundPosition, nextBackgroundPosition;
    private int currentFrame = 0;
    private int characterPositionAxisY;
    private int jumpVelocity = 0;
    private int obstaclesDodged = 0;
    private boolean isJumping = false;
    private boolean isGameOver = false;

    public GameWindow(int obstacleCount) {
        // Window configuration
        setTitle("Cute Runner - Game");
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize obstacle count
        this.obstacleCount = obstacleCount;

        // Load images
        background = ImageUtil.loadImage("/images/background_game.png");
        obstacleImage = ImageUtil.loadImage("/images/obstacle.png");

        // Load sprites
        ImageUtil.loadSprites(runSprites, 20, "Run");
        ImageUtil.loadSprites(jumpSprites, 30, "Jump");
        ImageUtil.loadSprites(deathSprites, 30, "Dead");

        // Initialize character position
        characterPositionAxisY = FEET;

        // Set initial background positions
        backgroundPosition = 0;
        nextBackgroundPosition = WIDTH;

        // Create the texture to be applied over the gradient
        createTexture();

        // Create initial clouds
        createClouds();

        // Create obstacles
        createObstacles();

        // Create and configure the main panel
        JPanel gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGradientBackground(g); // Draw gradient background
                drawTexturizeOverlay(g);   // Apply texture overlay
                drawBackground(g);         // Draw ground images
                drawClouds(g);             // Draw clouds
                drawObstacles(g);          // Draw obstacles
                drawCharacter(g);          // Draw the character
                drawScore(g);              // Draw score
            }
        };

        // Apply the normal cursor to the main panel
        CursorUtil.applyNormalCursor(gamePanel);

        // Add a mouse listener to handle click events globally in this panel
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                CursorUtil.applyClickCursor(gamePanel);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Timer timer = new Timer(200, event -> {
                    CursorUtil.applyNormalCursor(gamePanel);
                    ((Timer) event.getSource()).stop(); // Stop the timer after execution
                });
                timer.setRepeats(false); // Ensure the timer runs only once
                timer.start();
            }
        });

        // Start animations
        startAnimations(gamePanel);

        // Add key listener for jump action
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                    initiateJump();
                }
            }
        });

        // Add panel to the window and display it
        getContentPane().add(gamePanel);
        setVisible(true);
    }

    private void startAnimations(JPanel panel) {
        Timer timer = new Timer(20, e -> {
            if (!isGameOver) {
                updateGameObjects();
                checkCollision();
                checkWinCondition();
            }
            panel.repaint();
        });
        timer.start();
    }

    private void updateGameObjects() {
        // Move the background to the left
        backgroundPosition -= 5;
        nextBackgroundPosition -= 5;

        // Reset background position when it moves out of the screen
        if (backgroundPosition + getWidth() <= 0) backgroundPosition = nextBackgroundPosition + WIDTH;
        if (nextBackgroundPosition + getWidth() <= 0) nextBackgroundPosition = backgroundPosition + WIDTH;

        // Move character
        currentFrame = (currentFrame + 1) % (isJumping ? jumpSprites.size() : runSprites.size());
    }

    private void checkCollision() {
        // Define the horizontal range where collision can occur
        int minHorizontalRange = 70;
        int maxHorizontalRange = 130;

        // Define the safe vertical height for the character to avoid collision
        int safeCharacterHeight = 325;

        // Check each obstacle for potential collision with the character
        for (Obstacle obstacle : obstacles) {
            // Check if the obstacle is within the defined horizontal range
            if (obstacle.positionX >= minHorizontalRange && obstacle.positionX <= maxHorizontalRange) {
                // If the character is below the safe height, trigger game over
                if (characterPositionAxisY > safeCharacterHeight) {
                    triggerGameOver(); // Stop the game on collision
                    break;
                }
            }
        }
    }

    private void checkWinCondition() {
        if (obstacleCount > 0 && obstaclesDodged >= obstacleCount) {
            isGameOver = true;
            showEndGameDialog("Congratulations! You won by dodging " + obstaclesDodged + " obstacles.");
        }
    }

    private void triggerGameOver() {
        isGameOver = true;
        new Timer(100, e -> {
            currentFrame++;
            if (currentFrame >= deathSprites.size()) {
                ((Timer) e.getSource()).stop();
                showEndGameDialog("Game Over! You dodged " + obstaclesDodged + " obstacles.");
            }
            repaint();
        }).start();
    }

    private void showEndGameDialog(String message) {
        // Create a custom JPanel with padding for the message
        JPanel panel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>", SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add margins
        panel.add(messageLabel, BorderLayout.CENTER);

        // Create buttons
        JButton restartButton = new JButton("Restart");
        JButton menuButton = new JButton("Menu");

        // Button panel to hold the buttons horizontally
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(restartButton);
        buttonPanel.add(menuButton);

        // Add button panel to the main panel
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Create the dialog
        JDialog dialog = new JDialog(this, "Game Over", true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Add action listeners to the buttons
        restartButton.addActionListener(e -> {
            dialog.dispose();
            restartGame();
        });

        menuButton.addActionListener(e -> {
            dialog.dispose();
            returnToMenu();
        });

        dialog.setVisible(true);
    }

    private void restartGame() {
        dispose();
        new GameWindow(obstacleCount);  // Start a new game with the same obstacle count
    }

    private void returnToMenu() {
        dispose();
        new Menu();  // Return to the main menu
    }

    private void drawScore(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.BLACK);
        g.drawString("Obstacles Dodged: " + obstaclesDodged, 10, 30);
    }

    private void createClouds() {
        // Create clouds with random positions within the width of the screen
        Random rand = new Random();
        for (int index = 0; index < 20; index++) {
            int x = rand.nextInt(WIDTH);  // Random initial X position within the screen width
            int y = rand.nextInt(200);  // Random Y position in the upper part of the screen (0-200px)
            double scale = 0.5 + rand.nextDouble();  // Random scaling factor for the cloud size
            double speed = 2 + rand.nextDouble(3);  // Random speed for each cloud
            clouds.add(new Cloud(x, y, scale, speed));  // Add the new cloud to the list
        }
    }

    private void drawClouds(Graphics g) {
        Graphics2D originalG2D = (Graphics2D) g.create();

        for (Cloud cloud : clouds) {
            Graphics2D g2d = (Graphics2D) originalG2D.create();

            // Apply transformations
            AffineTransform transform = new AffineTransform();
            transform.translate(cloud.positionX, cloud.positionY);
            transform.scale(cloud.scale, cloud.scale);
            g2d.setTransform(transform);

            // Draw the cloud
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillOval(cloud.positionX, cloud.positionY, 100, 50);

            // Stop movement if the game is over
            if (!isGameOver) {
                cloud.positionX -= (int) cloud.speed;
                if (cloud.positionX + 100 < 0) {
                    cloud.positionX = WIDTH + 200;
                    cloud.positionY = new Random().nextInt(200);
                }
            }

            g2d.dispose();
        }

        originalG2D.dispose();
    }

    private void createObstacles() {
        while (obstacles.isEmpty()) {
            int positionX = WIDTH + 200; // Generate beyond screen
            int positionY = FEET + 40; // Generate near the ground
            obstacles.add(new Obstacle(positionX, positionY)); // Random speed
        }
    }

    private void drawObstacles(Graphics g) {
        Graphics2D originalG2D = (Graphics2D) g.create();

        for (Obstacle obstacle : obstacles) {
            Graphics2D g2d = (Graphics2D) originalG2D.create();

            // Apply transformations
            AffineTransform transform = new AffineTransform();
            transform.translate(obstacle.positionX, obstacle.positionY);
            g2d.setTransform(transform);

            // Draw the obstacle
            g2d.drawImage(obstacleImage, obstacle.positionX, obstacle.positionY, this);

            // Stop movement if the game is over
            if (!isGameOver) {
                obstacle.positionX -= 10;
                if (obstacle.positionX + obstacleImage.getWidth(this) < 0) {
                    obstacle.positionX = WIDTH + 200 + new Random().nextInt(WIDTH);
                    obstaclesDodged++;
                }
            }

            g2d.dispose();
        }

        originalG2D.dispose();
    }

    private void initiateJump() {
        // Verify if the character is already in the air
        if (!isJumping) {
            isJumping = true;
            jumpVelocity = -15; // Initial jump velocity
            new Timer(20, e -> {
                characterPositionAxisY += jumpVelocity;
                jumpVelocity += 1; // Gravity effect

                if (characterPositionAxisY >= FEET) {
                    characterPositionAxisY = FEET;
                    isJumping = false; // Reset jump state
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            }).start();
        }
    }

    private void createTexture() {
        // Create the texture
        BufferedImage textureImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = textureImage.createGraphics();
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.fillOval(4, 4, 4, 4);
        g2d.fillOval(12, 12, 2, 2);
        g2d.dispose();
        texturePaint = new TexturePaint(textureImage, new Rectangle(0, 0, 16, 16));
    }

    private void drawGradientBackground(Graphics g) {
        // Create the gradient
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 182, 193),
                0, height, new Color(221, 160, 221)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
    }

    private void drawTexturizeOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(texturePaint);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawBackground(Graphics g) {
        g.drawImage(background, backgroundPosition, HEIGHT - GROUND, WIDTH, GROUND, this);
        g.drawImage(background, nextBackgroundPosition, HEIGHT - GROUND, WIDTH, GROUND, this);
    }

    private void drawCharacter(Graphics g) {
        // Determine the correct sprite to draw
        Image currentImage;

        // Check if the death sprite should be drawn
        if (isGameOver && (obstacleCount == 0 || obstaclesDodged != obstacleCount)) {
            // Use death sprite if the game is over and either in infinite mode or the player hasn't won
            currentImage = deathSprites.get(Math.min(currentFrame, deathSprites.size() - 1));
            g.drawImage(currentImage, 100, characterPositionAxisY, 135, 110, this);  // Draw death sprite
        } else {
            // Use running or jumping sprites otherwise
            currentImage = isJumping
                    ? jumpSprites.get(currentFrame % jumpSprites.size())
                    : runSprites.get(currentFrame % runSprites.size());
            g.drawImage(currentImage, 100, characterPositionAxisY, 100, 100, this);  // Draw normal sprite
        }
    }

}
