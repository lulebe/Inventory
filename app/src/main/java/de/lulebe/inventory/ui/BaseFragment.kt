package de.lulebe.inventory.ui

import android.animation.Animator
import android.animation.AnimatorInflater
import android.support.v4.app.Fragment

open class BaseFragment : Fragment() {

    protected var transitionEndListener: (() -> Unit)? = null
    var publicTransitionEndListener: (() -> Unit)? = null

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        if (nextAnim == 0x0) return null
        val animator = AnimatorInflater.loadAnimator(activity, nextAnim)
        animator.addListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                transitionEndListener?.invoke()
                publicTransitionEndListener?.invoke()
            }

        })
        return animator
    }

}