package com.episi.recyclens.data.tflite

interface DetectorListener {
    fun onEmptyDetect()
    fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long)
}