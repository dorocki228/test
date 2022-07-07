package l2s.commons.reflect.converter;

import l2s.commons.reflect.converter.converters.IConverter;
import l2s.commons.reflect.converter.numberConverters.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Mangol
 * @since 09.01.2016
 */
public class Converter {
	@SuppressWarnings("unchecked")
	public static <T> T convert(final Class<? extends T> type, final String value) {
		final Optional<INumberConverter> numberConverter = Optional.ofNullable(RegisterObject.getNumberConverter(type));
		if(numberConverter.isPresent()) {
			return (T) numberConverter.get().toObject(value);
		}
		final Optional<IConverter> converter = Optional.ofNullable(RegisterObject.getConverter(type));
		if(converter.isPresent()) {
			return (T) converter.get().toObject(value);
		}
		throw new NullPointerException(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] convertArraysNumber(final Class<?> type, final String value, final String split) {
		final Optional<INumberConverter> numberConverter = Optional.ofNullable(RegisterObject.getNumberConverter(type));
		if(numberConverter.isPresent()) {
			return (T[]) numberConverter.get().toArrays(value, split);
		}
		throw new NullPointerException(value);
	}

	public static int[] toIntArray(final String value, final String split) {
		return toIntArray(value, split, null);
	}

	public static int[] toIntArray(final String value, final String split, final int[] jobArray) {
		final INumberConverter converter = RegisterObject.getNumberConverter(Integer.class);
		final IntegerConverter integerConverter = (IntegerConverter) converter;
		return integerConverter.toIntArraysPrimitive(value, split, jobArray);
	}

	public static double[] toDoubleArray(final String value, final String split) {
		final INumberConverter converter = RegisterObject.getNumberConverter(Double.class);
		final DoubleConverter doubleConverter = (DoubleConverter) converter;
		return doubleConverter.toDoubleArraysPrimitive(value, split);
	}

	public static float[] toFloatArray(final String value, final String split) {
		final INumberConverter converter = RegisterObject.getNumberConverter(Float.class);
		final FloatConverter floatConverter = (FloatConverter) converter;
		return floatConverter.toFloatPrimitive(value, split);
	}

	public static long[] toLongArray(final String value, final String split) {
		final INumberConverter converter = RegisterObject.getNumberConverter(Long.class);
		final LongConverter longConverter = (LongConverter) converter;
		return longConverter.toLongPrimitive(value, split);
	}

	public static byte[] toByteArray(final String value, final String split) {
		final INumberConverter converter = RegisterObject.getNumberConverter(Byte.class);
		final ByteConverter byteConverter = (ByteConverter) converter;
		return byteConverter.toBytePrimitive(value, split);
	}

	public static short[] toShortArray(final String value, final String split) {
		final INumberConverter converter = RegisterObject.getNumberConverter(Short.class);
		final ShortConverter shortConverter = (ShortConverter) converter;
		return shortConverter.toShortPrimitive(value, split);
	}

	/**
	 * @param clazz
	 * @param value
	 * @param split
	 * @return
	 */
	public static <T extends Number> List<T> toList(Class<T> clazz, String value, String split) {
		return toList(new ArrayList<T>(), clazz, value, split);
	}

	/**
	 * @param list
	 * @param clazz
	 * @param value
	 * @param split
	 * @return
	 */
	public static <T extends Number> List<T> toList(List<T> list, Class<T> clazz, String value, String split) {
		Objects.requireNonNull(list);
		if(value == null || value.isEmpty()) {
			return list;
		}
		String[] arrays = value.split(split);
		for(String val : arrays) {
			list.add(convert(clazz, val));
		}
		return list;
	}

	public static String arrayToString(final int[] array, String split) {
		return arrayToString(array, split, -1);
	}

	public static String arrayToString(final int[] array, final String split, final int maxSize) {
		if(array == null || array.length == 0)
			return "";
		final StringBuilder builder = new StringBuilder();
		final int startIndex = 0;
		final int endIndex = array.length;
		for(int i = startIndex; i < endIndex; i++) {
			if(maxSize != -1 && i == maxSize)
				break;
			String sp = i == startIndex ? "" : split;
			builder.append(sp);
			builder.append(array[i]);
		}
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum> Enum[] toEnums(final String val, final Class<T> enumClazz, final String split) {
		String[] values = val.split(split);
		Enum[] array = (Enum[]) Array.newInstance(enumClazz, values.length);
		for(int i = 0; i < values.length; i++)
			array[i] = Enum.valueOf(enumClazz, values[i]);
		return array;
	}
}