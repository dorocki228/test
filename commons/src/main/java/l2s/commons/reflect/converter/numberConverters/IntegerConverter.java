package l2s.commons.reflect.converter.numberConverters;
import java.util.stream.IntStream;

/**
 * @author Mangol
 * @since 16.01.2016
 */
public class IntegerConverter implements INumberConverter<Integer> {
	@Override
	public Integer toObject(final String value) {
		return Integer.valueOf(value);
	}

	@Override
	public int[] toIntArraysPrimitive(final String value, final String split, final int[] jobArray) {
		if(value == null || value.isEmpty()) {
			if(jobArray != null && jobArray.length > 0)
				return jobArray;
			else
				return new int[0];
		}
		final String[] str = value.split(split);
		final int[] array;
		if(jobArray != null && jobArray.length > 0)
			array = jobArray;
		else
			array = new int[str.length];
		int endIndex = array.length > str.length ? str.length : array.length;
		IntStream.range(0, endIndex).forEach(i -> array[i] = toObject(str[i]));
		return array;
	}

	@Override
	public Integer[] toArrays(final String value, final String split) {
		final String[] str = value.split(split);
		final Integer[] array = new Integer[str.length];
		IntStream.range(0, str.length).forEach(i -> array[i] = toObject(str[i]));
		return array;
	}
}