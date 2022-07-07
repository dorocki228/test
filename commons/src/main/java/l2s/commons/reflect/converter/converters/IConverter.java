package l2s.commons.reflect.converter.converters;

/**
 * @author Mangol
 * @since 23.01.2016
 */
public interface IConverter<T>
{
	T toObject(final String value);
}