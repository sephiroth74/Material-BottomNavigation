package it.sephiroth.android.library.bottomnavigation.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import it.sephiroth.android.library.bottomnavigation.BottomNavigation
import it.sephiroth.android.library.bottomnavigation.setListener
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)

        ResourcesCompat.getFont(this, R.font.nunito_sans_bold)?.also {
            bottomNavigation.setDefaultTypeface(it)
        }

        bottomNavigation.setListener {
            onMenuItemSelect { itemId, position, fromUser ->
                Timber.v("onMenuItemSelect($itemId, $position, $fromUser)")

                title = bottomNavigation.getMenuItemTitle(position)

                if(fromUser) {
                    viewPager.setCurrentItem(position, true)
                }
            }

            onMenuItemReselect { itemId, position, fromUser ->
                Timber.v("onMenuItemReselect($itemId, $position, $fromUser)")
            }
        }


        bottomNavigation.menuChangedListener = object : BottomNavigation.OnMenuChangedListener {
            override fun onMenuChanged(parent: BottomNavigation) {
                Timber.v("onMenuChanged()")
                title = bottomNavigation.getMenuItemTitle(bottomNavigation.selectedIndex)
                viewPager.adapter = ScreenSlidePagerAdapter(parent.menuItemCount, supportFragmentManager)
            }
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                Timber.i("onPageSelected($position)")
                bottomNavigation.setSelectedIndex(position, false)
            }

        })
    }

    private inner class ScreenSlidePagerAdapter(val numPages: Int, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = numPages

        override fun getItem(position: Int): Fragment = ScreenSlidePageFragment()
    }

}
