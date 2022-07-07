package l2s.commons.reflect;

import l2s.commons.reflect.converter.Converter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * @author Mangol
 * @since 19.11.2016
 */
public final class FieldHelper {
    /**
     * Support: int, long, double, float, byte, short; Enum, Integer, Float, Double, Byte, String, Short, Long
     *
     * @param field
     * @param obj
     * @param value
     * @param splitter
     * @throws IllegalAccessException
     */
    public static void setArrayPrimitiveAndObjectField(final Field field, final Object obj, final String value, final String splitter) throws IllegalAccessException {
        if(field.getType() == int[].class)
            field.set(obj, Converter.toIntArray(value, splitter));
        else if(field.getType() == long[].class)
            field.set(obj, Converter.toLongArray(value, splitter));
        else if(field.getType() == double[].class)
            field.set(obj, Converter.toDoubleArray(value, splitter));
        else if(field.getType() == float[].class)
            field.set(obj, Converter.toFloatArray(value, splitter));
        else if(field.getType() == byte[].class)
            field.set(obj, Converter.toByteArray(value, splitter));
        else if(field.getType() == short[].class)
            field.set(obj, Converter.toShortArray(value, splitter));
        else
            setArrayObjectField(field, obj, value, splitter);
    }

    /**
     * Support: Enum, Integer, Float, Double, Byte, String, Short, Long
     *
     * @param field
     * @param obj
     * @param value
     * @param splitter
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public static void setArrayObjectField(final Field field, final Object obj, final String value, final String splitter) throws IllegalAccessException {
        if(field.getType().getComponentType().isEnum()) {
            String[] str = value.split(splitter);
            Class<? extends Enum> clazz = (Class<? extends Enum>) field.getType().getComponentType();
            Enum[] array = (Enum[]) Array.newInstance(clazz, str.length);
            for (int i = 0; i < str.length; i++)
                array[i] = Enum.valueOf(clazz, str[i]);
            field.set(obj, array);
        } else if(field.getType() == String[].class)
            field.set(obj, value.split(splitter));
        else
            field.set(obj, Converter.convertArraysNumber(field.getType(), value, splitter));
    }

    /**
     * Support: Enum, Integer, Float, Double, Byte, String, Short, Long, String
     *
     * @param field
     * @param obj
     * @param value
     * @throws IllegalAccessException
     */
    public static void setObjectField(final Field field, final Object obj, final String value) throws IllegalAccessException {
        setObject(obj, value, field);
    }

    /**
     * Support: Enum, Integer, Float, Double, Byte, String, Short, Long, String
     *
     * @param obj
     * @param fieldName
     * @param value
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public static void setObjectField(final Object obj, final String fieldName, final String value) throws IllegalAccessException, NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        setObject(obj, value, field);
        field.setAccessible(false);
    }

    @SuppressWarnings("unchecked")
    private static void setObject(Object obj, String value, Field field) throws IllegalAccessException {
        if(field.getType().isEnum())
            field.set(obj, Enum.valueOf((Class<? extends Enum>) field.getType(), value));
        else if(field.getType() == String.class)
            field.set(obj, value);
        else
            field.set(obj, Converter.convert(field.getType(), value));
    }

    /**
     * Support: int, float, double, byte, short, long, boolean
     *
     * @param field
     * @param obj
     * @param value
     * @throws IllegalAccessException
     */
    public static void setPrimitiveField(final Field field, final Object obj, final String value) throws IllegalAccessException {
        field.set(obj, Converter.convert(field.getType(), value));
    }

    /**
     * Support: (int, long, double, float, byte, short; Enum, Integer, Float, Double, Byte, String, Short, Long) and Array
     *
     * @param obj
     * @param fieldName
     * @param value
     * @param splitter
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public static void setObjectField(final Object obj, final String fieldName, final String value, final String splitter) throws IllegalAccessException, NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        boolean accessible = field.canAccess(obj);
        field.setAccessible(true);
        if(field.getType().isArray())
            FieldHelper.setArrayPrimitiveAndObjectField(field, obj, value, splitter);
        else if(field.getType().isPrimitive())
            FieldHelper.setPrimitiveField(field, obj, value);
        else
            FieldHelper.setObjectField(field, obj, value);
        field.setAccessible(accessible);
    }
}