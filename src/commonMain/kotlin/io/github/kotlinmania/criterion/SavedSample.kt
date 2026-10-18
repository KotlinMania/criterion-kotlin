package io.github.kotlinmania.criterion

import kotlinx.serialization.Serializable

@Serializable
data class SavedSample(
    val samplingMode: ActualSamplingMode,
    val iters: List<Double>,
    val times: List<Double>,
)
