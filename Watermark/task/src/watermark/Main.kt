package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

val VALID_EXTENSIONS = arrayOf("jpg", "png")
val VALID_PIXEL_SIZE = arrayOf(24, 32)
val TRANSPARENT_COLOR = Color(0, 0, 0, 0)
val VALID_COLOR_RANGE = 0..255

fun main() {
    val image = getBaseImage()
    val watermark = getWatermarkImage(image)
    val useAlpha = getUseAlpha(watermark)
    val percentage = askForPercentage()
    val outputImage = getOutputImage(image, watermark, percentage, useAlpha)
    val output = writeOutputImageFile(outputImage)
    println("The watermarked image $output has been created.")
}

private fun getBaseImage(): BufferedImage {
    println("Input the image filename: ")
    val fileName = readln()
    val image = getImageFromFilename(fileName)
    validateImage(image, "image")
    return image
}

private fun getWatermarkImage(image: BufferedImage): BufferedImage {
    println("Input the watermark image filename: ")
    val watermarkFile = readln()
    val watermark = getImageFromFilename(watermarkFile)
    validateImage(watermark, "watermark")
    validateImagesSizes(image, watermark)
    return watermark
}

private fun askForPercentage(): Int {
    println("Input the watermark transparency percentage (Integer 0-100):")
    val percentageInput = readln()
    return getPercentage(percentageInput)
}

private fun writeOutputImageFile(outputImage: BufferedImage): String {
    println("Input the output image filename (jpg or png extension):")
    val output = readln()
    val outputType = getOutputExtension(output)
    ImageIO.write(outputImage, outputType, File(output))
    return output
}

private fun getOutputImage(
    image: BufferedImage,
    watermark: BufferedImage,
    percentage: Int,
    useAlpha: Color?
): BufferedImage {
    println("Choose the position method (single, grid):")
    val outputImage = when (readln()) {
        "single" -> {
            val (x, y) = getWatermarkPosition(image, watermark)
            blendImagesSingle(image, watermark, percentage, useAlpha, x, y)
        }
        "grid" -> blendImagesGrid(image, watermark, percentage, useAlpha)
        else -> {
            println("The position method input is invalid.")
            exitProcess(0)
        }
    }
    return outputImage
}

private fun getWatermarkPosition(
    image: BufferedImage,
    watermark: BufferedImage
): List<Int> {
    val maxX = image.width - watermark.width
    val maxY = image.height - watermark.height
    println("Input the watermark position ([x 0-$maxX] [y 0-$maxY]):")
    val xy = listFromSpacedInput("The position input is invalid.")
    if (xy.size != 2 || xy[0] !in 0..maxX || xy[1] !in 0..maxY) {
        println(if (xy.size != 2) "The position input is invalid." else "The position input is out of range.")
        exitProcess(0)
    }
    return xy
}

private fun getUseAlpha(watermark: BufferedImage) =
    if (watermark.colorModel.transparency == Transparency.TRANSLUCENT) {
        println("Do you want to use the watermark's Alpha channel?")
        if ("yes" == readln().lowercase()) TRANSPARENT_COLOR else null
    } else {
        println("Do you want to set a transparency color?")
        getAlphaColor()
    }

private fun getAlphaColor() = if ("yes" == readln().lowercase()) {
    println("Input a transparency color ([Red] [Green] [Blue]):")
    val rgb = listFromSpacedInput("The transparency color input is invalid.")
    if (rgb.size != 3 || rgb[0] !in VALID_COLOR_RANGE || rgb[1] !in VALID_COLOR_RANGE
        || rgb[2] !in VALID_COLOR_RANGE
    ) {
        println("The transparency color input is invalid.")
        exitProcess(0)
    }
    Color(rgb[0], rgb[1], rgb[2])
} else null

private fun listFromSpacedInput(error: String) = readln().split(" ").map {
    try {
        it.toInt()
    } catch (e: NumberFormatException) {
        println(error)
        exitProcess(0)
    }
}

