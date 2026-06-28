import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
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

public class GenerateReportTestImages {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 900;
    private static final Path TEST_DATA = Path.of("test-data");
    private static final String FOLDER_SYNC_ROOT = "Report-Test-Images-FolderSync";
    private static final String UPLOAD_ROOT = "Report-Test-Images-Upload";

    private record ImageSpec(String root, String folder, String fileName, String title,
                             String subtitle, String note, int theme, String icon) {
    }

    private record GenerationReport(String root, int count, long minSize, long maxSize, Path zipPath) {
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(TEST_DATA);
        List<ImageSpec> specs = new ArrayList<>();
        addFolderSyncSpecs(specs);
        addUploadSpecs(specs);

        recreateDirectory(TEST_DATA.resolve(FOLDER_SYNC_ROOT));
        recreateDirectory(TEST_DATA.resolve(UPLOAD_ROOT));
        Files.deleteIfExists(TEST_DATA.resolve(FOLDER_SYNC_ROOT + ".zip"));
        Files.deleteIfExists(TEST_DATA.resolve(UPLOAD_ROOT + ".zip"));

        for (ImageSpec spec : specs) {
            Path output = TEST_DATA.resolve(spec.root()).resolve(spec.folder()).resolve(spec.fileName());
            Files.createDirectories(output.getParent());
            writeImage(output, spec);
        }

        GenerationReport folderReport = reportFor(FOLDER_SYNC_ROOT);
        GenerationReport uploadReport = reportFor(UPLOAD_ROOT);
        zipRoot(FOLDER_SYNC_ROOT);
        zipRoot(UPLOAD_ROOT);
        writeReadme();

        System.out.println("Folder Sync PNG count: " + folderReport.count());
        System.out.println("Upload PNG count: " + uploadReport.count());
        System.out.println("Total PNG count: " + (folderReport.count() + uploadReport.count()));
        System.out.println("Folder Sync size range: " + folderReport.minSize() + " - " + folderReport.maxSize() + " bytes");
        System.out.println("Upload size range: " + uploadReport.minSize() + " - " + uploadReport.maxSize() + " bytes");
        System.out.println("Folder Sync ZIP: " + folderReport.zipPath().toAbsolutePath());
        System.out.println("Upload ZIP: " + uploadReport.zipPath().toAbsolutePath());
    }

