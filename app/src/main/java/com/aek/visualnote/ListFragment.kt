package com.aek.visualnote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_list.*
import java.lang.Exception


class ListFragment : Fragment() {

    var noteList: ArrayList<Note> = ArrayList()

    private lateinit var listAdapter: ListRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteList.clear()
        listAdapter = ListRecyclerAdapter(noteList)
        recyclerView.layoutManager= LinearLayoutManager(context)
        recyclerView.adapter=listAdapter
        getDatabaseData()
    }

    fun getDatabaseData() {


        try {

            activity?.let {
                val database = it.openOrCreateDatabase("localnotes", Context.MODE_PRIVATE, null)

                val cursor = database.rawQuery("SELECT * FROM notes", null)
                val idIndex = cursor.getColumnIndex("id")
                val textIndex = cursor.getColumnIndex("notetext")
                val imageIndex = cursor.getColumnIndex("imagesrc")

                while (cursor.moveToNext()) {

                    val imgData = cursor.getBlob(imageIndex)

                    noteList.add(
                        Note(
                            cursor.getInt(idIndex),
                            cursor.getString(textIndex),
                            BitmapFactory.decodeByteArray(imgData,0,imgData.size)
                        )
                    )

                }
                listAdapter.notifyDataSetChanged()

                cursor.close()

            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}