package l2s.commons.lang.reference;

public class AbstractHardReference<T> implements HardReference<T>
{
	private T reference;

	public AbstractHardReference(T reference)
	{
		this.reference = reference;
	}

	@Override
	public T get()
	{
		return reference;
	}

	@Override
	public void clear()
	{
        reference = null;
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this
				|| o instanceof AbstractHardReference && ((AbstractHardReference) o).get() != null
				&& ((AbstractHardReference) o).get().equals(get());
	}

	@Override
	public int hashCode()
	{
		T reference = get();
		if (reference != null) {
			return 17 * reference.hashCode() + 16410;
		} else {
			return 0;
		}
	}
}
