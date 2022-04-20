package co.nilin.opex.captcha.app.service

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

object SimpleCaptcha {
    /**
     * Generates a random alpha-numeric string of eight characters.
     *
     * @return random alpha-numeric string of eight characters.
     */
    fun generateText(): String {
        return StringTokenizer(UUID.randomUUID().toString(), "-").nextToken()
    }

    /**
     * Generates a PNG image of text 180 pixels wide, 40 pixels high with white background.
     *
     * @param text expects string size eight (5) characters.
     * @return byte array that is a PNG image generated with text displayed.
     */
    fun generateImage(text: String): ByteArray {
        val w = 180
        val h = 40
        val image = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.color = Color.white
        g.fillRect(0, 0, w, h)
        g.font = Font("Serif", Font.PLAIN, 26)
        g.color = Color.blue
        val start = 10
        val bytes = text.toByteArray()
        val random = Random()
        for (i in bytes.indices) {
            g.color = Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))
            g.drawString(String(byteArrayOf(bytes[i])), start + i * 20, (Math.random() * 20 + 20).toInt())
        }
        g.color = Color.white
        for (i in 0..4) {
            g.drawOval((Math.random() * 160).toInt(), (Math.random() * 10).toInt(), 30, 30)
        }
        g.dispose()
        val bout = ByteArrayOutputStream()
        kotlin.runCatching { ImageIO.write(image, "png", bout) }.onFailure { throw RuntimeException(it) }
        return bout.toByteArray()
    }
}
