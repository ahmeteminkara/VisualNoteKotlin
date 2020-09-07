package com.aek.visualnote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_row.view.*

class ListRecyclerAdapter(val list: ArrayList<Note>) :
    RecyclerView.Adapter<ListRecyclerAdapter.DataHoler>() {
    class DataHoler(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataHoler {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row, parent, false)
        return DataHoler(view)
    }

    override fun onBindViewHolder(holder: DataHoler, position: Int) {
        holder.itemView.textView.text = list.elementAt(position).text
        holder.itemView.imageView.setImageBitmap(list.elementAt(position).imageBitmap)

        holder.itemView.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToSaveDataFragment( enumActionNeed = ActionNeed.EditData,id=list.elementAt(position).id)
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


}