package l2s.gameserver.utils.velocity;

import l2s.gameserver.Config;
import l2s.gameserver.utils.TimeUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.util.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class VelocityUtils
{
	private static final Logger _log = LoggerFactory.getLogger(VelocityUtils.class);

	public static final Map<String, Object> GLOBAL_VARIABLES = new HashMap<>();

	public static void init()
	{
		Velocity.setProperty("resource.default_encoding", "UTF-8");
		Velocity.init();

		GLOBAL_VARIABLES.put("TimeUtils", TimeUtils.class);

		Field[] declaredFields = Config.class.getDeclaredFields();
		for(Field f : declaredFields)
			try
			{
				if(f.isAnnotationPresent(VelocityVariable.class))
					GLOBAL_VARIABLES.put(f.getName(), f.get(null));
			}
			catch(IllegalAccessException e)
			{
				throw new Error(e);
			}
	}

	private static String evaluate0(String text, Map<String, Object> variables)
	{
		if(variables.isEmpty())
			return text;
		VelocityContext velocityContext = new VelocityContext(variables);
		Writer writer = new StringBuilderWriter(new StringBuilder(text.length() + 32));
		if(!Velocity.evaluate(velocityContext, writer, "", text))
		{
			_log.warn("Fail to evaluate: \n" + text);
			return "";
		}
		return writer.toString();
	}

	public static String evaluate(String text, Map<String, Object> variables)
	{
		if(variables != null)
		{
			variables.putAll(GLOBAL_VARIABLES);
			return evaluate0(text, variables);
		}
		else
		{
			variables = GLOBAL_VARIABLES;
			return evaluate0(text, variables);
		}
	}
}
