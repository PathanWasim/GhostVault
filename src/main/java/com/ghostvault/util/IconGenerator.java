package com.ghostvault.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Generates application icons programmatically in various sizes
 */
public class IconGenerator {
    
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // Professional blue
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219); // Lighter blue
    private static final Color ACCENT_COLOR = new Color(231, 76, 60); // Red accent
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241); // Light gray
    
    public static void main(String[] args) {
        IconGenerator generator = new IconGenerator();
        try {
            generator.generateAllIcons();
            System.out.println("✓ All icons generated successfully!");
        } catch (IOException e) {
            System.err.println("Error generating icons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void generateAllIcons() throws IOException {
        // Create icons directory if it doesn't exist
        File iconsDir = new File("src/main/resources/icons");
        if (!iconsDir.exists()) {
            iconsDir.mkdirs();
        }
        
        // Generate different sizes for different purposes
        List<Integer> sizes = Arrays.asList(16, 24, 32, 48, 64, 128, 256, 512);
        
        for (int size : sizes) {
            BufferedImage icon = generateIcon(size);
            File outputFile = new File(iconsDir, "ghostvault_" + size + ".png");
            ImageIO.write(icon, "PNG", outputFile);
            System.out.println("Generated: " + outputFile.getName());
        }
        
        // Generate Windows ICO file (contains multiple sizes)
        generateWindowsIcon(iconsDir);
        
        // Generate macOS ICNS file placeholder
        generateMacIcon(iconsDir);
        
        // Generate favicon for web
        BufferedImage favicon = generateIcon(32);
        File faviconFile = new File(iconsDir, "favicon.ico");
        ImageIO.write(favicon, "PNG", new File(iconsDir, "favicon.png"));
        System.out.println("Generated: favicon.png");
        
        // Generate logo for documentation
        BufferedImage logo = generateLogo(512, 256);
        File logoFile = new File("docs");
        if (!logoFile.exists()) {
            logoFile.mkdirs();
        }
        ImageIO.write(logo, "PNG", new File(logoFile, "logo.png"));
        System.out.println("Generated: docs/logo.png");
    }
    
    private BufferedImage generateIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Create gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, PRIMARY_COLOR,
            size, size, SECONDARY_COLOR
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, size, size, size/8, size/8);
        
        // Draw vault/safe icon
        drawVaultIcon(g2d, size);
        
        g2d.dispose();
        return image;
    }
    
    private void drawVaultIcon(Graphics2D g2d, int size) {
        // Set up for drawing the vault door
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(size / 20f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Draw outer vault door circle
        int margin = size / 8;
        int doorSize = size - (2 * margin);
        g2d.drawOval(margin, margin, doorSize, doorSize);
        
        // Draw inner circle (vault door detail)
        int innerMargin = size / 5;
        int innerSize = size - (2 * innerMargin);
        g2d.setStroke(new BasicStroke(size / 30f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawOval(innerMargin, innerMargin, innerSize, innerSize);
        
        // Draw lock mechanism (combination dial)
        int centerX = size / 2;
        int centerY = size / 2;
        int dialRadius = size / 6;
        
        // Draw center dial
        g2d.setStroke(new BasicStroke(size / 25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawOval(centerX - dialRadius, centerY - dialRadius, dialRadius * 2, dialRadius * 2);
        
        // Draw dial marks
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            int x1 = centerX + (int) (Math.cos(angle) * dialRadius * 0.7);
            int y1 = centerY + (int) (Math.sin(angle) * dialRadius * 0.7);
            int x2 = centerX + (int) (Math.cos(angle) * dialRadius * 0.9);
            int y2 = centerY + (int) (Math.sin(angle) * dialRadius * 0.9);
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Draw handle
        g2d.setStroke(new BasicStroke(size / 18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int handleRadius = size / 4;
        g2d.drawArc(centerX - handleRadius, centerY - handleRadius / 2, 
                    handleRadius * 2, handleRadius, 
                    30, 120);
        
        // Add ghost effect (transparency)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.fillOval(margin + doorSize/4, margin + doorSize/8, doorSize/3, doorSize/3);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    private BufferedImage generateLogo(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        // Clear background
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        
        // Draw icon on left
        int iconSize = height - 40;
        BufferedImage icon = generateIcon(iconSize);
        g2d.drawImage(icon, 20, 20, null);
        
        // Draw text on right
        g2d.setColor(PRIMARY_COLOR);
        
        // Draw "GhostVault" text
        Font titleFont = new Font("Arial", Font.BOLD, height / 3);
        g2d.setFont(titleFont);
        g2d.drawString("GhostVault", iconSize + 40, height / 2);
        
        // Draw tagline
        Font taglineFont = new Font("Arial", Font.PLAIN, height / 8);
        g2d.setFont(taglineFont);
        g2d.setColor(SECONDARY_COLOR);
        g2d.drawString("Secure File Vault", iconSize + 40, height / 2 + height / 4);
        
        g2d.dispose();
        return image;
    }
    
    private void generateWindowsIcon(File iconsDir) throws IOException {
        // For Windows, we'll create a multi-size PNG that can be converted to ICO
        // Windows typically uses 16, 32, 48, 256 sizes
        BufferedImage icon256 = generateIcon(256);
        File icoFile = new File(iconsDir, "ghostvault.ico");
        
        // For now, save as PNG (would need ICO library for proper ICO format)
        ImageIO.write(icon256, "PNG", new File(iconsDir, "ghostvault_windows.png"));
        System.out.println("Generated: ghostvault_windows.png (convert to .ico using external tool)");
    }
    
    private void generateMacIcon(File iconsDir) throws IOException {
        // For macOS, generate the largest size
        BufferedImage icon1024 = generateIcon(1024);
        File icnsFile = new File(iconsDir, "ghostvault.icns");
        
        // For now, save as PNG (would need ICNS library for proper ICNS format)
        ImageIO.write(icon1024, "PNG", new File(iconsDir, "ghostvault_mac.png"));
        System.out.println("Generated: ghostvault_mac.png (convert to .icns using external tool)");
    }
}