import kotlin.math.abs
import kotlin.math.hypot

fun perimeter(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double, x4: Double? = null, y4: Double? = null): Double {

    var per:Double = 0.0;
    per += hypot(abs(x1 - x2), abs(y1 - y2))
    per += hypot(abs(x2 - x3), abs(y2 - y3))
    per += if(null == x4 || null == y4){
        hypot(abs(x3 - x1), abs(y3 - y1))
    }else{
        hypot(abs(x3 - x4), abs(y3 - y4)) +
                hypot(abs(x4 - x1), abs(y4 - y1))
    }
    return per
}