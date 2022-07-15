package l2s.commons.util

import com.google.common.collect.ImmutableSet
import com.google.common.reflect.ClassPath

object ReflectionUtils {

    private val classpath: ClassPath = ClassPath.from(javaClass.classLoader)

    private val allClasses: ImmutableSet<ClassPath.ClassInfo> = classpath.allClasses

    fun findClassByName(name: String): Class<out Any>? {
        return allClasses.find { classInfo -> classInfo.simpleName == name }?.load()
    }

}