    private static void addFolderSyncSpecs(List<ImageSpec> specs) {
        add(specs, FOLDER_SYNC_ROOT, "01_FolderSync_Basic/level1", "001-sync-01.png",
                "SYNC 01", "Folder Sync Demo", "Level 1 / New Item", 0, "folder");
        add(specs, FOLDER_SYNC_ROOT, "01_FolderSync_Basic/level1", "002-sync-02.png",
                "SYNC 02", "Folder Preset Demo", "Source Path: D:/Report-Test-Images-FolderSync", 1, "folder");
        add(specs, FOLDER_SYNC_ROOT, "01_FolderSync_Basic/level1", "003-relative-path.png",
                "RELATIVE PATH", "Scan Result Demo", "01_FolderSync_Basic/level1", 2, "cards");
        add(specs, FOLDER_SYNC_ROOT, "01_FolderSync_Basic/level1", "004-new-item.png",
                "NEW FILE", "Default Selected", "New images are checked before Apply Sync", 3, "star");
        add(specs, FOLDER_SYNC_ROOT, "01_FolderSync_Basic/level1/level2", "005-nested-level-2.png",
                "NESTED LEVEL 2", "Recursive Folder Scan", "level1/level2 relative path", 4, "tree");
        add(specs, FOLDER_SYNC_ROOT, "01_FolderSync_Basic/level1/level2", "006-sync-status.png",
                "SYNC STATUS", "New / Changed / Unchanged", "Used for scan result screenshots", 5, "robot");
        add(specs, FOLDER_SYNC_ROOT, "01_FolderSync_Basic/level1/level2", "007-folder-preset.png",
                "FOLDER PRESET", "Authorize and Sync", "Preset belongs to current account", 0, "folder");

        add(specs, FOLDER_SYNC_ROOT, "02_DeleteMissing_Test/before_cleanup", "008-delete-test-01.png",
                "DELETE TEST 01", "Delete Missing Demo", "Upload first, then remove locally", 1, "trash");
        add(specs, FOLDER_SYNC_ROOT, "02_DeleteMissing_Test/before_cleanup", "009-delete-test-02.png",
                "DELETE TEST 02", "Cloud Cleanup Target", "Expected to disappear after cleanup", 2, "trash");
        add(specs, FOLDER_SYNC_ROOT, "02_DeleteMissing_Test/before_cleanup", "010-delete-test-03.png",
                "DELETE TEST 03", "Missing Local File", "Use for cleanup checkbox screenshot", 3, "trash");
        add(specs, FOLDER_SYNC_ROOT, "02_DeleteMissing_Test/before_cleanup", "011-cleanup-test.png",
                "CLEANUP TEST", "Delete cloud images missing from this folder", "Confirm dialog demo", 4, "trash");
        add(specs, FOLDER_SYNC_ROOT, "02_DeleteMissing_Test/before_cleanup", "012-keep-file.png",
                "KEEP FILE", "Still Exists Locally", "This one should remain", 5, "shield");
        add(specs, FOLDER_SYNC_ROOT, "02_DeleteMissing_Test/nested", "013-delete-nested-01.png",
                "NESTED DELETE", "Nested Cleanup Demo", "02_DeleteMissing_Test/nested", 0, "trash");
        add(specs, FOLDER_SYNC_ROOT, "02_DeleteMissing_Test/nested", "014-cleanup-result.png",
                "CLEANUP RESULT", "Deleted Count Demo", "Message: uploaded, updated, skipped, deleted", 1, "check");

        add(specs, FOLDER_SYNC_ROOT, "03_Changed_Files_Test/round1", "015-changed-round1.png",
                "CHANGED FILE", "Round 1 Version", "Sync once, then replace to test Changed", 2, "refresh");
        add(specs, FOLDER_SYNC_ROOT, "03_Changed_Files_Test/round1", "016-old-version.png",
                "OLD VERSION", "Before Update", "File size or modified time will change", 3, "refresh");
        add(specs, FOLDER_SYNC_ROOT, "03_Changed_Files_Test/round1", "017-change-target.png",
                "CHANGE TARGET", "Changed Status Demo", "Use this row for status screenshot", 4, "refresh");
        add(specs, FOLDER_SYNC_ROOT, "03_Changed_Files_Test/round1", "018-round1-baseline.png",
                "BASELINE", "Initial Sync", "Unchanged if file is not edited", 5, "cards");
        add(specs, FOLDER_SYNC_ROOT, "03_Changed_Files_Test/round1/updated", "019-updated-version.png",
                "UPDATED FILE", "Round 2 Version", "Changed item after local edit", 0, "refresh");
        add(specs, FOLDER_SYNC_ROOT, "03_Changed_Files_Test/round1/updated", "020-updated-metadata.png",
                "UPDATED META", "Size / Time Changed", "Backend updates existing DB row", 1, "database");

        add(specs, FOLDER_SYNC_ROOT, "04_Chinese_Path_Test/\u5b50\u76ee\u5f55\u6d4b\u8bd5", "021-chinese-path.png",
                "CHINESE PATH", "Path Compatibility", "Chinese subfolder name test", 2, "folder");
        add(specs, FOLDER_SYNC_ROOT, "04_Chinese_Path_Test/\u5b50\u76ee\u5f55\u6d4b\u8bd5", "022-unicode-folder.png",
                "UNICODE FOLDER", "Folder Sync Demo", "Relative path contains non-ASCII directory", 3, "tree");
        add(specs, FOLDER_SYNC_ROOT, "04_Chinese_Path_Test/\u5b50\u76ee\u5f55\u6d4b\u8bd5", "023-path-preview.png",
                "PATH PREVIEW", "Report Screenshot", "Show sourcePath + relativePath clearly", 4, "cards");
        add(specs, FOLDER_SYNC_ROOT, "04_Chinese_Path_Test/\u5b50\u76ee\u5f55\u6d4b\u8bd5", "024-sync-chinese.png",
                "SYNC CHINESE", "Browser Directory Authorization", "No backend local disk access", 5, "shield");

        add(specs, FOLDER_SYNC_ROOT, "05_Unchanged_Test/deep/level/a/b", "025-unchanged-01.png",
                "UNCHANGED 01", "Already Synced", "Should be skipped on second sync", 0, "check");
        add(specs, FOLDER_SYNC_ROOT, "05_Unchanged_Test/deep/level/a/b", "026-unchanged-02.png",
                "UNCHANGED 02", "Skipped Upload", "Status: Unchanged", 1, "check");
        add(specs, FOLDER_SYNC_ROOT, "05_Unchanged_Test/deep/level/a/b", "027-deep-level.png",
                "DEEP LEVEL", "Nested Path Demo", "deep/level/a/b", 2, "tree");
        add(specs, FOLDER_SYNC_ROOT, "05_Unchanged_Test/deep/level/a/b", "028-skip-demo.png",
                "SKIP DEMO", "No Duplicate Upload", "Same fileSize and lastModified", 3, "database");

        add(specs, FOLDER_SYNC_ROOT, "06_Mixed_Status_Test/new_items", "029-mixed-new-01.png",
                "NEW ITEM", "Mixed Status Demo", "Default checked", 4, "star");
        add(specs, FOLDER_SYNC_ROOT, "06_Mixed_Status_Test/new_items", "030-mixed-new-02.png",
                "NEW ITEM 02", "Add to Cloud", "Shows New in scan result", 5, "star");
        add(specs, FOLDER_SYNC_ROOT, "06_Mixed_Status_Test/changed_items", "031-mixed-changed-01.png",
                "CHANGED ITEM", "Needs Update", "Reupload and replace object", 0, "refresh");
        add(specs, FOLDER_SYNC_ROOT, "06_Mixed_Status_Test/changed_items", "032-mixed-changed-02.png",
                "CHANGED ITEM 02", "Metadata Update", "DB row should be updated", 1, "refresh");
        add(specs, FOLDER_SYNC_ROOT, "06_Mixed_Status_Test/unchanged_items", "033-mixed-unchanged-01.png",
                "UNCHANGED ITEM", "No Upload Needed", "Default unchecked", 2, "check");
        add(specs, FOLDER_SYNC_ROOT, "06_Mixed_Status_Test/unchanged_items", "034-mixed-unchanged-02.png",
                "UNCHANGED ITEM 02", "Skipped", "Used for comparison screenshot", 3, "check");
    }

