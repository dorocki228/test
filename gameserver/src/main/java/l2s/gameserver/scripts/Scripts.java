package l2s.gameserver.scripts;

import com.google.common.flogger.FluentLogger;
import l2s.commons.compiler.Compiler;
import l2s.commons.compiler.MemoryClassLoader;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.listener.script.OnLoadScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.annotation.OnScriptInit;
import l2s.gameserver.scripts.annotation.OnScriptLoad;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ClassUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

import static com.google.common.flogger.LazyArgs.lazy;

public class Scripts
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static class ScriptListenerImpl extends ListenerList<Scripts>
	{
		public void load()
		{
			for(Listener<Scripts> listener : getListeners())
				if(OnLoadScriptListener.class.isInstance(listener))
					((OnLoadScriptListener) listener).onLoad();
		}

		public void init()
		{
			for(Listener<Scripts> listener : getListeners())
				if(OnInitScriptListener.class.isInstance(listener))
					((OnInitScriptListener) listener).onInit();
		}
	}

	

	private static final Scripts _instance = new Scripts();

	public static Scripts getInstance()
	{
		return _instance;
	}

	private Map<String, Class<?>> _classes;
	private final Map<Class<?>, Object> _instances = new ConcurrentHashMap<Class<?>, Object>();
	private final ScriptListenerImpl _listeners = new ScriptListenerImpl();

	private Scripts()
	{
		load();
	}

	/**
	 * Вызывается при загрузке сервера. Загрузает все скрипты в data/scripts. Не инициирует объекты и обработчики.
	 *
	 * @return true, если загрузка прошла успешно
	 */
	private void load()
	{
		_log.atInfo().log( "Scripts: Loading..." );

		Path scriptsPath = Config.SCRIPTS_PATH;
		if (Files.notExists(scriptsPath))
			throw new IllegalArgumentException("Can't find scripts by path " + scriptsPath.toAbsolutePath());
		boolean loadingFromJar = scriptsPath.toString().endsWith(".jar");
		var scripts = loadingFromJar ? loadJar(scriptsPath) : loadScripts(scriptsPath);
		if(scripts.isEmpty())
			throw new Error("Failed loading scripts!");
		_classes = scripts.stream().collect(Collectors.toUnmodifiableMap(Class::getName, Function.identity()));

		_log.atInfo().log( "Scripts: Loaded %d classes " + (loadingFromJar ? "from jar." : "from scripts."), lazy(() -> _classes.size()) );

		for(Class<?> clazz : _classes.values())
		{
			try
			{
				Object o = getClassInstance(clazz);
				if(ClassUtils.isAssignable(clazz, OnLoadScriptListener.class))
				{
					if(o == null)
						o = clazz.newInstance();

					_listeners.add((OnLoadScriptListener) o);
				}

				for(Method method : clazz.getMethods())
				{
					if(method.isAnnotationPresent(OnScriptLoad.class))
					{
						Class<?>[] par = method.getParameterTypes();
						if(par.length != 0)
						{
							_log.atSevere().log( "Wrong parameters for load method: %s, class: %s", method.getName(), clazz.getSimpleName() );
							continue;
						}

						try
						{
							if(Modifier.isStatic(method.getModifiers()))
								method.invoke(clazz);
							else
							{
								if(o == null)
									o = clazz.newInstance();
								method.invoke(o);
							}
						}
						catch(Exception e)
						{
							_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Exception: %s", e );
						}
					}
				}
			}
			catch(Exception e)
			{
				_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
			}
		}

		_listeners.load();
	}

	private List<? extends Class<?>> loadJar(Path jar) {
		_log.atInfo().log( "Scripts: Loading library..." );

        var classes = new ArrayList<Class<?>>();
		try (URLClassLoader scriptsLoader = new URLClassLoader(new URL[]{jar.toFile().toURI().toURL()});
			 InputStream inputStream = Files.newInputStream(jar);
			 JarInputStream stream = new JarInputStream(inputStream)) {
			JarEntry entry;
			while ((entry = stream.getNextJarEntry()) != null) {
				//Вложенные класс
				if (entry.getName().contains(ClassUtils.INNER_CLASS_SEPARATOR) || !entry.getName().endsWith(".class"))
					continue;

				String name = entry.getName().replace(".class", "").replace("/", ".");
				Class<?> clazz = scriptsLoader.loadClass(name);
				if (Modifier.isAbstract(clazz.getModifiers()))
					continue;

                classes.add(clazz);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed loading scripts library!", e);
		}

        return classes;
	}


	/**
	 * Загрузить все классы в data/scripts/target
	 *
	 * @param scriptsPath путь до класса, или каталога со скриптами
	 * @return список загруженых скриптов
	 */
	public List<Class<?>> loadScripts(Path scriptsPath)
	{
		_log.atInfo().log( "Scripts: Loading scripts..." );

		Collection<Path> scriptFiles = Collections.emptyList();

		if(Files.isRegularFile(scriptsPath))
		{
			scriptFiles = List.of(scriptsPath);
		}
		else if(Files.isDirectory(scriptsPath))
		{
			var temp = FileUtils.listFiles(scriptsPath.toFile(), FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
			scriptFiles = temp.stream().map(File::toPath).collect(Collectors.toUnmodifiableList());
		}

		if(scriptFiles.isEmpty())
			return Collections.emptyList();

		List<Class<?>> classes = new ArrayList<Class<?>>();
		Compiler compiler = new Compiler();

		if(compiler.compile(scriptFiles))
		{
			MemoryClassLoader classLoader = compiler.getClassLoader();
			for(String name : classLoader.getLoadedClasses())
			{
				//Вложенные класс
				if(name.contains(ClassUtils.INNER_CLASS_SEPARATOR))
					continue;

				try
				{
					Class<?> clazz = classLoader.loadClass(name);
					if(Modifier.isAbstract(clazz.getModifiers()))
						continue;

					classes.add(clazz);
				}
				catch(ClassNotFoundException e)
				{
					_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Scripts: Can\'t load script class: %s", name );
					classes.clear();
					break;
				}
			}
		}

		return classes;
	}

	/**
	 * Вызывается при загрузке сервера. Инициализирует объекты и обработчики.
	 */
	public void init()
	{
		for(Class<?> clazz : _classes.values())
			init(clazz);

		_listeners.init();
	}

	private Object init(Class<?> clazz)
	{
		Object o = getClassInstance(clazz);
		try
		{
			if(ClassUtils.isAssignable(clazz, OnInitScriptListener.class))
			{
				if(o == null)
					o = clazz.newInstance();
				_listeners.add((OnInitScriptListener) o);
			}

			for(Method method : clazz.getMethods())
			{
				if(method.isAnnotationPresent(Bypass.class))
				{
					Class<?>[] par = method.getParameterTypes();
					if(par.length == 0 || par[0] != Player.class || par[1] != NpcInstance.class || par[2] != String[].class)
					{
						_log.atSevere().log( "Wrong parameters for bypass method: %s, class: %s", method.getName(), clazz.getSimpleName() );
						continue;
					}

					Bypass an = method.getAnnotation(Bypass.class);
					if(Modifier.isStatic(method.getModifiers()))
						BypassHolder.getInstance().registerBypass(an.value(), clazz, method);
					else
					{
						if(o == null)
							o = clazz.newInstance();
						BypassHolder.getInstance().registerBypass(an.value(), o, method);
					}
				}
				else if(method.isAnnotationPresent(OnScriptInit.class))
				{
					Class<?>[] par = method.getParameterTypes();
					if(par.length != 0)
					{
						_log.atSevere().log( "Wrong parameters for init method: %s, class: %s", method.getName(), clazz.getSimpleName() );
						continue;
					}

					try
					{
						if(Modifier.isStatic(method.getModifiers()))
							method.invoke(clazz);
						else
						{
							if(o == null)
								o = clazz.newInstance();
							method.invoke(o);
						}
					}
					catch(Exception e)
					{
						_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Exception: %s", e );
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		return o;
	}


	public Map<String, Class<?>> getClasses()
	{
		return _classes;
	}

	public Object getClassInstance(Class<?> clazz)
	{
		return _instances.get(clazz);
	}

	public Object getClassInstance(String className)
	{
		Class<?> clazz = _classes.get(className);
		if(clazz != null)
			return getClassInstance(clazz);
		return null;
	}
}