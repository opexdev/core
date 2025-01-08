import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import org.jfree.graphics2d.svg.SVGGraphics2D
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDateTime

fun createLineChart(prices: List<BigDecimal>, times: List<LocalDateTime>): ByteArrayInputStream {
    val series = XYSeries("Price").apply {
        prices.zip(times).forEach { (price, time) ->
            add(time.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), price.toDouble())
        }
    }
    val dataset = XYSeriesCollection().apply {
        addSeries(series)
    }
    val chart = ChartFactory.createXYLineChart(
        null, // Chart title
        null, // X-axis label
        null, // Y-axis label
        dataset, // Data
        PlotOrientation.VERTICAL,
        false,
        false,
        false
    )
    val plot: XYPlot = chart.xyPlot
    val renderer = XYLineAndShapeRenderer(true, false)
    // Set chart color
    renderer.setSeriesPaint(
        0, when {
            prices.first() > prices.last() -> java.awt.Color.RED
            prices.first() < prices.last() -> java.awt.Color.GREEN
            else -> java.awt.Color.WHITE
        }
    )
    // Set axis ranges to keep the chart logical
    plot.rangeAxis.range = org.jfree.data.Range(
        prices.minOrNull()?.toDouble()?.times(0.8) ?: 0.0,
        prices.maxOrNull()?.toDouble()?.times(1.2) ?: 0.0
    )
    // Remove gridlines, axis, and background
    plot.renderer = renderer
    plot.isDomainGridlinesVisible = false
    plot.isRangeGridlinesVisible = false
    plot.backgroundPaint = null
    plot.domainAxis.isVisible = false
    plot.rangeAxis.isVisible = false
    plot.isOutlineVisible = false
    chart.backgroundPaint = null

    return chartToSvgInputStream(chart, 100, 35)
}

fun chartToSvgInputStream(chart: JFreeChart, width: Int, height: Int): ByteArrayInputStream {
    val svg = SVGGraphics2D(width, height)
    chart.draw(svg, java.awt.Rectangle(width, height))

    val outputStream = ByteArrayOutputStream()
    outputStream.write(svg.svgDocument.toByteArray())
    return ByteArrayInputStream(outputStream.toByteArray())
}