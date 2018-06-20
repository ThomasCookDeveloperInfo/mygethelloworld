package com.condecosoftware.core.utils

/**
 * Collection of useful functions
 */
object BasicUtils {

    /**
     * Used to check if one of the list of objects supports desired class type and if it does
     * returns cast reference.
     *
     * @param interfaceClass Class type we would like to check if the objects support it.
     * @param objects        Array of objects that need checking
     * @return If one of the specified objects is instance of the interfaceClass then
     */
    fun <T> getClassInstance(interfaceClass: Class<T>, vararg objects: Any): T? {
        for (obj in objects) {
            if (interfaceClass.isInstance(obj)) {
                return interfaceClass.cast(obj)
            }
        }
        return null
    }
}
