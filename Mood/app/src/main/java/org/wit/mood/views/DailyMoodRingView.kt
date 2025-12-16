package org.wit.mood.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import org.wit.mood.R
import org.wit.mood.models.MoodType
import kotlin.math.min

/**
 * Custom view to display a circular mood ring for a day.
 *
 * Each segment represents a MoodType proportionally to its count.
 * The center text displays the average mood or a placeholder if no data.
 */
class DailyMoodRingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // Paint for drawing mood segments
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    // Background track paint (light gray with transparency)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.LTGRAY
        alpha = 60
    }

    // Paint for center text (average mood label)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private var counts: Map<MoodType, Int> = emptyMap()  // Mood counts for the day
    private var averageLabel: String = ""               // Average mood label to display
    private var total = 0                               // Total number of moods

    // Color mapping for each MoodType, using the app's palette
    private val moodColors by lazy {
        mapOf(
            MoodType.HAPPY   to ContextCompat.getColor(context, R.color.mood_happy),
            MoodType.RELAXED to ContextCompat.getColor(context, R.color.mood_relaxed),
            MoodType.NEUTRAL to ContextCompat.getColor(context, R.color.mood_neutral),
            MoodType.SAD     to ContextCompat.getColor(context, R.color.mood_sad),
            MoodType.ANGRY   to ContextCompat.getColor(context, R.color.mood_angry)
        )
    }

    /**
     * Set data for the ring and trigger redraw.
     *
     * @param counts Map of MoodType to count for the day
     * @param averageLabel Label representing the average mood (e.g., "Happy 😊")
     */
    fun setData(counts: Map<MoodType, Int>, averageLabel: String) {
        this.counts = counts
        this.total = counts.values.sum()
        this.averageLabel = averageLabel
        invalidate() // redraw the view
    }

    /**
     * Draw the circular mood ring and center label.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Determine size and stroke width
        val size = min(width, height).toFloat()
        val stroke = size * 0.12f
        paint.strokeWidth = stroke
        bgPaint.strokeWidth = stroke

        // Padding inside the view to avoid clipping
        val pad = stroke / 2f + size * 0.04f
        val rect = RectF(pad, pad, size - pad, size - pad)

        // Draw background circle (full ring)
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // Draw each mood segment proportionally
        if (total > 0) {
            var start = -90f // start at top
            counts.entries
                .sortedBy { it.key.ordinal } // stable order for consistent segment positions
                .forEach { (mood, count) ->
                    val sweep = 360f * (count.toFloat() / total.toFloat()) // segment angle
                    paint.color = moodColors[mood] ?: Color.DKGRAY
                    canvas.drawArc(rect, start, sweep, false, paint)
                    start += sweep
                }
        }

        // Draw center text (average mood label)
        textPaint.textSize = size * 0.09f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        val label = if (total == 0) "No entries" else averageLabel
        canvas.drawText(label, width / 2f, y, textPaint)
    }
}
