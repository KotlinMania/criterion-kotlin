package io.github.kotlinmania.criterion

enum class AxisScale {
    Linear,
    Logarithmic,
}

data class PlotConfiguration(
    val summaryScale: AxisScale = AxisScale.Linear,
)
