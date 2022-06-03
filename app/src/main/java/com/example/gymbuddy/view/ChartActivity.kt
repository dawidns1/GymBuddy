package com.example.gymbuddy.view

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gymbuddy.R
import com.example.gymbuddy.databinding.ActivityChartBinding
import com.example.gymbuddy.helpers.Helpers
import com.example.gymbuddy.helpers.Helpers.arrayToTotal
import com.example.gymbuddy.helpers.Helpers.handleNativeAds
import com.example.gymbuddy.helpers.Helpers.rirSuffix
import com.example.gymbuddy.helpers.Helpers.round
import com.example.gymbuddy.helpers.Helpers.setupActionBar
import com.example.gymbuddy.helpers.Helpers.stringDateToMillis
import com.example.gymbuddy.helpers.Helpers.toPrettyString
import com.example.gymbuddy.helpers.LineChartXAxisValueFormatter
import com.example.gymbuddy.model.Exercise
import com.example.gymbuddy.model.Session
import com.github.mikephil.charting.components.MarkerImage
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
class ChartActivity : AppCompatActivity() {

    private val txtLxRH = ArrayList<TextView>()
    private val txtLxRB = ArrayList<TextView>()
    private var position = 0
    private var bestTotal = 0f
    private var total = 0f
    private var bestSessionPosition = 0
    private var exercise: Exercise? = null
    private lateinit var sessions: ArrayList<Session>
    private val entries = ArrayList<Entry>()
    private var dataSet: ArrayList<ILineDataSet> = ArrayList()
    private var myData: LineData? = null
    private lateinit var binding: ActivityChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymBuddy)
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        exercise = intent.getSerializableExtra(Helpers.CHART_KEY) as Exercise?
        sessions = exercise?.sessions ?: ArrayList()
        for (i in sessions.indices) {
            total = arrayToTotal(sessions[i].reps, sessions[i].load)
            if (total > bestTotal) {
                bestTotal = total
                bestSessionPosition = i
            }
            entries.add(0, Entry(stringDateToMillis(sessions[i].date!!), total))
            entries[0].icon = ContextCompat.getDrawable(this, R.drawable.ic_hexagon_icon)
        }
        initViews()

        handleNativeAds(binding.chartAdTemplate, this, Helpers.AD_ID_CHART_NATIVE, null)
        binding.txtTotalProgress.text = (entries[entries.size - 1].y - entries[0].y).round(1).toPrettyString()
        binding.txtLastProgress.text = (entries[entries.size - 1].y - entries[entries.size - 2].y).round(1).toPrettyString()
        binding.txtTotalProgressPercentage.text = "${((entries[entries.size - 1].y - entries[0].y) / entries[0].y * 100).roundToInt()}%"
        binding.txtLastProgressPercentage.text = "${((entries[entries.size - 1].y - entries[entries.size - 2].y) / 
                entries[entries.size - 2].y * 100).roundToInt()}%"
        for (i in 0 until exercise!!.sets) {
            txtLxRB[i].text = "${sessions[bestSessionPosition].load[i].toPrettyString()}x${sessions[bestSessionPosition].reps[i]}${rirSuffix(sessions[bestSessionPosition].rir,i,this)}"
        }
        binding.txtDateB.text = sessions[bestSessionPosition].date
        binding.txtTotalB.text = entries[entries.size - 1 - bestSessionPosition].y.roundToInt().toString()
        setupActionBar(exercise!!.name + resources.getString(R.string.progress), "", supportActionBar, this)
        dataSet.add(newSet(entries))
        myData = LineData(dataSet)
        val markerImage = MarkerImage(this, R.drawable.ic_hexagon_marker)
        markerImage.setOffset(-13f, -13f)
        binding.exerciseChart.apply {
            data=myData
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(this@ChartActivity, R.color.white)
                axisLineWidth = 2f
                setDrawGridLines(false)
                valueFormatter = LineChartXAxisValueFormatter(0)
            }
            axisLeft.apply {
                setDrawGridLines(false)
                textColor = ContextCompat.getColor(this@ChartActivity, R.color.orange_500)
                textSize = 15f
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                axisLineWidth = 2f
            }
            marker = markerImage
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    for (i in entries.indices) {
                        if (e.equalTo(entries[i])) {
                            position = i
                            viewHighlighted(position)
                        }
                    }
                }
                override fun onNothingSelected() {}
            })
            highlightValue(entries.last().x, 0, true)
            onChartGestureListener = object : OnChartGestureListener {
                override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartGesture) {}
                override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartGesture) {}
                override fun onChartLongPressed(me: MotionEvent) {
                    if (binding.exerciseChart.viewPortHandler.scaleX != 1f) {
                        binding.exerciseChart.fitScreen()
                    }
                }
                override fun onChartDoubleTapped(me: MotionEvent) {}
                override fun onChartSingleTapped(me: MotionEvent) {}
                override fun onChartFling(me1: MotionEvent, me2: MotionEvent, velocityX: Float, velocityY: Float) {}
                override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {}
                override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {}
            }
        }
    }

    private fun newSet(yValues: ArrayList<Entry>): LineDataSet {
        return LineDataSet(yValues, "Dataset1").apply {
            highLightColor = ContextCompat.getColor(this@ChartActivity, R.color.orange_500)
            setDrawHorizontalHighlightIndicator(false)
            setDrawVerticalHighlightIndicator(false)
            highlightLineWidth = 0f
            fillAlpha = 110
            color = ContextCompat.getColor(this@ChartActivity, R.color.orange_500)
            circleRadius = 2f
            setCircleColor(ContextCompat.getColor(this@ChartActivity, R.color.orange_700))
            circleHoleColor = ContextCompat.getColor(this@ChartActivity, R.color.grey_500)
            circleHoleRadius = 1f
            setDrawValues(false)
            valueTextColor = ContextCompat.getColor(this@ChartActivity, R.color.grey_200)
            lineWidth = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }
    }

    private fun initViews() {
        txtLxRH.apply {
            add(binding.txtLxR1H)
            add(binding.txtLxR2H)
            add(binding.txtLxR3H)
            add(binding.txtLxR4H)
            add(binding.txtLxR5H)
            add(binding.txtLxR6H)
            add(binding.txtLxR7H)
            add(binding.txtLxR8H)
        }
        txtLxRB.apply {
            add(binding.txtLxR1B)
            add(binding.txtLxR2B)
            add(binding.txtLxR3B)
            add(binding.txtLxR4B)
            add(binding.txtLxR5B)
            add(binding.txtLxR6B)
            add(binding.txtLxR7B)
            add(binding.txtLxR8B)
        }
        binding.apply {
            if (exercise!!.sets < 4) {
                txtLxR4H.visibility = View.GONE
                separator3H.visibility = View.GONE
                txtLxR4B.visibility = View.GONE
                separator3B.visibility = View.GONE
            }
            if (exercise!!.sets < 5) {
                txtLxR5H.visibility = View.GONE
                txtLxR5B.visibility = View.GONE
            }
            if (exercise!!.sets < 6) {
                txtLxR6H.visibility = View.GONE
                separator4H.visibility = View.GONE
                txtLxR6B.visibility = View.GONE
                separator4B.visibility = View.GONE
            }
            if (exercise!!.sets < 7) {
                txtLxR7H.visibility = View.GONE
                separator5H.visibility = View.GONE
                txtLxR7B.visibility = View.GONE
                separator5B.visibility = View.GONE
            }
            if (exercise!!.sets < 8) {
                txtLxR8H.visibility = View.GONE
                separator6H.visibility = View.GONE
                txtLxR8B.visibility = View.GONE
                separator6B.visibility = View.GONE
            }
        }
    }

    private fun viewHighlighted(position: Int) {
        val session= sessions[sessions.size-1-position]
        for (i in 0 until exercise!!.sets) {
            txtLxRH[i].text = "${session.load[i].toPrettyString()}x${session.reps[i]}${rirSuffix(session.rir,i,this)}"
        }
        binding.txtDateH.text = session.date
        binding.txtTotalH.text = entries[position].y.toPrettyString()
    }
}