private fun blendImagesGrid(
    image: BufferedImage,
    watermark: BufferedImage,
    weight: Int,
    useAlpha: Color?
): BufferedImage {
    val resultImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val color = blendPixel(
                Color(image.getRGB(x, y)),
                Color(
                    watermark.getRGB(x % watermark.width, y % watermark.height),
                    useAlpha == TRANSPARENT_COLOR
                ),
                weight,
                useAlpha
            )
            resultImage.setRGB(x, y, color.rgb)
        }
    }
    return resultImage
}

private fun blendImagesSingle(
    image: BufferedImage,
    watermark: BufferedImage,
    weight: Int,
    useAlpha: Color?,
    posx: Int,
    posy: Int
): BufferedImage {
    val resultImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val rangeX = posx until posx + watermark.width
    val rangeY = posy until posy + watermark.height
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            if (x in rangeX && y in rangeY) {
                val color = blendPixel(
                    Color(image.getRGB(x, y)),
                    Color(watermark.getRGB(x - posx, y - posy), useAlpha == TRANSPARENT_COLOR),
                    weight,
                    useAlpha
                )
                resultImage.setRGB(x, y, color.rgb)
            } else {
                resultImage.setRGB(x, y, image.getRGB(x, y))
            }
        }
    }
    return resultImage
}

private fun blendPixel(imageColor: Color, watermarkColor: Color, weight: Int, useAlpha: Color?) =
    when (useAlpha) {
        TRANSPARENT_COLOR -> blendPixelWithAlpha(imageColor, watermarkColor, weight)
        null -> blendPixelOldSchool(imageColor, watermarkColor, weight)
        else -> blendPixelWithColor(imageColor, watermarkColor, weight, useAlpha)
    }


private fun blendPixelOldSchool(imageColor: Color, watermarkColor: Color, weight: Int) = Color(
    (weight * watermarkColor.red + (100 - weight) * imageColor.red) / 100,
    (weight * watermarkColor.green + (100 - weight) * imageColor.green) / 100,
    (weight * watermarkColor.blue + (100 - weight) * imageColor.blue) / 100
)

private fun blendPixelWithAlpha(imageColor: Color, watermarkColor: Color, weight: Int) =
    if (watermarkColor.alpha == 0) imageColor else blendPixelOldSchool(
        imageColor,
        watermarkColor,
        weight
    )

private fun blendPixelWithColor(
    imageColor: Color,
    watermarkColor: Color,
    weight: Int,
    useAlpha: Color
) =
    if (watermarkColor.rgb == useAlpha.rgb) imageColor else blendPixelOldSchool(
        imageColor,
        watermarkColor,
        weight
    )

private fun getOutputExtension(output: String): String {
    val ext = output.split(".").last()
    if (!VALID_EXTENSIONS.contains(ext)) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(0)
    }
    return ext
}

private fun getPercentage(percentageInput: String): Int {
    if (!percentageInput.all { char -> char.isDigit() }) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
    val percentage = percentageInput.toInt()
    if (percentage !in 0..100) {
        println("The transparency percentage is out of range.")
        exitProcess(0)
    }
    return percentage
}

fun getImageFromFilename(fileName: String): BufferedImage {
    val imageFile = File(fileName)
    if (imageFile.exists()) {
        return ImageIO.read(imageFile)
    } else {
        println("The file $fileName doesn't exist.")
        exitProcess(0)
    }
}

fun validateImage(image: BufferedImage, imageType: String) {
    if (image.colorModel.numColorComponents != 3) {
        println("The number of $imageType color components isn't 3.")
        exitProcess(0)
    }
    if (!VALID_PIXEL_SIZE.contains(image.colorModel.pixelSize)) {
        println("The $imageType isn't 24 or 32-bit.")
        exitProcess(0)
    }
}

private fun validateImagesSizes(image: BufferedImage, watermark: BufferedImage) {
    if (image.width < watermark.width || image.height < watermark.height) {
        println("The watermark's dimensions are larger.")
        exitProcess(0)
    }
}
