package l2s.gameserver.taskmanager;

import l2s.gameserver.taskmanager.tasks.AutomaticTask;
import l2s.gameserver.taskmanager.tasks.DailyTask;
import l2s.gameserver.taskmanager.tasks.DeleteExpiredMailTask;
import l2s.gameserver.taskmanager.tasks.WeeklyTask;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class AutomaticTasks {
    private static final AutomaticTasks INSTANCE = new AutomaticTasks();
    private final Map<Class<? extends AutomaticTask>, AutomaticTask> tasks = new ConcurrentHashMap<>();

    private AutomaticTasks() {
    }

    public void init() {
        add(new DailyTask(), true);
        add(new DeleteExpiredMailTask(), true);
        add(new WeeklyTask(), true);
    }

    public void addOrReplace(AutomaticTask task, boolean start) {
        final AutomaticTask t = tasks.put(task.getClass(), task);
        if(t != null)
            t.stopTask();
        task.init(start);
    }

    public void add(AutomaticTask task, boolean start) {
        final AutomaticTask t = tasks.putIfAbsent(task.getClass(), task);
        if(!Objects.equals(t, task))
            task.init(start);
    }
    public static AutomaticTasks getInstance() {
        return INSTANCE;
    }
}
