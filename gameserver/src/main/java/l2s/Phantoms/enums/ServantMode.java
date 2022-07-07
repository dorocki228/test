package  l2s.Phantoms.enums;

public enum ServantMode
{
	FARM(new int[] { 2, 1, 1, 1, 1, 1 }),
	ASSIST(new int[] { 1, 2, 1, 1, 1, 1 }),
	PROTECTION(new int[] { 1, 1, 2, 1, 1, 1 }),
	SUPPORT(new int[] { 1, 1, 1, 2, 1, 1 }),
	STOP(new int[] { 1, 1, 1, 1, 2, 1 });

	private int[] button_state;

	ServantMode(int[] i)
	{
		button_state = i;
	}

	public int[] getButtonState()
	{
		return button_state;
	}

}
