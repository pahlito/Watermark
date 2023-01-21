import java.awt.Color
import java.awt.image.BufferedImage  

fun drawCircles(): BufferedImage {
  // Add your code here
    val image = BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)
    val graphics = image.getGraphics()
    graphics.color = Color.RED
    graphics.drawOval(50, 50, 100, 100)
    graphics.color = Color.YELLOW
    graphics.drawOval(50, 75, 100, 100)
    graphics.color = Color.GREEN
    graphics.drawOval(75, 50, 100, 100)
    graphics.color = Color.BLUE
    graphics.drawOval(75, 75, 100, 100)
    return image
}
