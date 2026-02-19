package com.example.statement_detect.timer

import kotlin.random.Random

fun handleTime(totalSeconds: Int, isAdd: Boolean, isMinus: Boolean): Int {
    val dayInSeconds = 86400
    var newTotalSeconds = totalSeconds
    if (isAdd) newTotalSeconds++ else if (isMinus) newTotalSeconds--
    return (newTotalSeconds + dayInSeconds) % dayInSeconds
}

fun getPhotoPoints(totalSeconds: Int, segments: Int): List<Int> {
    val result = mutableListOf(0)
    val segmentLen = totalSeconds / segments
    for (i in 1 until segments) {
        val fix = if ((-segmentLen * 0.4).toInt() >= (segmentLen * 0.4).toInt()) {
            0
        } else {
            Random.nextInt((-segmentLen * 0.4).toInt(), (segmentLen * 0.4).toInt())
        }
        result.add(segmentLen * i + fix)
    }
    return result
}
