package com.example.helperapplication

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager : ViewPager2 = findViewById(R.id.viewPager)
        val cameraStreamButton: Button = findViewById(R.id.cameraStreamButton)
        val servicesButton: Button = findViewById(R.id.servicesButton)
        val settingsButton: Button = findViewById(R.id.settingsButton)
        val memoryUsageButton: Button = findViewById(R.id.memoryUsageButton)
        val fragments = listOf(FragmentServices(), FragmentSettings(), FragmentCameraStream(), FragmentMemoryUsage())
        val adapter = ViewPagerAdapter(this, fragments)
        val exitButton: Button = findViewById(R.id.exitButton)

        viewPager.adapter = adapter
        servicesButton.setOnClickListener {
            viewPager.currentItem = 0
        }
        settingsButton.setOnClickListener {
            viewPager.currentItem = 1
        }
        cameraStreamButton.setOnClickListener{
            viewPager.currentItem = 2
        }
        memoryUsageButton.setOnClickListener{
            viewPager.currentItem = 3
        }
        exitButton.setOnClickListener {
            finish()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view != null) {
                if (!isTouchInsideView(view, event)) {
                    hideKeyboard()
                    view.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun isTouchInsideView(view: View, event: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        return x >= location[0] && x <= location[0] + view.width &&
                y >= location[1] && y <= location[1] + view.height
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
        view?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}
class ViewPagerAdapter(fragmentActivity: FragmentActivity, private val fragments: List<Fragment>)
    : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}

