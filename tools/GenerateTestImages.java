import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenerateTestImages {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 400;
    private static final String ROOT_NAME = "ImageManager-Test-Images";
    private static final Path OUTPUT_DIR = Path.of("test-data", ROOT_NAME);
    private static final Path ZIP_PATH = Path.of("test-data", ROOT_NAME + ".zip");

    private record ImageSpec(String folder, String title, String subtitle, int theme) {
    }

    public static void main(String[] args) throws Exception {
        recreateOutputDirectory();
        List<ImageSpec> specs = buildSpecs();
        List<Path> pngFiles = new ArrayList<>();
        for (int i = 0; i < specs.size(); i++) {
            ImageSpec spec = specs.get(i);
            Path target = OUTPUT_DIR.resolve(spec.folder()).resolve(String.format("img-%03d.png", i + 1));
            Files.createDirectories(target.getParent());
            writeImage(target, spec, i + 1);
            pngFiles.add(target);
        }
        zipDirectory();
        writeReadme();
        report(pngFiles);
    }

    private static void recreateOutputDirectory() throws IOException {
        if (Files.exists(OUTPUT_DIR)) {
            try (var stream = Files.walk(OUTPUT_DIR)) {
                stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to delete " + path, e);
                    }
                });
            }
        }
        Files.createDirectories(OUTPUT_DIR);
        Files.deleteIfExists(ZIP_PATH);
    }

    private static List<ImageSpec> buildSpecs() {
        String[] folders = {
                "01_Common/level-1",
                "01_Common/level-1/level-2",
                "02_Notes/java",
                "02_Notes/java/web",
                "03_ReadLater/cloud",
                "03_ReadLater/cloud/deployment",
                "04_Favorite/ui",
                "04_Favorite/ui/cards",
                "05_Funny/stickers",
                "05_Funny/stickers/cats",
                "06_Nested/level1/level2/level3",
                "07_Chinese_Path_Test/\u5b50\u76ee\u5f55\u6d4b\u8bd5"
        };
        String[] titles = {
                "Common Note", "Star Memo", "Cloud Icon", "Study Card", "Java Window",
                "Servlet Flow", "Green Dashboard", "Cute Robot", "Rocket Deploy", "Folder Sync",
                "Mobile Upload", "Image Card", "Search Panel", "Favorite UI", "Funny Face",
                "Tiny Cat", "Moon Note", "Mountain File", "Tag Bubble", "Code Review",
                "Health Check", "R2 Object", "Aiven DB", "Railway App", "Pages Site",
                "Clipboard", "Batch Upload", "Source Path", "Time Filter", "Sort Menu",
                "Nested One", "Nested Two", "Nested Three", "Folder Tree", "Sync Result",
                "Delete Missing", "Common Tag", "Notes Tag", "Read Later", "Favorite Tag",
                "Funny Sticker", "Cloud Deploy", "Local Path", "Image Detail", "Upload Zone",
                "Card Grid", "Dark Theme", "Green Button", "Chinese Path", "Final Draft"
        };
        List<ImageSpec> specs = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            specs.add(new ImageSpec(folders[i % folders.length], titles[i], "IMG " + String.format("%03d", i + 1), i % 12));
        }
        return specs;
    }

    private static void writeImage(Path target, ImageSpec spec, int index) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color[] palette = palette(spec.theme());
            g.setPaint(new GradientPaint(0, 0, palette[0], WIDTH, HEIGHT, palette[1]));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            drawBackgroundPattern(g, palette, index);
            drawScene(g, spec.theme(), palette, index);
            drawTitle(g, spec, palette);
        } finally {
            g.dispose();
        }
        ImageIO.write(image, "png", target.toFile());
    }

    private static Color[] palette(int theme) {
        Color[][] palettes = {
                {new Color(26, 35, 39), new Color(42, 72, 50), new Color(118, 185, 0), new Color(232, 255, 195)},
                {new Color(31, 27, 46), new Color(71, 43, 86), new Color(255, 194, 87), new Color(255, 247, 210)},
                {new Color(16, 36, 59), new Color(37, 91, 116), new Color(113, 207, 255), new Color(225, 248, 255)},
                {new Color(45, 27, 28), new Color(109, 44, 52), new Color(255, 101, 132), new Color(255, 230, 235)},
                {new Color(20, 38, 36), new Color(34, 92, 78), new Color(89, 230, 177), new Color(225, 255, 244)},
                {new Color(37, 33, 24), new Color(97, 74, 29), new Color(250, 208, 77), new Color(255, 246, 200)}
        };
        return palettes[theme % palettes.length];
    }

    private static void drawBackgroundPattern(Graphics2D g, Color[] palette, int index) {
        g.setColor(withAlpha(palette[3], 35));
        for (int i = 0; i < 18; i++) {
            int x = (i * 73 + index * 19) % WIDTH;
            int y = (i * 41 + index * 31) % HEIGHT;
            int size = 18 + (i % 5) * 9;
            if (i % 2 == 0) {
                g.fill(new Ellipse2D.Double(x, y, size, size));
            } else {
                g.fillRoundRect(x, y, size + 22, size, 14, 14);
            }
        }
        g.setStroke(new BasicStroke(2f));
        g.setColor(withAlpha(palette[2], 70));
        for (int i = 0; i < 5; i++) {
            g.drawArc(40 + i * 110, 26 + i * 23, 130, 70, 20, 190);
        }
    }

    private static void drawScene(Graphics2D g, int theme, Color[] palette, int index) {
        int scene = theme % 12;
        switch (scene) {
            case 0 -> drawClouds(g, palette);
            case 1 -> drawStars(g, palette);
            case 2 -> drawRobot(g, palette);
            case 3 -> drawCodeWindow(g, palette);
            case 4 -> drawNotebook(g, palette);
            case 5 -> drawPhone(g, palette);
            case 6 -> drawFolder(g, palette);
            case 7 -> drawImageCards(g, palette);
            case 8 -> drawHills(g, palette);
            case 9 -> drawRocket(g, palette);
            case 10 -> drawChat(g, palette);
            default -> drawCat(g, palette, index);
        }
    }

    private static void drawClouds(Graphics2D g, Color[] palette) {
        g.setColor(withAlpha(Color.WHITE, 210));
        fillCloud(g, 130, 130, 95);
        fillCloud(g, 330, 170, 120);
        g.setColor(palette[2]);
        g.fillOval(250, 105, 42, 42);
    }

    private static void drawStars(Graphics2D g, Color[] palette) {
        g.setColor(palette[2]);
        drawStar(g, 190, 145, 60);
        drawStar(g, 360, 175, 82);
        g.setColor(withAlpha(palette[3], 210));
        drawStar(g, 470, 100, 38);
    }

    private static void drawRobot(Graphics2D g, Color[] palette) {
        g.setColor(withAlpha(Color.WHITE, 230));
        g.fillRoundRect(230, 95, 180, 145, 24, 24);
        g.setColor(palette[0]);
        g.fillOval(270, 145, 24, 24);
        g.fillOval(348, 145, 24, 24);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(290, 202, 350, 202);
        g.setColor(palette[2]);
        g.fillRoundRect(285, 65, 70, 18, 12, 12);
        g.drawLine(320, 84, 320, 96);
    }

    private static void drawCodeWindow(Graphics2D g, Color[] palette) {
        g.setColor(withAlpha(Color.BLACK, 130));
        g.fillRoundRect(115, 72, 410, 205, 22, 22);
        g.setColor(withAlpha(Color.WHITE, 235));
        g.fillRoundRect(130, 92, 380, 165, 18, 18);
        g.setColor(palette[0]);
        for (int i = 0; i < 6; i++) {
            g.fillRoundRect(165, 125 + i * 20, 90 + i * 32, 8, 8, 8);
        }
        g.setColor(palette[2]);
        g.fillOval(150, 112, 12, 12);
        g.fillOval(170, 112, 12, 12);
        g.fillOval(190, 112, 12, 12);
    }

    private static void drawNotebook(Graphics2D g, Color[] palette) {
        g.setColor(withAlpha(Color.WHITE, 230));
        g.fillRoundRect(205, 78, 235, 205, 24, 24);
        g.setColor(palette[2]);
        g.fillRoundRect(205, 78, 48, 205, 22, 22);
        g.setColor(palette[0]);
        for (int y = 120; y < 235; y += 26) {
            g.fillRoundRect(280, y, 120, 7, 7, 7);
        }
    }

    private static void drawPhone(Graphics2D g, Color[] palette) {
        g.setColor(withAlpha(Color.BLACK, 160));
        g.fillRoundRect(235, 58, 170, 245, 30, 30);
        g.setColor(withAlpha(Color.WHITE, 235));
        g.fillRoundRect(252, 82, 136, 187, 18, 18);
        g.setColor(palette[2]);
        g.fillRoundRect(275, 114, 90, 58, 18, 18);
        g.fillRoundRect(275, 190, 90, 24, 12, 12);
        g.setColor(palette[3]);
        g.fillOval(313, 277, 16, 16);
    }

    private static void drawFolder(Graphics2D g, Color[] palette) {
        g.setColor(withAlpha(Color.BLACK, 115));
        g.fillRoundRect(148, 125, 360, 150, 24, 24);
        g.setColor(palette[2]);
        g.fillRoundRect(160, 105, 145, 55, 18, 18);
        g.setColor(palette[3]);
        g.fillRoundRect(160, 136, 330, 150, 24, 24);
    }

    private static void drawImageCards(Graphics2D g, Color[] palette) {
        for (int i = 0; i < 3; i++) {
            int x = 145 + i * 95;
            int y = 95 + i * 28;
            g.setColor(withAlpha(Color.WHITE, 230));
            g.fillRoundRect(x, y, 190, 120, 18, 18);
            g.setColor(palette[2]);
            g.fillOval(x + 22, y + 20, 32, 32);
            g.setColor(palette[0]);
            Path2D hill = new Path2D.Double();
            hill.moveTo(x + 20, y + 98);
            hill.lineTo(x + 74, y + 62);
            hill.lineTo(x + 112, y + 94);
            hill.lineTo(x + 152, y + 55);
            hill.lineTo(x + 172, y + 98);
            hill.closePath();
            g.fill(hill);
        }
    }

    private static void drawHills(Graphics2D g, Color[] palette) {
        g.setColor(palette[2]);
        g.fillOval(88, 178, 260, 170);
        g.setColor(withAlpha(palette[3], 220));
        g.fillOval(250, 148, 310, 205);
        g.setColor(withAlpha(Color.WHITE, 220));
        g.fillOval(380, 76, 70, 70);
    }

    private static void drawRocket(Graphics2D g, Color[] palette) {
        AffineTransform old = g.getTransform();
        g.translate(315, 170);
        g.rotate(-0.45);
        g.setColor(withAlpha(Color.WHITE, 235));
        g.fillRoundRect(-38, -95, 76, 170, 38, 38);
        g.setColor(palette[2]);
        g.fillOval(-22, -55, 44, 44);
        g.setColor(palette[3]);
        Path2D flame = new Path2D.Double();
        flame.moveTo(-24, 72);
        flame.lineTo(0, 130);
        flame.lineTo(24, 72);
        flame.closePath();
        g.fill(flame);
        g.setTransform(old);
    }

    private static void drawChat(Graphics2D g, Color[] palette) {
        g.setColor(withAlpha(Color.WHITE, 230));
        g.fillRoundRect(135, 95, 245, 105, 28, 28);
        g.fillRoundRect(260, 178, 230, 92, 28, 28);
        g.setColor(palette[2]);
        g.fillOval(178, 135, 15, 15);
        g.fillOval(215, 135, 15, 15);
        g.fillOval(252, 135, 15, 15);
    }

    private static void drawCat(Graphics2D g, Color[] palette, int index) {
        g.setColor(withAlpha(palette[3], 235));
        Path2D leftEar = new Path2D.Double();
        leftEar.moveTo(225, 125);
        leftEar.lineTo(255, 66);
        leftEar.lineTo(285, 128);
        leftEar.closePath();
        g.fill(leftEar);
        Path2D rightEar = new Path2D.Double();
        rightEar.moveTo(355, 128);
        rightEar.lineTo(385, 66);
        rightEar.lineTo(415, 125);
        rightEar.closePath();
        g.fill(rightEar);
        g.fillOval(210, 105, 220, 165);
        g.setColor(palette[0]);
        g.fillOval(270, 165, 18, 22);
        g.fillOval(352, 165, 18, 22);
        g.setColor(palette[2]);
        g.fillOval(312, 195, 18, 12);
        g.setStroke(new BasicStroke(3f));
        g.drawLine(321, 207, 321, 224);
        g.drawArc(288, 210, 35, 24, 205, 110);
        g.drawArc(320, 210, 35, 24, 225, 110);
        if (index % 2 == 0) {
            g.drawLine(240, 195, 190, 180);
            g.drawLine(240, 215, 190, 220);
            g.drawLine(400, 195, 450, 180);
            g.drawLine(400, 215, 450, 220);
        }
    }

    private static void drawTitle(Graphics2D g, ImageSpec spec, Color[] palette) {
        g.setColor(withAlpha(Color.BLACK, 120));
        g.fillRoundRect(34, 306, 572, 62, 22, 22);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.drawString(spec.title(), 58, 344);
        g.setColor(palette[3]);
        g.setFont(new Font("SansSerif", Font.PLAIN, 17));
        g.drawString(spec.subtitle(), 58, 364);
    }

    private static void fillCloud(Graphics2D g, int x, int y, int size) {
        g.fillOval(x, y + size / 4, size, size / 2);
        g.fillOval(x + size / 4, y, size / 2, size / 2);
        g.fillOval(x + size / 2, y + size / 8, size / 2, size / 2);
        g.fillRoundRect(x + size / 8, y + size / 3, size, size / 3, 24, 24);
    }

    private static void drawStar(Graphics2D g, int cx, int cy, int radius) {
        Path2D star = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            double r = i % 2 == 0 ? radius : radius * 0.42;
            double x = cx + Math.cos(angle) * r;
            double y = cy + Math.sin(angle) * r;
            if (i == 0) {
                star.moveTo(x, y);
            } else {
                star.lineTo(x, y);
            }
        }
        star.closePath();
        g.fill(star);
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private static void zipDirectory() throws IOException {
        Files.createDirectories(ZIP_PATH.getParent());
        try (OutputStream outputStream = Files.newOutputStream(ZIP_PATH);
             ZipOutputStream zip = new ZipOutputStream(outputStream);
             var stream = Files.walk(OUTPUT_DIR)) {
            for (Path path : stream.filter(Files::isRegularFile).toList()) {
                Path relative = OUTPUT_DIR.getParent().relativize(path);
                zip.putNextEntry(new ZipEntry(relative.toString().replace("\\", "/")));
                Files.copy(path, zip);
                zip.closeEntry();
            }
        }
    }

    private static void writeReadme() throws IOException {
        String text = """
                ImageManager Folder Sync test images

                1. Extract ImageManager-Test-Images.zip to the D drive root.
                2. After extraction, the folder should be:
                   D:\\ImageManager-Test-Images
                3. Create a Folder Preset in ImageManager:
                   Name: Test Images
                   Source Path: D:/ImageManager-Test-Images
                4. Authorize the folder in Folder Sync.
                5. Click Sync, review the scan result, then Apply Sync.
                6. Delete several local images, scan again, check:
                   Delete cloud images missing from this folder
                   Then Apply Sync to test cloud cleanup.
                7. The 07_Chinese_Path_Test folder contains a Chinese-named subfolder for path compatibility testing.
                """;
        Files.writeString(Path.of("test-data", "README-Test-Images.txt"), text);
    }

    private static void report(List<Path> pngFiles) throws IOException {
        long oversized = 0;
        for (Path file : pngFiles) {
            if (Files.size(file) >= 500L * 1024L) {
                oversized++;
            }
        }
        System.out.println("PNG count: " + pngFiles.size());
        System.out.println("Oversized PNG count: " + oversized);
        System.out.println("ZIP path: " + ZIP_PATH.toAbsolutePath());
        System.out.println("ZIP size: " + Files.size(ZIP_PATH) + " bytes");
        System.out.println("Output folder: " + OUTPUT_DIR.toAbsolutePath());
    }
}