    private static void addUploadSpecs(List<ImageSpec> specs) {
        add(specs, UPLOAD_ROOT, "01_DirectUpload", "035-upload-01.png",
                "UPLOAD 01", "Direct Upload Page", "Manual single image upload", 0, "upload");
        add(specs, UPLOAD_ROOT, "01_DirectUpload", "036-upload-02.png",
                "UPLOAD 02", "Title / Description / Tags", "Use this for form screenshot", 1, "upload");
        add(specs, UPLOAD_ROOT, "01_DirectUpload", "037-upload-demo.png",
                "UPLOAD DEMO", "R2 Storage + MySQL Metadata", "Redirects back to Dashboard", 2, "cloud");
        add(specs, UPLOAD_ROOT, "01_DirectUpload", "038-single-upload.png",
                "SINGLE UPLOAD", "Mobile or Desktop", "Good for Upload result card", 3, "phone");

        add(specs, UPLOAD_ROOT, "02_BatchUpload", "039-batch-01.png",
                "BATCH 01", "Batch Upload Demo", "Select multiple files", 4, "cards");
        add(specs, UPLOAD_ROOT, "02_BatchUpload", "040-batch-02.png",
                "BATCH 02", "Select All / Partial", "Upload selected images", 5, "cards");
        add(specs, UPLOAD_ROOT, "02_BatchUpload", "041-batch-03.png",
                "BATCH 03", "Five At A Time", "Batch size limit screenshot", 0, "cards");
        add(specs, UPLOAD_ROOT, "02_BatchUpload", "042-multi-upload.png",
                "MULTI UPLOAD", "Progress Demo", "Good for progress bar screenshot", 1, "upload");
        add(specs, UPLOAD_ROOT, "02_BatchUpload", "043-folder-import.png",
                "FOLDER IMPORT", "Browser Selected Folder", "webkitdirectory fallback demo", 2, "folder");

        add(specs, UPLOAD_ROOT, "03_TagTest", "044-tag-test.png",
                "TAG TEST", "Context Menu Demo", "Menu stays open for multiple tags", 3, "tag");
        add(specs, UPLOAD_ROOT, "03_TagTest", "045-common-tag.png",
                "COMMON TAG", "Toggle Tag", "Common selected", 4, "tag");
        add(specs, UPLOAD_ROOT, "03_TagTest", "046-notes-tag.png",
                "NOTES TAG", "Toggle Tag", "Notes selected", 5, "tag");
        add(specs, UPLOAD_ROOT, "03_TagTest", "047-favorite-tag.png",
                "FAVORITE TAG", "Filter by Favorite", "Use for tag filter screenshot", 0, "tag");
        add(specs, UPLOAD_ROOT, "03_TagTest", "048-funny-tag.png",
                "FUNNY TAG", "Tag Menu Screenshot", "Continuous tag operation", 1, "tag");

        add(specs, UPLOAD_ROOT, "04_CopyTest", "049-copy-test.png",
                "COPY TEST", "Copy Image Binary", "Clipboard first, link fallback", 2, "copy");
        add(specs, UPLOAD_ROOT, "04_CopyTest", "050-clipboard-demo.png",
                "CLIPBOARD DEMO", "navigator.clipboard.write", "HTTPS browser screenshot", 3, "copy");
        add(specs, UPLOAD_ROOT, "04_CopyTest", "051-copy-image.png",
                "COPY IMAGE", "Protected Content API", "GET /api/images/{id}/content", 4, "copy");
        add(specs, UPLOAD_ROOT, "04_CopyTest", "052-paste-test.png",
                "PASTE TEST", "Fallback to Link or Download", "Good for report notes", 5, "copy");

        add(specs, UPLOAD_ROOT, "05_SearchTest", "053-search-java.png",
                "SEARCH JAVA", "Keyword Search", "Search title, tag, source or path", 0, "search");
        add(specs, UPLOAD_ROOT, "05_SearchTest", "054-search-cloud.png",
                "SEARCH CLOUD", "Cloud Filter Demo", "R2 / Railway / Aiven", 1, "cloud");
        add(specs, UPLOAD_ROOT, "05_SearchTest", "055-search-note.png",
                "SEARCH NOTE", "Notes Category", "Use keyword: note", 2, "search");
        add(specs, UPLOAD_ROOT, "05_SearchTest", "056-filter-favorite.png",
                "FILTER FAVORITE", "Tag Filter", "Dashboard filter screenshot", 3, "tag");
        add(specs, UPLOAD_ROOT, "05_SearchTest", "057-sort-time.png",
                "SORT TIME", "Sort by File Time", "Instant dropdown filter", 4, "search");

        add(specs, UPLOAD_ROOT, "06_MobileTest", "058-mobile-view.png",
                "MOBILE VIEW", "Responsive Dashboard", "Phone screenshot demo", 5, "phone");
        add(specs, UPLOAD_ROOT, "06_MobileTest", "059-responsive-ui.png",
                "RESPONSIVE UI", "Touch Friendly Buttons", "Cards fit small screens", 0, "phone");
        add(specs, UPLOAD_ROOT, "06_MobileTest", "060-phone-upload.png",
                "PHONE UPLOAD", "Mobile Upload", "Use camera or photo picker", 1, "phone");
    }

