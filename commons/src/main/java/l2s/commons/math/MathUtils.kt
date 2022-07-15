package l2s.commons.math

object MathUtils {

    /**
     * Constrains a number to be within a range.
     * @param input the number to constrain, all data types
     * @param min the lower end of the range, all data types
     * @param max the upper end of the range, all data types
     * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
     */
    fun constrain(input: Int, min: Int, max: Int): Int {
        return if (input < min) min else if (input > max) max else input
    }

    /**
     * Constrains a number to be within a range.
     * @param input the number to constrain, all data types
     * @param min the lower end of the range, all data types
     * @param max the upper end of the range, all data types
     * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
     */
    fun constrain(input: Long, min: Long, max: Long): Long {
        return if (input < min) min else if (input > max) max else input
    }

    /**
     * Constrains a number to be within a range.
     * @param input the number to constrain, all data types
     * @param min the lower end of the range, all data types
     * @param max the upper end of the range, all data types
     * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
     */
    fun constrain(input: Double, min: Double, max: Double): Double {
        return if (input < min) min else if (input > max) max else input
    }

    /**
     * Constrains a number to be within a range.
     * @param input the number to constrain, all data types
     * @param min the lower end of the range, all data types
     * @param max the upper end of the range, all data types
     * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
     */
    fun constrain(input: Float, min: Float, max: Float): Float {
        return if (input < min) min else if (input > max) max else input
    }

    fun add(oldValue: Double, value: Double): Double {
        return oldValue + value
    }

    fun mul(oldValue: Double, value: Double): Double {
        return oldValue * value
    }

    fun div(oldValue: Double, value: Double): Double {
        return oldValue / value
    }

}