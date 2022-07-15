package l2s.commons.math

/**
 * Constrains a number to be within a range.
 * @param min the lower end of the range, all data types
 * @param max the upper end of the range, all data types
 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
 */
fun Int.constrain(min: Int, max: Int): Int {
    return if (this < min) min else if (this > max) max else this
}

/**
 * Constrains a number to be within a range.
 * @param min the lower end of the range, all data types
 * @param max the upper end of the range, all data types
 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
 */
fun Long.constrain(min: Long, max: Long): Long {
    return if (this < min) min else if (this > max) max else this
}

/**
 * Constrains a number to be within a range.
 * @param min the lower end of the range, all data types
 * @param max the upper end of the range, all data types
 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
 */
fun Double.constrain(min: Double, max: Double): Double {
    return if (this < min) min else if (this > max) max else this
}

/**
 * Constrains a number to be within a range.
 * @param min the lower end of the range, all data types
 * @param max the upper end of the range, all data types
 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
 */
fun Float.constrain(min: Float, max: Float): Float {
    return if (this < min) min else if (this > max) max else this
}