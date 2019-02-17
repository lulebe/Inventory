package de.lulebe.inventory.ui

import android.arch.lifecycle.LiveData
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.GridLayoutManager
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
import de.lulebe.inventory.data.BoxWithContent
import de.lulebe.inventory.data.DB
import de.lulebe.inventory.data.Item
import de.lulebe.inventory.views.DowndrawerBackground
import kotlinx.android.synthetic.main.fragment_box.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.math.sqrt

class BoxFragment : BaseFragment(), DataReceiver {

    init {
        transitionEndListener = {
            if (isVisible && view != null) {
                view!!.findViewById<View>(R.id.l_additem)?.animate()
                    ?.translationY(resources.getDimension(R.dimen.additem_translation))
                    ?.setDuration(300L)
                    ?.setInterpolator(DecelerateInterpolator())
                    ?.start()
            }
        }
    }

    private var dragOriginalX = 0F
    private var dragOriginalY = 0F
    private var dragOriginalTranslation = 0F
    private var addItemIsExtended = false
    private var isAnimating = false
    
    private var data: Bundle? = null
    private var boxId: UUID? = null

    private var liveBox: LiveData<BoxWithContent>? = null
    private var liveItems: LiveData<List<Item>>? = null
    private val itemAdapter = ItemAdapter(object: DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(i1: Item, i2: Item): Boolean {
            return i1.id == i2.id
        }

        override fun areContentsTheSame(i1: Item, i2: Item): Boolean {
            return i1.id == i2.id && i1.name == i2.name
        }
    }, {

    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_box, container, false)
        setupViews(rootView)
        return rootView
    }

    override fun setData(bundle: Bundle) {
        data = bundle
        boxId = UUID.fromString(bundle.getString("boxId"))
    }

    override fun onStart() {
        super.onStart()
        (activity as FragmentSwitcher).showUpBtn(true)
        context?.let { c ->
            liveBox = DB.getInstance(c).boxDao().getBoxWithContentLive(boxId!!)
            liveBox!!.observe(this, android.arch.lifecycle.Observer {
                it?.let {
                    activity?.title = it.name
                }
            })
            liveItems = DB.getInstance(c).itemDao().getItemsInBoxLive(boxId!!)
            liveItems!!.observe(this, android.arch.lifecycle.Observer {
                it?.let { l ->
                    if (l.isEmpty()) {
                        rv_items.visibility = View.GONE
                        tv_emptyinfo.visibility = View.VISIBLE
                    } else {
                        rv_items.visibility = View.VISIBLE
                        tv_emptyinfo.visibility = View.GONE
                    }
                    itemAdapter.submitList(l)
                }
            })

        }
    }

    override fun onStop() {
        super.onStop()
        liveBox?.removeObservers(this)
        liveItems?.removeObservers(this)
    }

    override fun onDetach() {
        super.onDetach()
        view?.findViewById<View>(R.id.l_additem)?.setAlpha(0F)
    }

    private fun setupViews (rootView: View) {
        val dropdownHandle = rootView.findViewById<View>(R.id.dropdown_handle)
        dropdownHandle.setOnClickListener {
            animateToFinalPosition(true)
        }
        val lAddItem = rootView.findViewById<View>(R.id.l_additem)
        lAddItem.background = DowndrawerBackground(activity!!.applicationContext)
        val vDropdownCover = rootView.findViewById<View>(R.id.v_dropdown_cover)
        vDropdownCover.setOnClickListener {
            if (!isAnimating) {
                dragOriginalTranslation = lAddItem.translationY
                animateToFinalPosition(true)
            }
        }
        val translationExtendedPosition = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60F, resources.displayMetrics)
        dragOriginalTranslation = translationExtendedPosition
        val fullTranslationDistance = resources.getDimension(R.dimen.additem_translation)
        dropdownHandle.setOnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragOriginalX = ev.rawX
                    dragOriginalY = ev.rawY
                    dragOriginalTranslation = lAddItem.translationY
                    vDropdownCover.visibility = View.VISIBLE
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val tapped = sqrt(Math.pow(ev.rawX - dragOriginalX.toDouble(), 2.0) + Math.pow(ev.rawY - dragOriginalY.toDouble(), 2.0)) < 10.0
                    if (!tapped)
                        animateToFinalPosition(false)
                    else
                        if (addItemIsExtended)
                            addItem()
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
                    lAddItem.translationY = translation
                    vDropdownCover.alpha = 1 - translation / fullTranslationDistance
                    true
                }
                else -> false
            }
        }
        val rvItems = rootView.findViewById<RecyclerView>(R.id.rv_items)
        rvItems.layoutManager = GridLayoutManager(context!!, resources.getInteger(R.integer.itemgrid_columns))
        rvItems.adapter = itemAdapter
    }

    private fun animateToFinalPosition(toggle: Boolean) {
        isAnimating = true
        val fullTranslationDistance = - resources.getDimension(R.dimen.additem_translation)
        val translationExtendedPosition = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60F, resources.displayMetrics)
        var translateTo = -translationExtendedPosition
        val interpolator = DecelerateInterpolator()
        var duration = 90L
        var endingOnClosed = false
        if (
            (dragOriginalTranslation == -translationExtendedPosition && (l_additem.translationY < (-fullTranslationDistance / 3))) ||
            (!toggle && dragOriginalTranslation != -translationExtendedPosition && (l_additem.translationY < (-fullTranslationDistance / 1.3)))
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
        l_additem.animate()
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
        addItemIsExtended = !endingOnClosed
        iv_btn_additem.setImageResource(if (endingOnClosed) R.drawable.ic_add_black_24dp else R.drawable.ic_save_black_24dp)
    }

    private fun addItem() {
        val newName = et_additem_name.text.toString()
        if (newName.isBlank()) {
            Toast.makeText(context!!, "You need to enter a name for your Item.", Toast.LENGTH_SHORT).show()
        } else {
            val newItem = Item(
                UUID.randomUUID(),
                boxId!!,
                newName,
                "pcs",
                1,
                false
            )
            et_additem_name.text.clear()
            doAsync {
                context?.let { c ->
                    DB.getInstance(c).itemDao().insertItem(newItem)
                    uiThread {
                        animateToFinalPosition(true)
                    }
                }
            }
        }
    }
}