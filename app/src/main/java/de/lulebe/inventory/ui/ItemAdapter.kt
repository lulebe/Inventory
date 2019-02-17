package de.lulebe.inventory.ui

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.lulebe.inventory.R
import de.lulebe.inventory.data.Item

class ItemAdapter(
    itmCb: DiffUtil.ItemCallback<Item>,
    val clickCb: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ViewHolder>(itmCb) {

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ItemAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(vh: ItemAdapter.ViewHolder, index: Int) {
        vh.tvName.text = getItem(index).name
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tv_name)!!
        init {
            view.setOnClickListener {
                clickCb(getItem(adapterPosition))
            }
        }
    }
}