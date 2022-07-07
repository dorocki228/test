package l2s.gameserver.scripts;

import l2s.commons.compiler.Compiler;
import l2s.commons.compiler.MemoryClassLoader;
import l2s.commons.io.MoreFiles;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.listener.script.OnLoadScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scripts {
    private static final Logger _log = LoggerFactory.getLogger(Scripts.class);

    private static final Scripts _instance = new Scripts();

    private Map<String, Class<?>> _classes;
    private final ScriptListenerImpl _listeners;

    public static Scripts getInstance() {
        return _instance;
    }

    private Scripts() {
        _classes = new TreeMap<>();
        _listeners = new ScriptListenerImpl();
        load();
    }

    private void load() {
        _log.info("Scripts: Loading...");

        Path scriptsPath = Config.SCRIPTS_PATH;
        if (Files.notExists(scriptsPath))
            throw new IllegalArgumentException("Can't find scripts by path " + scriptsPath.toAbsolutePath());
        boolean loadingFromJar = scriptsPath.toString().endsWith(".jar");
        var scripts = loadingFromJar ? loadJar(scriptsPath) : loadScripts(scriptsPath);
        _classes = scripts.stream().collect(Collectors.toUnmodifiableMap(Class::getName, Function.identity()));

        _log.info("Scripts: Loaded {} classes " + (loadingFromJar ? "from jar." : "from scripts."), _classes.size());

        _listeners.load();
    }

    public void init() {
        for (Class<?> clazz : _classes.values())
            init(clazz);
        _listeners.init();
    }

    public List<? extends Class<?>> loadJar(Path jar) {
        var result = new ArrayList<Class<?>>();

        try (URLClassLoader scriptsLoader = new URLClassLoader(new URL[]{jar.toFile().toURI().toURL()});
             InputStream inputStream = Files.newInputStream(jar);
             JarInputStream stream = new JarInputStream(inputStream)) {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                //Вложенные класс
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                String name = entry.getName().replace(".class", "").replace("/", ".");

                var clazz = loadClass(scriptsLoader, name);
                if (clazz != null)
                    result.add(clazz);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Fail to load scripts.jar!", e);
        }

        return result;
    }

    public List<? extends Class<?>> loadScripts(Path scriptsPath) {
        Collection<Path> scriptFiles;
        try {
            scriptFiles = MoreFiles.collectFiles(scriptsPath);
        } catch (IOException e) {
            _log.error("Can't read file(s) {}.", scriptsPath, e);
            return List.of();
        }

        if (scriptFiles.isEmpty())
            return List.of();

        Compiler compiler = new Compiler();
        if (compiler.compile(scriptFiles)) {
            MemoryClassLoader classLoader = compiler.getClassLoader();
            return classLoader.getLoadedClasses()
                    .filter(name -> !name.contains(ClassUtils.INNER_CLASS_SEPARATOR))
                    .map(name -> loadClass(classLoader, name))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableList());
        }

        return List.of();
    }

    private Class<?> loadClass(ClassLoader classLoader, String name) {
        try {
            Class<?> clazz = classLoader.loadClass(name);

            if (Modifier.isAbstract(clazz.getModifiers()))
                return null;

            if (ClassUtils.isAssignable(clazz, OnLoadScriptListener.class)) {
                if (ClassUtils.isAssignable(clazz, OnInitScriptListener.class))
                    _log.warn("Scripts: Error in class: {}. " +
                            "Can not use OnLoad and OnInit listeners together!", clazz.getName());
                if (Arrays.stream(clazz.getMethods())
                        .anyMatch(method -> method.isAnnotationPresent(Bypass.class)))
                    _log.warn("Scripts: Error in class: {}. " +
                            "Can not use OnLoad listener and bypass annotation together!", clazz.getName());
                _listeners.add((Listener) clazz.getDeclaredConstructor().newInstance());
            }

            _log.info("Script {} loaded.", name);

            return clazz;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            _log.error("Scripts: Can't load script class: " + name, e);
        }

        return null;
    }

    private Object init(Class<?> clazz) {
        if (ClassUtils.isAssignable(clazz, OnLoadScriptListener.class))
            return null;
        Object o = null;
        try {
            if (ClassUtils.isAssignable(clazz, OnInitScriptListener.class)) {
                o = clazz.getDeclaredConstructor().newInstance();
                _listeners.add((Listener) o);
            }
            for (Method method : clazz.getMethods())
                if (method.isAnnotationPresent(Bypass.class)) {
                    Bypass an = method.getAnnotation(Bypass.class);
                    if (o == null)
                        o = clazz.getDeclaredConstructor().newInstance();
                    Class<?>[] par = method.getParameterTypes();
                    if (par.length == 0 || par[0] != Player.class || par[1] != NpcInstance.class || par[2] != String[].class)
                        _log.error("Wrong parameters for bypass method: " + method.getName() + ", class: " + clazz.getSimpleName());
                    else
                        BypassHolder.getInstance().registerBypass(an.value(), o, method);
                }
        } catch (Exception e) {
            _log.error("Can't init {} class.", clazz.getName(), e);
        }
        return o;
    }

    public Map<String, Class<?>> getClasses() {
        return _classes;
    }

    public class ScriptListenerImpl extends ListenerList<Scripts> {
        public void load() {
            for (Listener<Scripts> listener : getListeners())
                if (OnLoadScriptListener.class.isInstance(listener))
                    ((OnLoadScriptListener) listener).onLoad();
        }

        public void init() {
            for (Listener<Scripts> listener : getListeners())
                if (OnInitScriptListener.class.isInstance(listener))
                    ((OnInitScriptListener) listener).onInit();
        }
    }
}
