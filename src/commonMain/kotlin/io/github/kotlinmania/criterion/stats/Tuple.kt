// port-lint: source stats/tuple.rs
package io.github.kotlinmania.criterion.stats

/**
 * Any tuple of values.
 */
internal interface Tuple

/**
 * A tuple of distributions associated with a tuple.
 */
internal interface TupledDistributions

/**
 * A builder for creating tupled distributions.
 */
internal interface TupledDistributionsBuilder<T : Tuple, D : TupledDistributions> {
    fun push(tuple: T)

    fun extend(other: TupledDistributionsBuilder<T, D>)

    fun complete(): D
}

internal data class Tuple1<A>(
    val first: A,
) : Tuple

internal data class Tuple2<A, B>(
    val first: A,
    val second: B,
) : Tuple

internal data class Tuple3<A, B, C>(
    val first: A,
    val second: B,
    val third: C,
) : Tuple

internal data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
) : Tuple

internal data class Dist1<A : Any>(
    val first: Distribution<A>,
) : TupledDistributions

internal data class Dist2<A : Any, B : Any>(
    val first: Distribution<A>,
    val second: Distribution<B>,
) : TupledDistributions

internal data class Dist3<A : Any, B : Any, C : Any>(
    val first: Distribution<A>,
    val second: Distribution<B>,
    val third: Distribution<C>,
) : TupledDistributions

internal data class Dist4<A : Any, B : Any, C : Any, D : Any>(
    val first: Distribution<A>,
    val second: Distribution<B>,
    val third: Distribution<C>,
    val fourth: Distribution<D>,
) : TupledDistributions

internal class Builder1<A : Any>(
    size: Int,
) : TupledDistributionsBuilder<Tuple1<Double>, Dist1<A>> {
    private val list = ArrayList<Double>(size)

    override fun push(tuple: Tuple1<Double>) {
        list.add(tuple.first)
    }

    override fun extend(other: TupledDistributionsBuilder<Tuple1<Double>, Dist1<A>>) {
        if (other is Builder1<A>) {
            list.addAll(other.list)
        }
    }

    override fun complete(): Dist1<A> = Dist1(Distribution(list.toDoubleArray()))
}

internal class Builder2<A : Any, B : Any>(
    size: Int,
) : TupledDistributionsBuilder<Tuple2<Double, Double>, Dist2<A, B>> {
    private val list1 = ArrayList<Double>(size)
    private val list2 = ArrayList<Double>(size)

    override fun push(tuple: Tuple2<Double, Double>) {
        list1.add(tuple.first)
        list2.add(tuple.second)
    }

    override fun extend(other: TupledDistributionsBuilder<Tuple2<Double, Double>, Dist2<A, B>>) {
        if (other is Builder2<A, B>) {
            list1.addAll(other.list1)
            list2.addAll(other.list2)
        }
    }

    override fun complete(): Dist2<A, B> = Dist2(Distribution(list1.toDoubleArray()), Distribution(list2.toDoubleArray()))
}
