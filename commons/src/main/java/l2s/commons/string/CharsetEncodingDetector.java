package l2s.commons.string;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class CharsetEncodingDetector {
    private static final UniversalDetector DETECTOR = new UniversalDetector(null);

    public static Charset detectEncoding(Path path, Charset defaultEncoding) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            byte[] buf = new byte[4096];

            int nread;
            while ((nread = inputStream.read(buf)) > 0 && !DETECTOR.isDone())
                DETECTOR.handleData(buf, 0, nread);

            DETECTOR.dataEnd();

            String encoding = DETECTOR.getDetectedCharset();
            return encoding == null ? defaultEncoding : Charset.forName(encoding);
        } catch (Exception e) {
            return defaultEncoding;
        } finally {
            DETECTOR.reset();
        }
    }
}