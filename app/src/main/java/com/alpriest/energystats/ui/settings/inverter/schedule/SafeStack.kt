package com.alpriest.energystats.ui.settings.inverter.schedule

class SafeStack<T> {
    private val items = mutableListOf<T>()

    val isEmpty: Boolean
        get() = items.isEmpty()

    fun push(item: T) {
        items.add(item)
    }

    fun pop(): T? {
        if (isEmpty) return null
        return items.removeAt(items.size - 1)
    }

    val size: Int
        get() = items.size
}