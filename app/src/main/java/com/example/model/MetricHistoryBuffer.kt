package com.example.model

/**
 * High-performance circular buffer for real-time telemetry graphs.
 * Avoids object allocation on every tick.
 */
class MetricHistoryBuffer(val capacity: Int = 80) {
    private val buffer = FloatArray(capacity)
    private var head = 0
    private var count = 0

    @Synchronized
    fun add(value: Float) {
        buffer[head] = value
        head = (head + 1) % capacity
        if (count < capacity) count++
    }

    @Synchronized
    fun toList(): List<Float> {
        val result = ArrayList<Float>(count)
        val start = if (count < capacity) 0 else head
        for (i in 0 until count) {
            val idx = (start + i) % capacity
            result.add(buffer[idx])
        }
        return result
    }

    @Synchronized
    fun clear() {
        head = 0
        count = 0
    }

    @Synchronized
    fun latest(): Float {
        if (count == 0) return 0f
        val prev = if (head == 0) capacity - 1 else head - 1
        return buffer[prev]
    }
}
