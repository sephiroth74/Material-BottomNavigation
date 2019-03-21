package it.sephiroth.android.library.bottomnavigation.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.sephiroth.android.library.bottomnavigation.BottomNavigation
import it.sephiroth.android.library.bottomnavigation.setListener
import kotlinx.android.synthetic.main.main_activity.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)

        bottomNavigation.setListener {
            onMenuItemSelect { itemId, position, fromUser ->
                Timber.v("onMenuItemSelect($itemId, $position, $fromUser)")

            }

            onMenuItemReselect { itemId, position, fromUser ->
                Timber.v("onMenuItemReselect($itemId, $position, $fromUser)")
            }
        }


        bottomNavigation.menuChangedListener = object : BottomNavigation.OnMenuChangedListener {
            override fun onMenuChanged(parent: BottomNavigation) {
                Timber.v("onMenuChanged()")
            }

        }
    }

}
