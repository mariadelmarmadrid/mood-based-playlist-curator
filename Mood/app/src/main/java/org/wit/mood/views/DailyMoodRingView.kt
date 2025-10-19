package org.wit.mood.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import org.wit.mood.R
import org.wit.mood.models.MoodType
import kotlin.math.min

class DailyMoodRingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.LTGRAY; alpha = 60
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK; textAlign = Paint.Align.CENTER
    }

    private var counts: Map<MoodType, Int> = emptyMap()
    private var averageLabel: String = ""
    private var total = 0

    // Colors per mood (uses your palette from colors.xml)
    private val moodColors by lazy {
        mapOf(
            MoodType.HAPPY   to ContextCompat.getColor(context, R.color.mood_happy),
            MoodType.RELAXED to ContextCompat.getColor(context, R.color.mood_relaxed),
            MoodType.NEUTRAL to ContextCompat.getColor(context, R.color.mood_neutral),
            MoodType.SAD     to ContextCompat.getColor(context, R.color.mood_sad),
            MoodType.ANGRY   to ContextCompat.getColor(context, R.color.mood_angry)
        )
    }

    fun setData(counts: Map<MoodType, Int>, averageLabel: String) {
        this.counts = counts
        this.total = counts.values.sum()
        this.averageLabel = averageLabel
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val stroke = size * 0.12f
        paint.strokeWidth = stroke
        bgPaint.strokeWidth = stroke

        val pad = stroke / 2f + size * 0.04f
        val rect = RectF(pad, pad, size - pad, size - pad)

        // Background track
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // Segments
        if (total > 0) {
            var start = -90f
            counts.entries
                .sortedBy { it.key.ordinal } // stable order
                .forEach { (mood, count) ->
                    val sweep = 360f * (count.toFloat() / total.toFloat())
                    paint.color = moodColors[mood] ?: Color.DKGRAY
                    canvas.drawArc(rect, start, sweep, false, paint)
                    start += sweep
                }
        }

        // Center text (average mood)
        textPaint.textSize = size * 0.09f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        val label = if (total == 0) "No entries" else averageLabel
        canvas.drawText(label, width / 2f, y, textPaint)
    }
}
