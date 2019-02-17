package de.lulebe.inventory.ui

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.lulebe.inventory.R
import de.lulebe.inventory.data.BoxWithContent

class BoxAdapter(
    itmCb: DiffUtil.ItemCallback<BoxWithContent>,
    val clickCb: (BoxWithContent) -> Unit,
    val editCb: (BoxWithContent) -> Unit,
    val deleteCb: (BoxWithContent) -> Unit
) : ListAdapter<BoxWithContent, BoxAdapter.ViewHolder>(itmCb) {

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): BoxAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_box, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(vh: BoxAdapter.ViewHolder, index: Int) {
        vh.tvName.text = getItem(index).name
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tv_name)!!
        val btnEdit = view.findViewById<ImageView>(R.id.btn_edit)!!
        val btnDelete = view.findViewById<ImageView>(R.id.btn_delete)!!
        init {
            view.setOnClickListener {
                clickCb(getItem(adapterPosition))
            }
            btnEdit.setOnClickListener {
                editCb(getItem(adapterPosition))
            }
            btnDelete.setOnClickListener {
                deleteCb(getItem(adapterPosition))
            }
        }
    }
}