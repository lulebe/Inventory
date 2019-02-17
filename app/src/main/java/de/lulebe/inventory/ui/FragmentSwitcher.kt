package de.lulebe.inventory.ui

import android.os.Bundle

interface FragmentSwitcher {

    fun goToFragment(fragment: Fragment, bundle: Bundle?)

    fun showUpBtn(visible: Boolean)

    fun goUp()

}