    private static void add(List<ImageSpec> specs, String root, String folder, String fileName,
                            String title, String subtitle, String note, int theme, String icon) {
        specs.add(new ImageSpec(root, folder, fileName, title, subtitle, note, theme, icon));
    }

    private static void recreateDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (var stream = Files.walk(dir)) {
                for (Path path : stream.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
        Files.createDirectories(dir);
    }

    private static void writeImage(Path output, ImageSpec spec) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color[] palette = palette(spec.theme());
            drawBackground(g, palette);
            drawIcon(g, spec.icon(), palette);
            drawTextPanel(g, spec, palette);
            drawFooter(g, spec);
        } finally {
            g.dispose();
        }
        ImageIO.write(image, "png", output.toFile());
    }

    private static Color[] palette(int index) {
        Color[][] palettes = {
                {new Color(17, 29, 24), new Color(57, 101, 45), new Color(118, 185, 0), new Color(235, 255, 211)},
                {new Color(18, 30, 52), new Color(32, 98, 136), new Color(84, 205, 255), new Color(226, 250, 255)},
                {new Color(45, 28, 61), new Color(110, 65, 137), new Color(255, 190, 82), new Color(255, 244, 214)},
                {new Color(53, 27, 34), new Color(132, 50, 69), new Color(255, 97, 131), new Color(255, 230, 236)},
                {new Color(20, 42, 45), new Color(32, 120, 104), new Color(84, 234, 183), new Color(224, 255, 245)},
                {new Color(42, 35, 23), new Color(128, 91, 27), new Color(255, 218, 84), new Color(255, 248, 212)}
        };
        return palettes[index % palettes.length];
    }

    private static void drawBackground(Graphics2D g, Color[] palette) {
        g.setPaint(new GradientPaint(0, 0, palette[0], WIDTH, HEIGHT, palette[1]));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setStroke(new BasicStroke(3f));
        g.setColor(withAlpha(palette[3], 28));
        for (int i = 0; i < 12; i++) {
            int x = 70 + i * 105;
            g.drawArc(x % WIDTH, 55 + (i % 5) * 110, 250, 130, 15, 230);
        }
        g.setColor(withAlpha(palette[2], 58));
        for (int i = 0; i < 18; i++) {
            int size = 18 + (i % 6) * 13;
            int x = (i * 149 + 70) % WIDTH;
            int y = (i * 97 + 120) % HEIGHT;
            g.fill(new Ellipse2D.Double(x, y, size, size));
        }
    }

    private static void drawIcon(Graphics2D g, String icon, Color[] palette) {
        int x = 705;
        int y = 145;
        int w = 360;
        int h = 320;
        g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (icon) {
            case "folder" -> drawFolder(g, x, y, w, h, palette);
            case "cards" -> drawCards(g, x, y, palette);
            case "star" -> drawStarIcon(g, x + 180, y + 160, 130, palette);
            case "tree" -> drawTree(g, x, y, palette);
            case "robot" -> drawRobot(g, x, y, palette);
            case "trash" -> drawTrash(g, x, y, palette);
            case "shield" -> drawShield(g, x, y, palette);
            case "check" -> drawCheck(g, x, y, palette);
            case "refresh" -> drawRefresh(g, x, y, palette);
            case "database" -> drawDatabase(g, x, y, palette);
            case "upload" -> drawUpload(g, x, y, palette);
            case "cloud" -> drawCloud(g, x, y, palette);
            case "phone" -> drawPhone(g, x, y, palette);
            case "tag" -> drawTag(g, x, y, palette);
            case "copy" -> drawCopy(g, x, y, palette);
            case "search" -> drawSearch(g, x, y, palette);
            default -> drawCards(g, x, y, palette);
        }
    }

    private static void drawTextPanel(Graphics2D g, ImageSpec spec, Color[] palette) {
        g.setColor(withAlpha(Color.BLACK, 120));
        g.fillRoundRect(68, 124, 590, 470, 44, 44);
        g.setColor(withAlpha(Color.WHITE, 28));
        g.drawRoundRect(68, 124, 590, 470, 44, 44);

        drawFittedText(g, spec.title(), 102, 238, 520, 122, Font.BOLD, Color.WHITE);
        drawFittedText(g, spec.subtitle(), 104, 354, 520, 54, Font.BOLD, palette[3]);
        drawFittedText(g, spec.note(), 104, 434, 520, 38, Font.PLAIN, new Color(232, 232, 232));

        g.setColor(palette[2]);
        g.fillRoundRect(104, 510, 205, 16, 16, 16);
        g.setColor(withAlpha(palette[3], 180));
        g.fillRoundRect(324, 510, 94, 16, 16, 16);
    }

    private static void drawFooter(Graphics2D g, ImageSpec spec) {
        g.setColor(withAlpha(Color.BLACK, 96));
        g.fillRoundRect(68, 720, 1064, 92, 32, 32);
        g.setColor(new Color(235, 235, 235));
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        g.drawString("Report Screenshot Test Image", 104, 776);
        g.setColor(new Color(190, 190, 190));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        String path = spec.root() + "/" + spec.folder() + "/" + spec.fileName();
        g.drawString(fitPlainText(g, path, 980), 104, 808);
    }

    private static void drawFittedText(Graphics2D g, String text, int x, int baseline, int maxWidth,
                                       int startSize, int style, Color color) {
        int size = startSize;
        Font font;
        FontMetrics metrics;
        do {
            font = new Font("SansSerif", style, size);
            g.setFont(font);
            metrics = g.getFontMetrics();
            size -= 2;
        } while (metrics.stringWidth(text) > maxWidth && size > 20);
        g.setColor(color);
        g.drawString(text, x, baseline);
    }

    private static String fitPlainText(Graphics2D g, String text, int maxWidth) {
        if (g.getFontMetrics().stringWidth(text) <= maxWidth) {
            return text;
        }
        String result = text;
        while (result.length() > 8 && g.getFontMetrics().stringWidth("..." + result) > maxWidth) {
            result = result.substring(1);
        }
        return "..." + result;
    }

    private static void drawFolder(Graphics2D g, int x, int y, int w, int h, Color[] p) {
        g.setColor(withAlpha(Color.BLACK, 90));
        g.fillRoundRect(x + 18, y + 92, w, h - 76, 42, 42);
        g.setColor(p[2]);
        g.fillRoundRect(x + 18, y + 64, 152, 72, 26, 26);
        g.setColor(p[3]);
        g.fillRoundRect(x, y + 108, w, h - 78, 44, 44);
        g.setColor(p[0]);
        g.fillRoundRect(x + 62, y + 180, w - 124, 30, 22, 22);
        g.fillRoundRect(x + 62, y + 232, w - 174, 30, 22, 22);
    }

    private static void drawCards(Graphics2D g, int x, int y, Color[] p) {
        for (int i = 0; i < 3; i++) {
            int dx = x + i * 54;
            int dy = y + i * 46;
            g.setColor(withAlpha(Color.BLACK, 78));
            g.fillRoundRect(dx + 20, dy + 20, 260, 180, 34, 34);
            g.setColor(p[3]);
            g.fillRoundRect(dx, dy, 260, 180, 34, 34);
            g.setColor(p[2]);
            g.fillOval(dx + 34, dy + 28, 46, 46);
            g.setColor(p[0]);
            Path2D mountain = new Path2D.Double();
            mountain.moveTo(dx + 30, dy + 152);
            mountain.lineTo(dx + 92, dy + 92);
            mountain.lineTo(dx + 138, dy + 138);
            mountain.lineTo(dx + 186, dy + 76);
            mountain.lineTo(dx + 232, dy + 152);
            mountain.closePath();
            g.fill(mountain);
        }
    }

    private static void drawStarIcon(Graphics2D g, int cx, int cy, int r, Color[] p) {
        g.setColor(withAlpha(Color.BLACK, 70));
        drawStar(g, cx + 12, cy + 14, r);
        g.setColor(p[2]);
        drawStar(g, cx, cy, r);
        g.setColor(p[3]);
        drawStar(g, cx + 8, cy - 4, r / 2);
    }

    private static void drawTree(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(p[3]);
        for (int i = 0; i < 4; i++) {
            g.fillRoundRect(x + 150, y + 34 + i * 72, 170, 50, 22, 22);
        }
        g.setColor(p[2]);
        g.drawLine(x + 105, y + 58, x + 105, y + 270);
        for (int i = 0; i < 4; i++) {
            g.drawLine(x + 105, y + 58 + i * 72, x + 150, y + 58 + i * 72);
        }
        g.fillOval(x + 73, y + 28, 64, 64);
    }

    private static void drawRobot(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(p[3]);
        g.fillRoundRect(x + 78, y + 58, 240, 220, 48, 48);
        g.setColor(p[0]);
        g.fillOval(x + 134, y + 138, 34, 34);
        g.fillOval(x + 232, y + 138, 34, 34);
        g.drawLine(x + 155, y + 220, x + 245, y + 220);
        g.setColor(p[2]);
        g.fillRoundRect(x + 155, y + 22, 86, 24, 18, 18);
        g.drawLine(x + 198, y + 48, x + 198, y + 58);
    }

    private static void drawTrash(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(p[3]);
        g.fillRoundRect(x + 105, y + 105, 190, 210, 28, 28);
        g.setColor(p[2]);
        g.fillRoundRect(x + 82, y + 74, 236, 34, 18, 18);
        g.fillRoundRect(x + 148, y + 34, 104, 30, 18, 18);
        g.setColor(p[0]);
        for (int i = 0; i < 3; i++) {
            g.drawLine(x + 152 + i * 48, y + 142, x + 152 + i * 48, y + 270);
        }
    }

    private static void drawShield(Graphics2D g, int x, int y, Color[] p) {
        Path2D shield = new Path2D.Double();
        shield.moveTo(x + 195, y + 35);
        shield.lineTo(x + 320, y + 82);
        shield.lineTo(x + 294, y + 238);
        shield.quadTo(x + 195, y + 330, x + 96, y + 238);
        shield.lineTo(x + 70, y + 82);
        shield.closePath();
        g.setColor(p[3]);
        g.fill(shield);
        g.setColor(p[2]);
        g.drawLine(x + 142, y + 188, x + 184, y + 230);
        g.drawLine(x + 184, y + 230, x + 264, y + 142);
    }

    private static void drawCheck(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(p[3]);
        g.fillOval(x + 62, y + 40, 260, 260);
        g.setColor(p[2]);
        g.drawLine(x + 128, y + 180, x + 180, y + 232);
        g.drawLine(x + 180, y + 232, x + 276, y + 126);
    }

    private static void drawRefresh(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(p[3]);
        g.drawArc(x + 72, y + 72, 236, 208, 40, 250);
        g.drawArc(x + 92, y + 50, 236, 208, 220, 240);
        g.setColor(p[2]);
        drawTriangle(g, x + 88, y + 92, x + 74, y + 176, x + 144, y + 138);
        drawTriangle(g, x + 314, y + 236, x + 332, y + 152, x + 260, y + 190);
    }

    private static void drawDatabase(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(p[3]);
        g.fillOval(x + 70, y + 52, 260, 74);
        g.fillRect(x + 70, y + 88, 260, 180);
        g.fillOval(x + 70, y + 232, 260, 74);
        g.setColor(p[2]);
        g.drawOval(x + 70, y + 52, 260, 74);
        g.drawArc(x + 70, y + 142, 260, 74, 0, -180);
        g.drawArc(x + 70, y + 202, 260, 74, 0, -180);
    }

    private static void drawUpload(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(p[3]);
        g.fillRoundRect(x + 78, y + 196, 250, 82, 26, 26);
        g.setColor(p[2]);
        g.drawLine(x + 202, y + 208, x + 202, y + 68);
        drawTriangle(g, x + 202, y + 38, x + 134, y + 124, x + 270, y + 124);
    }

    private static void drawCloud(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(p[3]);
        g.fillOval(x + 70, y + 150, 120, 100);
        g.fillOval(x + 145, y + 90, 150, 150);
        g.fillOval(x + 250, y + 150, 110, 98);
        g.fillRoundRect(x + 80, y + 188, 280, 86, 42, 42);
        g.setColor(p[2]);
        g.fillOval(x + 214, y + 146, 44, 44);
    }

    private static void drawPhone(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(withAlpha(Color.BLACK, 100));
        g.fillRoundRect(x + 122, y + 18, 174, 312, 34, 34);
        g.setColor(p[3]);
        g.fillRoundRect(x + 138, y + 42, 142, 252, 22, 22);
        g.setColor(p[2]);
        g.fillRoundRect(x + 164, y + 86, 90, 86, 24, 24);
        g.fillRoundRect(x + 164, y + 198, 90, 28, 18, 18);
        g.setColor(p[0]);
        g.fillOval(x + 199, y + 306, 18, 18);
    }

    private static void drawTag(Graphics2D g, int x, int y, Color[] p) {
        Path2D tag = new Path2D.Double();
        tag.moveTo(x + 82, y + 82);
        tag.lineTo(x + 252, y + 82);
        tag.lineTo(x + 346, y + 176);
        tag.lineTo(x + 196, y + 326);
        tag.lineTo(x + 64, y + 194);
        tag.closePath();
        g.setColor(p[3]);
        g.fill(tag);
        g.setColor(p[2]);
        g.fillOval(x + 130, y + 132, 36, 36);
    }

    private static void drawCopy(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(withAlpha(Color.BLACK, 85));
        g.fillRoundRect(x + 132, y + 96, 190, 210, 28, 28);
        g.setColor(p[2]);
        g.fillRoundRect(x + 82, y + 54, 190, 210, 28, 28);
        g.setColor(p[3]);
        g.fillRoundRect(x + 112, y + 92, 128, 22, 14, 14);
        g.fillRoundRect(x + 112, y + 142, 92, 22, 14, 14);
        g.fillRoundRect(x + 112, y + 192, 118, 22, 14, 14);
    }

    private static void drawSearch(Graphics2D g, int x, int y, Color[] p) {
        g.setColor(p[3]);
        g.drawOval(x + 88, y + 62, 168, 168);
        g.setColor(p[2]);
        g.drawLine(x + 224, y + 206, x + 320, y + 302);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x + 132, y + 152, x + 212, y + 152);
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

    private static void drawTriangle(Graphics2D g, int x1, int y1, int x2, int y2, int x3, int y3) {
        Path2D triangle = new Path2D.Double();
        triangle.moveTo(x1, y1);
        triangle.lineTo(x2, y2);
        triangle.lineTo(x3, y3);
        triangle.closePath();
        g.fill(triangle);
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private static GenerationReport reportFor(String root) throws IOException {
        Path dir = TEST_DATA.resolve(root);
        List<Path> pngs;
        try (var stream = Files.walk(dir)) {
            pngs = stream.filter(path -> path.toString().toLowerCase().endsWith(".png")).toList();
        }
        long min = Long.MAX_VALUE;
        long max = 0;
        for (Path png : pngs) {
            long size = Files.size(png);
            min = Math.min(min, size);
            max = Math.max(max, size);
            if (size >= 500L * 1024L) {
                throw new IllegalStateException("PNG is larger than 500KB: " + png + " (" + size + " bytes)");
            }
        }
        return new GenerationReport(root, pngs.size(), min, max, TEST_DATA.resolve(root + ".zip"));
    }

    private static void zipRoot(String root) throws IOException {
        Path rootPath = TEST_DATA.resolve(root);
        Path zipPath = TEST_DATA.resolve(root + ".zip");
        try (OutputStream outputStream = Files.newOutputStream(zipPath);
             ZipOutputStream zip = new ZipOutputStream(outputStream);
             var stream = Files.walk(rootPath)) {
            for (Path path : stream.filter(Files::isRegularFile).toList()) {
                Path relative = TEST_DATA.relativize(path);
                zip.putNextEntry(new ZipEntry(relative.toString().replace("\\", "/")));
                Files.copy(path, zip);
                zip.closeEntry();
            }
        }
    }

    private static void writeReadme() throws IOException {
        String text = """
                ImageManager report screenshot test images

                ZIP files:
                1. Folder Sync:
                   test-data/Report-Test-Images-FolderSync.zip
                   Extract to:
                   D:\\Report-Test-Images-FolderSync
                   Suggested Folder Preset:
                   Name: Report Sync
                   Source Path: D:/Report-Test-Images-FolderSync

                2. Direct Upload / Batch Upload:
                   test-data/Report-Test-Images-Upload.zip
                   Extract to:
                   D:\\Report-Test-Images-Upload

                Recommended screenshot usage:
                - Folder Sync overview:
                  Report-Test-Images-FolderSync/01_FolderSync_Basic
                - Folder Sync scan result and relativePath:
                  01_FolderSync_Basic/level1/level2
                - Delete Missing test:
                  02_DeleteMissing_Test
                  Sync once, delete several local files, scan again, enable Delete cloud images missing from this folder, then Apply Sync.
                - Changed / Unchanged status:
                  03_Changed_Files_Test and 05_Unchanged_Test
                - Chinese path compatibility:
                  04_Chinese_Path_Test
                - Direct Upload page:
                  Report-Test-Images-Upload/01_DirectUpload
                - Batch Upload page:
                  Report-Test-Images-Upload/02_BatchUpload
                - Tag menu screenshot:
                  Report-Test-Images-Upload/03_TagTest
                - Copy image screenshot:
                  Report-Test-Images-Upload/04_CopyTest
                - Search / Filter / Sort screenshot:
                  Report-Test-Images-Upload/05_SearchTest
                - Mobile screenshot:
                  Report-Test-Images-Upload/06_MobileTest

                All images are generated with Java standard library only.
                They use large report-oriented titles so Dashboard screenshots are easier to read.
                The Chinese path test folder is included under 04_Chinese_Path_Test.
                """;
        Files.writeString(TEST_DATA.resolve("README-Report-Test-Images.txt"), text);
    }
}
