package l2s.commons.compiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;

import javax.tools.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Locale;

/**
 * Класс компиляции внешних Java файлов<br>
 *
 * @author G1ta0
 */
public class Compiler
{
    private static final Logger LOGGER = LogManager.getLogger(Compiler.class);

    private final JavaCompiler compiler = new EclipseCompiler();
    private final MemoryClassLoader memoryClassLoader
            = AccessController.doPrivileged((PrivilegedAction<MemoryClassLoader>) MemoryClassLoader::new);

    public boolean compile(Iterable<Path> files)
    {
        // compiler options
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try(StandardJavaFileManager fileManager = new EclipseFileManager(null, StandardCharsets.UTF_8);
            JavaFileManager memFileManager = new MemoryJavaFileManager(fileManager, memoryClassLoader))
        {
            List<String> options = List.of("-11", "-Xlint:all", "-g");
            JavaCompiler.CompilationTask compile = compiler.getTask(null, memFileManager, diagnostics, options, null,
                    fileManager.getJavaFileObjectsFromPaths(files));
            return compile.call();
        }
        catch(IOException e)
        {
            LOGGER.error("Can't compile", e);
            return false;
        }
        finally
        {
            for(Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())
            {
                if(diagnostic.getKind() == Diagnostic.Kind.ERROR)
                {
                    LOGGER.error("{}{}: {}", diagnostic.getSource().getName(), diagnostic.getPosition() == Diagnostic.NOPOS
                                                                               ? "" : ":" + diagnostic.getLineNumber() + ',' + diagnostic.getColumnNumber(),
                            diagnostic.getMessage(Locale.getDefault()));
                }
                else
                {
                    String sourceName = diagnostic.getSource() == null ? "" : diagnostic.getSource().getName();
                    LOGGER.debug("{}{}: {}", sourceName, diagnostic.getPosition() == Diagnostic.NOPOS
                                                         ? "" : ":" + diagnostic.getLineNumber() + ',' + diagnostic.getColumnNumber(),
                            diagnostic.getMessage(Locale.getDefault()));
                }
            }
        }
    }

    public MemoryClassLoader getClassLoader()
    {
        return memoryClassLoader;
    }
}
