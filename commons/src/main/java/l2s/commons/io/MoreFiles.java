package l2s.commons.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Java-man
 * @since 09.02.2019
 */
public class MoreFiles {
    public static List<Path> collectFiles(Path start) throws IOException {
        if (!Files.isDirectory(start)) {
            return List.of(start);
        }

        List<Path> files = new ArrayList<>();
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                files.add(file);
                return FileVisitResult.CONTINUE;
            }
        };

        Files.walkFileTree(start, visitor);

        return files;
    }
}
