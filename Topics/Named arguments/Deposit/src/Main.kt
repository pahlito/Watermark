import kotlin.math.pow

fun main() {
    val parameter = readln()
    val value = readln().toInt()
    println(
        when (parameter) {
            "amount" -> creditTotal( amount = value).toInt()
            "percent" -> creditTotal( percent = value).toInt()
            "years" -> creditTotal( years = value).toInt()
            else -> creditTotal()
        }
    )
}

fun creditTotal(amount: Int = 1000, percent: Int = 5, years: Int = 10): Double {
    return amount.toDouble() * (1 + percent.toDouble() / 100).pow(years)
}