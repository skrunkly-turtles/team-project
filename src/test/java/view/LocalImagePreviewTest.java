package view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Tests bounded preview decoding from real local fixtures. */
class LocalImagePreviewTest {
    @TempDir Path directory;

    /** PNG/JPEG decode and shrink while small images retain their size. */
    @Test
    void decodesAndScalesSupportedImages() throws Exception {
        Path png = directory.resolve("photo.png");
        ImageIO.write(new BufferedImage(520, 320, BufferedImage.TYPE_INT_RGB), "png", png.toFile());
        BufferedImage thumbnail = LocalImagePreview.read(png);
        assertEquals(260, thumbnail.getWidth());
        assertEquals(160, thumbnail.getHeight());
        Path jpeg = directory.resolve("photo.jpg");
        ImageIO.write(new BufferedImage(10, 20, BufferedImage.TYPE_INT_RGB), "jpeg", jpeg.toFile());
        assertEquals(10, LocalImagePreview.read(jpeg).getWidth());
        assertEquals(20, LocalImagePreview.read(jpeg).getHeight());
    }

    /** Unsupported, corrupt, missing, and oversized files cannot become attachments. */
    @Test
    void rejectsInvalidFiles() throws Exception {
        Path text = Files.writeString(directory.resolve("fake.png"), "not an image");
        assertThrows(IOException.class, () -> LocalImagePreview.read(text));
        assertThrows(IOException.class, () -> LocalImagePreview.read(directory.resolve("missing.png")));
        Path gif = directory.resolve("photo.gif");
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "gif", gif.toFile());
        assertThrows(IOException.class, () -> LocalImagePreview.read(gif));
        Path huge = directory.resolve("huge.png");
        try (RandomAccessFile file = new RandomAccessFile(huge.toFile(), "rw")) {
            file.setLength(10L * 1024 * 1024 + 1);
        }
        assertThrows(IOException.class, () -> LocalImagePreview.read(huge));
    }
}
