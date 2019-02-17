package de.lulebe.inventory

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import de.lulebe.inventory.ui.*

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FragmentSwitcher {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp)
    }

    override fun onStart() {
        super.onStart()
        if (supportFragmentManager.findFragmentByTag("main") == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentcontainer, BoxListFragment(), "main")
            transaction.commit()
        }
    }

    override fun goToFragment(fragment: Fragment, bundle: Bundle?) {
        val oldFragment = supportFragmentManager.findFragmentById(R.id.fragmentcontainer)
        val transaction = supportFragmentManager.beginTransaction()
        val f = when (fragment) {
            Fragment.BOXLIST -> BoxListFragment()
            Fragment.BOX -> BoxFragment()
        }
        f.publicTransitionEndListener = {
            oldFragment?.let {
                supportFragmentManager.beginTransaction().remove(it).commitNow()
            }
            f.publicTransitionEndListener = null
        }
        if (bundle != null && f is DataReceiver)
            f.setData(bundle)
        transaction.setCustomAnimations(
            R.animator.fragment_scalein_up,
            R.animator.hold,
            0,
            R.animator.fragment_slideout_right)
        transaction.add(R.id.fragmentcontainer, f, "main")
        transaction.commit()
    }

    override fun showUpBtn(visible: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(visible)
    }

    override fun goUp() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, R.animator.fragment_slideout_right)
            .replace(R.id.fragmentcontainer, BoxListFragment(), "main")
            .commit()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { goUp(); true}
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentcontainer) is BoxListFragment)
            super.onBackPressed()
        else
            goUp()
    }

}
