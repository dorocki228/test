package l2s.gameserver.permission;

import l2s.gameserver.permission.interfaces.IActionPermission;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author mangol
 */
public class ActionPermissionComponent {
	private static final ThreadLocal<ActionPermissionContext> actionPermissionContextThreadLocal = ThreadLocal.withInitial(ActionPermissionContext::new);
	private final Map<EActionPermissionLevel, List<IActionPermission>> map = new ConcurrentHashMap<>();

	public static ActionPermissionComponent create() {
		return new ActionPermissionComponent();
	}

	public List<IActionPermission> getPermissionOfNullable(EActionPermissionLevel level) {
		return map.getOrDefault(level, Collections.emptyList());
	}

	public void add(EActionPermissionLevel level, IActionPermission permissionPredicate) {
		map.computeIfAbsent(level, k -> new CopyOnWriteArrayList<>()).add(permissionPredicate);
	}

	public void remove(EActionPermissionLevel level, IActionPermission permission) {
		Optional.of(getPermissionOfNullable(level)).filter(e -> !e.isEmpty()).ifPresent(c -> c.remove(permission));
	}

	public boolean anySuccess(EActionPermissionLevel level, Class<? extends IActionPermission> clazz, Object... args) {
		List<? extends IActionPermission> list = getListFromClazz(level, clazz);
		if(list.isEmpty()) {
			return false;
		}
		ActionPermissionContext context = getContext(level);
		return list.stream().
				map(p -> p.test(context, args)).
				anyMatch(e -> e == EActionPermissionReturnType.Success);
	}

	public ActionPermissionContext anyFailureContext(EActionPermissionLevel level, Class<? extends IActionPermission> clazz, Object... args) {
		ActionPermissionContext context = getContext(level);
		List<? extends IActionPermission> list = getListFromClazz(level, clazz);
		if(list.isEmpty()) {
			return context;
		}
		boolean success = list.stream().
				map(p -> p.test(context, args)).
				anyMatch(e -> e == EActionPermissionReturnType.Failure);
		context.setSuccess(success);
		return context;
	}

	public boolean anyFailure(EActionPermissionLevel level, Class<? extends IActionPermission> clazz, Object... args) {
		ActionPermissionContext actionPermissionContext = anyFailureContext(level, clazz, args);
		return actionPermissionContext.isSuccess();
	}

	private ActionPermissionContext getContext(EActionPermissionLevel level) {
		ActionPermissionContext context = actionPermissionContextThreadLocal.get();
		context.setLevel(level);
		context.setMessage(null);
		context.setSuccess(false);
		return context;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> getListFromClazz(EActionPermissionLevel level, Class<T> clazz) {
		List<IActionPermission> list = getPermissionOfNullable(level);
		if(list.isEmpty()) {
			return Collections.emptyList();
		}
		return (List<T>) list.stream().filter(clazz::isInstance).collect(Collectors.toList());
	}
}
