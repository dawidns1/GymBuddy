package com.example.gymbuddy.helpers

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.gymbuddy.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import kotlin.math.round

object Helpers {
    const val CHANNEL_ID = "id"
    const val NEW_WORKOUT_KEY = "new workout key"
    const val EXERCISES_KEY = "exercises"
    const val WORKOUTS_KEY = "edit workout key"
    const val POSITION_KEY = "position"
    const val WORKOUT_KEY = "workout"
    const val NEW_EXERCISE_KEY = "new exercise key"
    const val EDITED_EXERCISE_KEY = "edited exercise key"
    const val NEW_SESSION_KEY = "new session key"
    const val RESUMED_KEY = "resumed"
    const val CHART_KEY = "chart"
    const val RIR_KEY = "rir"
    const val REWARD_GRANTED_KEY = "reward granted"
    const val FOREGROUND_NOTIFICATION = 0
    const val AD_ID_MAIN_NATIVE = "ca-app-pub-3836143618707347/3998950331"
    const val AD_ID_NEW_SESSION_NATIVE = "ca-app-pub-3836143618707347/8449401049"
    const val AD_ID_EXERCISES_NATIVE = "ca-app-pub-3836143618707347/8500946906"
    const val AD_ID_CHART_NATIVE = "ca-app-pub-3836143618707347/9293980365"
    const val AD_ID_SCHEDULE_NATIVE = "ca-app-pub-3836143618707347/3192779903"
    const val AD_ID_SESSIONS_NATIVE = "ca-app-pub-3836143618707347/6713222519"
    const val AD_ID_REWARDED = "ca-app-pub-3836143618707347/7535495699"
    const val PICK_FILE = 52
    private var savedAdRequest: AdRequest? = null
    const val MODE_SAVE_TO_FILE = "saveToFile"
    const val MODE_SAVE_TO_CLOUD = "saveToCloud"
    const val MODE_SHARE = "share"
    const val MODE_ADD = "add"
    const val MODE_DELETE = "delete"
    const val WORKOUT_ID_KEY = "id"
    const val WORKOUT_NAME_KEY = "name"
    const val WORKOUT_EXERCISE_NUMBER_KEY = "exerciseNumber"
    const val WORKOUT_TYPE_KEY = "type"
    const val WORKOUT_MUSCLE_GROUP_KEY = "muscleGroup"
    const val WORKOUT_MUSCLE_GROUP_SECONDARY_KEY = "muscleGroupSecondary"
    const val WORKOUT_TIMESTAMP_KEY = "timestamp"
    const val WORKOUT_CLOUD_ID_KEY = "cloudId"
    const val EXERCISE_ID_KEY = "exercise id"
    const val EXERCISE_NAME_KEY = "exercise name"
    const val EXERCISE_SETS_KEY = "exercise sets"
    const val EXERCISE_BREAKS_KEY = "exercise breaks"
    const val EXERCISE_TEMPO_KEY = "exercise tempo"
    const val EXERCISE_MUSCLE_GROUP_KEY = "exercise muscleGroup"
    const val EXERCISE_MUSCLE_GROUP_SECONDARY_KEY = "exercise muscleGroupSecondary"
    const val EXERCISE_SUPERSET_KEY = "exercise superSet"
    const val EXERCISE_TIMESTAMP_KEY = "exercise timestamp"
    const val SESSION_ID_KEY = "session id"
    const val SESSION_LOAD_KEY = "session load"
    const val SESSION_REPS_KEY = "session reps"
    const val SESSION_DATE_KEY = "session date"
    const val SESSION_RIR_KEY = "session rir"
    const val REQUEST_CALENDAR = 2
    val PERMISSIONS_CALENDAR = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )
    const val MAIN_ACTIVITY_TAG = "MainActivity"
    var rewardGrantedThisSession = false

    @JvmStatic
    fun hideProgressBar(progressBarParent: CardView, layout: ConstraintLayout) {
        if (progressBarParent.isShown) {
            progressBarParent.visibility = View.GONE
            val no = layout.childCount
            var view: View
            for (i in 0 until no) {
                view = layout.getChildAt(i)
                view.alpha = 1.0f
            }
        }
    }

    fun rirSuffix(rir: IntArray?, i: Int, context: Context): String {
        if (Utils.getInstance(context).rirEnabled) {
            rir?.let {
                return if (rir[i] == 6) {
                    "|-"
                } else {
                    "|${rir[i]}"
                }
            } ?: run {
                return "|-"
            }
        } else {
            return ""
        }
    }

    @JvmStatic
    fun showProgressBar(progressBarParent: CardView, layout: ConstraintLayout) {
        if (!progressBarParent.isShown) {
            progressBarParent.visibility = View.VISIBLE
            val no = layout.childCount
            var view: View
            for (i in 0 until no) {
                view = layout.getChildAt(i)
                view.alpha = 0.5f
            }
        }
    }

    @JvmStatic
    fun workoutTypeComparator(type1: String, type2: String): Boolean {
        return if ((type1 == "Push" || type1 == "Pull" || type1 == "Legs") &&
            (type2 == "Push" || type2 == "Pull" || type2 == "Legs")
        ) {
            true
        } else if ((type1 == "Upper" || type1 == "Lower") &&
            (type2 == "Upper" || type2 == "Lower")
        ) {
            true
        } else type1 == type2
    }

    @JvmStatic
    fun workoutTypeHeaderGenerator(type: String, context: Context): String {
        return if (type == "Push" || type == "Pull" || type == "Legs") {
            "Push/Pull/Legs"
        } else if (type == "Upper" || type == "Lower") {
            "Upper/Lower"
        } else if (type == "---") {
            context.resources.getString(R.string.other)
        } else type
    }

    @JvmStatic
    fun showRatingUserInterface(activity: Activity) {
        val lastAppRating = Utils.getInstance(activity.applicationContext).lastAppRating
        var days = 0
        if (lastAppRating != 0L) {
            days = millisToDays(System.currentTimeMillis() - lastAppRating)
        }
        if (days > 14) {
            Utils.getInstance(activity.applicationContext).lastAppRating = System.currentTimeMillis()
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task: Task<ReviewInfo?> ->
                try {
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        manager.launchReviewFlow(activity, reviewInfo)
                    }
                } catch (ex: Exception) {
                }
            }
        }
    }

    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @JvmStatic
    fun showKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    @JvmStatic
    fun setupActionBar(text1: String?, text2: String?, actionBar: ActionBar?, activity: Activity): ImageView? {
        if (actionBar != null) {
            actionBar.apply {
                setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(activity.applicationContext, R.color.grey_900)))
                setDisplayShowTitleEnabled(false)
                setDisplayUseLogoEnabled(false)
                setDisplayHomeAsUpEnabled(false)
                setDisplayShowCustomEnabled(true)
                setDisplayShowHomeEnabled(false)
            }
            val params = ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
            val customActionBar = LayoutInflater.from(activity.applicationContext).inflate(R.layout.action_bar, null)
            actionBar.setCustomView(customActionBar, params)
            val imgSuperset = activity.findViewById<ImageView>(R.id.imgSupersetBar)
            val abText1 = activity.findViewById<TextView>(R.id.abText1)
            abText1.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.orange_500))
            val abText2 = activity.findViewById<TextView>(R.id.abText2)
            abText2.setTextColor(ContextCompat.getColor(activity.applicationContext, R.color.orange_500))
            abText1.text = text1
            abText2.text = text2
            return imgSuperset
        }
        return null
    }

    @JvmStatic
    fun shake(v: View?) {
        ObjectAnimator
            .ofFloat(v, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
            .setDuration(200)
            .start()
    }

    @JvmStatic
    fun shakeVertically(v: View?, duration: Int) {
        ObjectAnimator
            .ofFloat(v, "translationY", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
            .setDuration(duration.toLong())
            .start()
    }

    @JvmStatic
    fun enableEFABClickable(efab: ExtendedFloatingActionButton, context: Context?) {
        efab.isClickable = true
        efab.setTextColor(ContextCompat.getColor(context!!, R.color.white))
        val colorInt = ContextCompat.getColor(context, R.color.orange_500)
        val csl = ColorStateList.valueOf(colorInt)
        efab.strokeColor = csl
        val colorIntB = ContextCompat.getColor(context, R.color.grey_900)
        val cslB = ColorStateList.valueOf(colorIntB)
        efab.backgroundTintList = cslB
    }

    @JvmStatic
    fun disableEFABClickable(efab: ExtendedFloatingActionButton, context: Context?) {
        efab.isClickable = false
        efab.setTextColor(ContextCompat.getColor(context!!, R.color.grey_500))
        val colorInt = ContextCompat.getColor(context, R.color.grey_500)
        val csl = ColorStateList.valueOf(colorInt)
        efab.strokeColor = csl
        val colorIntB = ContextCompat.getColor(context, R.color.grey_700_alpha)
        val cslB = ColorStateList.valueOf(colorIntB)
        efab.backgroundTintList = cslB
    }

    fun Float.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    fun stringDateToMillis(date: String): Float {
        val values = date.split("-".toRegex()).toTypedArray()
        val year = values[0].toInt()
        val month = values[1].toInt() - 1
        val day = values[2].toInt()
        val time = Calendar.getInstance()
        time[year, month] = day
        return time.timeInMillis.toFloat()
    }

    fun arrayToTotal(reps: IntArray, load: FloatArray): Float {
        var total = 0f
        for (i in reps.indices) {
            total += reps[i] * load[i]
        }
        return total
    }

    fun handleNativeAds(template: TemplateView, activity: Activity, adUnitId: String, adLoader: AdLoader?, rewardGranted: Boolean = rewardGrantedThisSession): AdLoader? {
//        val adUnitId = "ca-app-pub-3940256099942544/2247696110";
        if (!rewardGranted) {
            var currentAdLoader = adLoader
            if (currentAdLoader == null) {
                currentAdLoader = AdLoader.Builder(activity.applicationContext, adUnitId)
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            template.visibility = View.GONE
                            super.onAdFailedToLoad(loadAdError)
                        }

                        override fun onAdLoaded() {
                            template.mockLayout?.visibility = View.GONE
                            template.realLayout?.visibility = View.VISIBLE
                            super.onAdLoaded()
                        }
                    })
                    .forNativeAd { nativeAd: NativeAd ->
                        val styles = NativeTemplateStyle.Builder().withMainBackgroundColor(ColorDrawable(ContextCompat.getColor(activity.applicationContext, R.color.grey_900))).build()
                        template.setStyles(styles)
                        template.setNativeAd(nativeAd)
                        if (activity.isDestroyed) {
                            nativeAd.destroy()
                        }
                    }
                    .build()
            }
            val lastAdShown = Utils.getInstance(activity.applicationContext).lastAdShown
            val currentTime = System.currentTimeMillis()
            if (lastAdShown == 0L || (currentTime - lastAdShown) / 1000 > 60 || savedAdRequest == null) {
                savedAdRequest = AdRequest.Builder().build()
                Utils.getInstance(activity.applicationContext).lastAdShown = currentTime
                //            Toast.makeText(activity, "new Ad", Toast.LENGTH_SHORT).show();
            }
            currentAdLoader?.loadAd(savedAdRequest!!)
            return currentAdLoader
        } else {
            template.visibility = View.GONE
            return null
        }
    }

    private fun millisToDays(millis: Long): Int {
        return (millis / 1000 / 60 / 60 / 24).toInt()
    }

    fun Float.toPrettyString() =
        if (this.toDouble() - this.toLong() == 0.0)
            String.format("%d", this.toLong())
        else
            String.format("%s", this)

    fun Double.toPrettyString() =
        if (this - this.toLong() == 0.0)
            String.format("%d", this.toLong())
        else
            String.format("%s", this)

    fun View.toggleEnabled(boolean: Boolean) {
        if (boolean) {
            if (!this.isEnabled) this.isEnabled = true
        } else {
            if (this.isEnabled) this.isEnabled = false
        }
    }

    fun View.toggleEnabled() {
        this.isEnabled = !this.isEnabled
    }

    fun View.toggleVisibility(boolean: Boolean) {
        if (boolean) {
            if (this.visibility == View.GONE) this.visibility = View.VISIBLE
        } else
            if (this.visibility == View.VISIBLE) this.visibility = View.GONE
    }

    fun ExtendedFloatingActionButton.toggleVisibilityEFAB(boolean: Boolean) {
        if (boolean) {
            if (!this.isShown) this.show()
        } else
            if (this.isShown) this.hide()
    }

    fun View.toggleVisibility() {
        this.visibility = if (this.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    fun TextView.toggleMarque(boolean: Boolean) {
        if (boolean) {
            setHorizontallyScrolling(true)
            ellipsize = TextUtils.TruncateAt.MARQUEE
            isHorizontalFadingEdgeEnabled = true
            marqueeRepeatLimit = -1
            setMarqueeSpeed(0.01F)
            isSelected = true

        } else {
            ellipsize = TextUtils.TruncateAt.END
            isSelected = false
        }
    }

    fun NumberPicker.setup(min: Int = 0, max: Int = 6, formatter: (Int) -> String = { i -> if (i == 6) "-" else i.toString() }) {
        maxValue = max
        minValue = min
        wrapSelectorWheel = false
        value = 3
        scaleY = 1f
        scaleX = 1f
        setFormatter(formatter)
    }

    private fun TextView.setMarqueeSpeed(speed: Float, speedIsMultiplier: Boolean = true) {
        try {
            val f = this.javaClass.getDeclaredField("mMarquee")
            f.isAccessible = true
            val marquee = f.get(this)
            marquee?.let { it ->
                val scrollSpeedFieldName = "mPixelsPerSecond"
                val mf = it.javaClass.getDeclaredField(scrollSpeedFieldName)
                mf.isAccessible = true
                var newSpeed = speed
                if (speedIsMultiplier) newSpeed = mf.getFloat(it) * speed
                mf.setFloat(it, newSpeed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ExtendedFloatingActionButton.toggleEnabled(context: Context, boolean: Boolean) {
        this.isClickable = boolean
        this.setTextColor(
            when (boolean) {
                true -> ContextCompat.getColor(context, R.color.white)
                false -> ContextCompat.getColor(context, R.color.grey_500)
            }
        )
        val strokeColor = when (boolean) {
            true -> ContextCompat.getColor(context, R.color.orange_500)
            false -> ContextCompat.getColor(context, R.color.grey_500)
        }
        val strokeColorStateList = ColorStateList.valueOf(strokeColor)
        this.strokeColor = strokeColorStateList
        val backgroundTint = when (boolean) {
            true -> ContextCompat.getColor(context, R.color.grey_900)
            false -> ContextCompat.getColor(context, R.color.grey_700_alpha)
        }
        val backgroundTintListStateColor = ColorStateList.valueOf(backgroundTint)
        this.backgroundTintList = backgroundTintListStateColor
    }

    fun isRewardGranted(rewardGranted: Long): Boolean {
        rewardGrantedThisSession = !(rewardGranted == 0L || millisToDays(System.currentTimeMillis() - rewardGranted) > 7)
        return rewardGrantedThisSession

    }
}