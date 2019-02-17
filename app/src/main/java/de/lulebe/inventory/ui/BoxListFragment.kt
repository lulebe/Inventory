package de.lulebe.inventory.ui

import android.arch.lifecycle.LiveData
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import de.lulebe.inventory.R
import de.lulebe.inventory.data.Box
import de.lulebe.inventory.data.BoxWithContent
import de.lulebe.inventory.data.DB
import de.lulebe.inventory.views.DowndrawerBackground
import kotlinx.android.synthetic.main.fragment_boxlist.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.math.sqrt


class BoxListFragment : BaseFragment() {

    private var dragOriginalX = 0F
    private var dragOriginalY = 0F
    private var dragOriginalTranslation = 0F
    private var addBoxIsExtended = false
    private var isAnimating = false

    private var liveBoxes: LiveData<List<BoxWithContent>>? = null
    private val boxAdapter = BoxAdapter(object: DiffUtil.ItemCallback<BoxWithContent>() {
        override fun areItemsTheSame(i1: BoxWithContent, i2: BoxWithContent): Boolean {
            return i1.id == i2.id
        }

        override fun areContentsTheSame(i1: BoxWithContent, i2: BoxWithContent): Boolean {
            return i1.id == i2.id && i1.name == i2.name
        }
    }, {
        val data = Bundle()
        data.putString("boxId", it.id.toString())
        (activity as FragmentSwitcher).goToFragment(de.lulebe.inventory.ui.Fragment.BOX, data)
    }, {
        editBox(it)
    }, {
        deleteBox(it)
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_boxlist, container, false)
        setupViews(rootView)
        return rootView
    }

    override fun onStart() {
        super.onStart()
        context?.let { c ->
            liveBoxes = DB.getInstance(c).boxDao().getAllBoxesWithContentLive()
            liveBoxes!!.observe(this, android.arch.lifecycle.Observer {
                it?.let { l ->
                    if (l.isEmpty()) {
                        rv_boxes.visibility = View.GONE
                        tv_emptyinfo.visibility = View.VISIBLE
                    } else {
                        rv_boxes.visibility = View.VISIBLE
                        tv_emptyinfo.visibility = View.GONE
                    }
                    boxAdapter.submitList(l)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as FragmentSwitcher).showUpBtn(false)
        activity?.title = "Your Inventory"
    }

    private fun setupViews (rootView: View) {
        val dropdownHandle = rootView.findViewById<View>(R.id.dropdown_handle)
        dropdownHandle.setOnClickListener {
            animateToFinalPosition(true)
        }
        val lAddbox = rootView.findViewById<View>(R.id.l_addbox)
        lAddbox.background = DowndrawerBackground(activity!!.applicationContext)
        val vDropdownCover = rootView.findViewById<View>(R.id.v_dropdown_cover)
        vDropdownCover.setOnClickListener {
            if (!isAnimating) {
                dragOriginalTranslation = lAddbox.translationY
                animateToFinalPosition(true)
            }
        }
        val translationExtendedPosition = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60F, resources.displayMetrics)
        dragOriginalTranslation = translationExtendedPosition
        val fullTranslationDistance = resources.getDimension(R.dimen.addbox_translation)
        dropdownHandle.setOnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragOriginalX = ev.rawX
                    dragOriginalY = ev.rawY
                    dragOriginalTranslation = lAddbox.translationY
                    vDropdownCover.visibility = View.VISIBLE
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val tapped = sqrt(Math.pow(ev.rawX - dragOriginalX.toDouble(), 2.0) + Math.pow(ev.rawY - dragOriginalY.toDouble(), 2.0)) < 10.0
                    if (!tapped)
                        animateToFinalPosition(false)
                    else
                        if (addBoxIsExtended)
                            addBox()
                        else
                            animateToFinalPosition(true)
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    animateToFinalPosition(false)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val distance = ev.rawY - dragOriginalY
                    var translation = dragOriginalTranslation + distance
                    if (translation > -translationExtendedPosition) {
                        val overDrag = distance + dragOriginalTranslation + translationExtendedPosition
                        val overDragFactor = 3 + (overDrag / translationExtendedPosition)
                        translation = translation - overDrag + (overDrag / overDragFactor)
                        if (translation > 0F)
                            translation = 0F
                    }
                    if (translation < fullTranslationDistance)
                        translation = fullTranslationDistance
                    lAddbox.translationY = translation
                    vDropdownCover.alpha = 1 - translation / fullTranslationDistance
                    true
                }
                else -> false
            }
        }
        val rvBoxes = rootView.findViewById<RecyclerView>(R.id.rv_boxes)
        rvBoxes.layoutManager = LinearLayoutManager(context!!)
        rvBoxes.adapter = boxAdapter
    }

    private fun animateToFinalPosition(toggle: Boolean) {
        isAnimating = true
        val fullTranslationDistance = - resources.getDimension(R.dimen.addbox_translation)
        val translationExtendedPosition = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60F, resources.displayMetrics)
        var translateTo = -translationExtendedPosition
        val interpolator = DecelerateInterpolator()
        var duration = 90L
        var endingOnClosed = false
        if (
            (dragOriginalTranslation == -translationExtendedPosition && (l_addbox.translationY < (-fullTranslationDistance / 3))) ||
            (!toggle && dragOriginalTranslation != -translationExtendedPosition && (l_addbox.translationY < (-fullTranslationDistance / 1.3)))
        ) {
            translateTo = -fullTranslationDistance
            endingOnClosed = true
        }
        if (toggle) {
            duration = 150L
            if (dragOriginalTranslation == -translationExtendedPosition) {
                translateTo = -fullTranslationDistance
                endingOnClosed = true
            } else {
                translateTo = -translationExtendedPosition
                endingOnClosed = false
            }
        }
        l_addbox.animate()
            .translationY(translateTo)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .start()
        v_dropdown_cover.animate()
            .alpha(if (endingOnClosed) 0F else (fullTranslationDistance-translationExtendedPosition)/fullTranslationDistance)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .withEndAction {
                if (v_dropdown_cover.alpha == 0F)
                    v_dropdown_cover.visibility = View.GONE
                isAnimating = false
            }
            .start()
        addBoxIsExtended = !endingOnClosed
        iv_btn_addbox.setImageResource(if (endingOnClosed) R.drawable.ic_add_black_24dp else R.drawable.ic_save_black_24dp)
    }

    private fun addBox() {
        val newName = et_addbox_name.text.toString()
        if (newName.isBlank()) {
            Toast.makeText(context!!, "You need to enter a name for your Box.", Toast.LENGTH_SHORT).show()
        } else {
            val newBox = Box(
                UUID.randomUUID(),
                newName
            )
            et_addbox_name.text.clear()
            doAsync {
                context?.let { c ->
                    DB.getInstance(c).boxDao().insertBox(newBox)
                    uiThread {
                        animateToFinalPosition(true)
                    }
                }
            }
        }
    }

    private fun deleteBox(box: BoxWithContent) {

    }

    private fun editBox(box: BoxWithContent) {

    }
}
