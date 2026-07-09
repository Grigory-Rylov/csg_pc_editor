package com.github.grishberg.viewer

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.github.grishberg.cad3d.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BottomSheetTest {

    @get:Rule
    val activityRule = ActivityTestRule(Cad3dActivity::class.java, true, false)

    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private fun readBottomSheetState(): SheetInfo {
        val latch = java.util.concurrent.CountDownLatch(1)
        val info = SheetInfoHolder()

        Handler(Looper.getMainLooper()).post {
            val activity = activityRule.activity
            val sheet = activity.findViewById<View>(R.id.editor_bottom_sheet)
            val behavior = BottomSheetBehavior.from(sheet)

            val bounds = android.graphics.Rect()
            sheet.getGlobalVisibleRect(bounds)

            info.stateName = when (behavior.state) {
                BottomSheetBehavior.STATE_HIDDEN -> "HIDDEN"
                BottomSheetBehavior.STATE_COLLAPSED -> "COLLAPSED"
                BottomSheetBehavior.STATE_EXPANDED -> "EXPANDED"
                BottomSheetBehavior.STATE_DRAGGING -> "DRAGGING"
                BottomSheetBehavior.STATE_SETTLING -> "SETTLING"
                else -> "UNK(${behavior.state})"
            }
            info.sheetHeight = sheet.height
            info.visibleHeight = bounds.height()
            info.yTranslation = sheet.translationY
            info.sheetTop = sheet.top
            info.sheetBottom = sheet.bottom
            info.decorBottom = activity.window.decorView.height
            info.screenTop = bounds.top
            info.screenBottom = bounds.bottom
            info.peekHeight = behavior.peekHeight
            latch.countDown()
        }

        latch.await(10, java.util.concurrent.TimeUnit.SECONDS)
        return SheetInfo(
            info.stateName, info.visibleHeight, info.sheetHeight,
            info.yTranslation, info.sheetTop, info.sheetBottom, info.decorBottom,
            info.screenTop, info.screenBottom, info.peekHeight
        )
    }

    private fun isOnScreen(info: SheetInfo): Boolean {
        return info.visibleHeight > 10
    }

    @Test
    fun bottomSheet_shouldBeVisibleOnStartup() {
        activityRule.launchActivity(null)
        Thread.sleep(5000)

        val info = readBottomSheetState()
        val debug = String.format(
            "State=%s translationY=%.0f visibleHeight=%d peekHeight=%d screenTop=%d screenBottom=%d",
            info.stateName, info.yTranslation, info.visibleHeight, info.peekHeight,
            info.screenTop, info.screenBottom
        )
        android.util.Log.d("BottomSheetTest", "STARTUP: $debug")
        assertTrue(
            "Bottom sheet MUST be visible on screen at startup! $debug",
            isOnScreen(info)
        )
    }

    @Test
    fun bottomSheet_shouldExpandAndRemainVisibleAfterSwipeUp() {
        activityRule.launchActivity(null)
        Thread.sleep(5000)

        val initialInfo = readBottomSheetState()
        assertTrue(
            "Bottom sheet should be visible before swipe, visibleHeight=${initialInfo.visibleHeight}",
            isOnScreen(initialInfo)
        )

        // Get visible screen coords of the bottom sheet
        val sheetView = activityRule.activity.findViewById<View>(R.id.editor_bottom_sheet)
        val bounds = android.graphics.Rect()
        sheetView.getGlobalVisibleRect(bounds)

        android.util.Log.d("BottomSheetTest", "Display: ${uiDevice.displayWidth}x${uiDevice.displayHeight}, sheet visible: $bounds")

        // Simulate swipe up on collapsed bottom sheet by programmatically expanding it
        Handler(Looper.getMainLooper()).syncPost {
            val sheet = activityRule.activity.findViewById<View>(R.id.editor_bottom_sheet)
            val beet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        Thread.sleep(2000)

        val afterSwipe = readBottomSheetState()
        val debug = String.format(
            "State=%s translationY=%.0f visibleHeight=%d peekHeight=%d screenTop=%d screenBottom=%d onScreen=%b",
            afterSwipe.stateName, afterSwipe.yTranslation, afterSwipe.visibleHeight,
            afterSwipe.peekHeight, afterSwipe.screenTop, afterSwipe.screenBottom,
            isOnScreen(afterSwipe)
        )
        android.util.Log.d("BottomSheetTest", "AFTER swipe: $debug")
        assertTrue(
            "Bottom sheet disappeared after swipe up! $debug",
            isOnScreen(afterSwipe)
        )
    }
}

private data class SheetInfo(
    val stateName: String,
    val visibleHeight: Int,
    val sheetHeight: Int,
    val yTranslation: Float,
    val sheetTop: Int,
    val sheetBottom: Int,
    val decorBottom: Int,
    val screenTop: Int = 0,
    val screenBottom: Int = 0,
    val peekHeight: Int = 0
)

private class SheetInfoHolder {
    var stateName: String = ""
    var sheetHeight: Int = 0
    var visibleHeight: Int = 0
    var yTranslation: Float = 0f
    var sheetTop: Int = 0
    var sheetBottom: Int = 0
    var decorBottom: Int = 0
    var screenTop: Int = 0
    var screenBottom: Int = 0
    var peekHeight: Int = 0
}
