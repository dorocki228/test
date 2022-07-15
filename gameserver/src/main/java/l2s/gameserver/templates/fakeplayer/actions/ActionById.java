package l2s.gameserver.templates.fakeplayer.actions;

import com.google.common.flogger.FluentLogger;
import java.util.Collections;
import java.util.List;
import l2s.gameserver.templates.fakeplayer.FakePlayerActionsHolder;
import org.dom4j.Element;

/**
 * @author Bonux
**/
public class ActionById extends AbstractAction
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private final FakePlayerActionsHolder _actionsHolder;
	private final int _actionId;

	public ActionById(FakePlayerActionsHolder actionsHolder, int actionId, double chance)
	{
		super(chance);
		_actionsHolder = actionsHolder;
		_actionId = actionId;
	}

	@Override
	public List<AbstractAction> makeActionsList()
	{
		OrdinaryActions action = _actionsHolder.getAction(_actionId);
		if(action == null)
		{
			_log.atWarning().log( "Cannot find action by ID[%s]!", _actionId );
			return Collections.emptyList();
		}
		return action.makeActionsList();
	}

	public static ActionById parse(FakePlayerActionsHolder actionsHolder, Element element)
	{
		int actionId = Integer.parseInt(element.attributeValue("id"));
		double chance = element.attributeValue("chance") == null ? 100. : Double.parseDouble(element.attributeValue("chance"));
		return new ActionById(actionsHolder, actionId, chance);
	}
}