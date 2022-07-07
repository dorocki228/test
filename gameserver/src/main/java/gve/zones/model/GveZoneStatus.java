package gve.zones.model;

/**
 * @author Java-man
 * @since 11.04.2018
 */
public enum GveZoneStatus
{
	DISABLED("d40204")
	{
		@Override
		public String toString()
		{
			return "Disabled";
		}
	},
	ENABLED("d2e500")
	{
		@Override
		public String toString()
		{
			return "Enabled";
		}
	},
	ACTIVATED("78c96d")
	{
		@Override
		public String toString()
		{
			return "Activated";
		}
	};

	private final String color;

	GveZoneStatus(String color)
	{
		this.color = color;
	}

	public String getColor()
	{
		return color;
	}
}
