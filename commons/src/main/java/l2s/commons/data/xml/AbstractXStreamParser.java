package l2s.commons.data.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import l2s.commons.logging.LoggerObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

public abstract class AbstractXStreamParser<H extends AbstractHolder, T> extends LoggerObject {
    private final H holder;
    private String currentFile;
    private XStream xstream;

    protected AbstractXStreamParser(H holder) {
        this.holder = holder;
        xstream = new XStream(new StaxDriver());
        initializeXStream(xstream);
    }

    protected void initializeXStream(XStream xstream) {
    }

    public abstract File getXMLPath();

    public boolean isIgnored(Path path) {
        return false;
    }

    protected void parseDocument(InputStream f, String name) {
        currentFile = name;
        T data = (T) xstream.fromXML(f);
        readData(data);
    }

    protected abstract void readData(T data);

    protected void parse() {
        File path = getXMLPath();
        if (!path.exists()) {
            warn("directory or file " + path.getAbsolutePath() + " not exists");
            return;
        }

        if (path.isDirectory()) {
            parseDir(path);
        } else {
            try {
                parseDocument(new FileInputStream(path), path.getName());
            } catch (Exception e) {
                warn("Exception: " + e, e);
            }
        }
        afterParseActions();
    }

    protected void afterParseActions() {
    }

    protected H getHolder() {
        return holder;
    }

    public String getCurrentFileName() {
        return currentFile;
    }

    public void load() {
        parse();
        holder.process();
        holder.log();
    }

    public void reload() {
        info("reload start...");
        holder.clear();
        load();
    }

    private void parseDir(File dir) {
        if (dir == null)
            return;

        if (!dir.exists()) {
            warn("Dir " + dir.getAbsolutePath() + " not exists");
            return;
        }

        var dirPath = dir.toPath();
        try (Stream<Path> pathStream = Files.list(dirPath)) {
            PathMatcher matcher =
                    FileSystems.getDefault().getPathMatcher("glob:**/*.xml");

            pathStream
                    .filter(matcher::matches)
                    .filter(path ->
                    {
                        try {
                            return !Files.isHidden(path) && !isIgnored(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .forEach(path ->
                    {
                        try {
                            parseDocument(Files.newInputStream(path), path.getFileName().toString());
                        } catch (Exception e) {
                            info("Exception: " + e + " in file: " + path.getFileName(), e);
                        }
                    });
        } catch (IOException e) {
            warn("Exception: " + e, e);
        }
    }
}
