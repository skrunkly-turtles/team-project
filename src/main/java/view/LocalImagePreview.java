package view;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Reads a bounded local image and creates a thumbnail without persisting it. */
public final class LocalImagePreview {
    /** Prevents construction of this image utility. */
    private LocalImagePreview() { }

    /**
     * Decodes PNG/JPEG images with local preview limits of 10 MiB and 20 million pixels.
     * @param path image file selected by the user
     * @return thumbnail no larger than 260 by 160 pixels
     * @throws IOException if the file is unsupported, unreadable, or over the preview limits
     */
    public static BufferedImage read(Path path) throws IOException {
        if (!Files.isRegularFile(path) || Files.size(path) > 10L * 1024 * 1024) {
            throw new IOException("Choose an image no larger than 10 MB.");
        }
        try (ImageInputStream stream = ImageIO.createImageInputStream(path.toFile())) {
            if (stream == null) { throw new IOException("Cannot read this image."); }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) { throw new IOException("Choose a PNG or JPEG image."); }
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("PNG") && !format.equalsIgnoreCase("JPEG")) {
                    throw new IOException("Choose a PNG or JPEG image.");
                }
                reader.setInput(stream, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > 20_000_000) {
                    throw new IOException("Choose an image with at most 20 million pixels.");
                }
                BufferedImage source = reader.read(0);
                double scale = Math.min(1, Math.min(260.0 / width, 160.0 / height));
                BufferedImage thumbnail = new BufferedImage(Math.max(1, (int) (width * scale)),
                        Math.max(1, (int) (height * scale)), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = thumbnail.createGraphics();
                try {
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics.drawImage(source, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), null);
                } finally { graphics.dispose(); }
                return thumbnail;
            } finally { reader.dispose(); }
        }
    }
}
