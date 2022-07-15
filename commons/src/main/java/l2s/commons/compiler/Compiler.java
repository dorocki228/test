package l2s.commons.compiler;

import com.google.common.flogger.FluentLogger;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import javax.tools.*;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;

/**
 * Класс компиляции внешних Java файлов<br>
 * В качестве компилятора используется Eclipse Java Compiler
 * 
 * @author G1ta0
 */
public class Compiler
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private static final JavaCompiler javac = new EclipseCompiler();

	private final DiagnosticListener<JavaFileObject> listener = new DefaultDiagnosticListener();
	private final StandardJavaFileManager fileManager = new EclipseFileManager(Locale.getDefault(), StandardCharsets.UTF_8);
	private final MemoryClassLoader memClassLoader = new MemoryClassLoader();
	private final MemoryJavaFileManager memFileManager = new MemoryJavaFileManager(fileManager, memClassLoader);

	public boolean compile(Iterable<Path> files)
	{
		Writer writer = new StringWriter();
		List<String> options = List.of("-11", "-Xlint:all", "-warn:none", "-g");
		JavaCompiler.CompilationTask compile = javac.getTask(writer, memFileManager, listener, options, null, fileManager.getJavaFileObjectsFromPaths(files));

		if(compile.call())
			return true;

		return false;
	}

	public MemoryClassLoader getClassLoader()
	{
		return memClassLoader;
	}

	private class DefaultDiagnosticListener implements DiagnosticListener<JavaFileObject>
	{
		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic)
		{
			_log.atSevere().log( "%s%s: %s", diagnostic.getSource().getName(), (diagnostic.getPosition() == Diagnostic.NOPOS ? "" : ":" + diagnostic.getLineNumber() + "," + diagnostic.getColumnNumber()), diagnostic.getMessage(Locale.getDefault()) );
		}
	}
}