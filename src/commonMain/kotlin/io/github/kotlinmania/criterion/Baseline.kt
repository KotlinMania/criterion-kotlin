package io.github.kotlinmania.criterion

/**
 * Baseline describes how the baseline directory is handled.
 */
enum class Baseline {
    /**
     * Compares against a previously saved baseline if it exists.
     */
    CompareLenient,

    /**
     * Compares against a previously saved baseline. Panics if missing.
     */
    CompareStrict,

    /**
     * Overwrites baseline directory with new results.
     */
    Save,

    /**
     * Discard benchmark results.
     */
    Discard,
}
