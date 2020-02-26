package com.kuelye.vkcup20ii.a.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuelye.vkcup20ii.a.R
import com.kuelye.vkcup20ii.a.model.VKDocument
import kotlinx.android.synthetic.main.activity_documents.*

class DocumentsActivity : AppCompatActivity() {

    private lateinit var adapter: Adapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)
        initlaizeLayout()
    }

    private fun initlaizeLayout() {
        adapter = Adapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val documents = List<VKDocument>(10) { VKDocument() }
        adapter.documents = documents
    }

    private class Adapter(context: Context) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        var documents: List<VKDocument>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int = documents?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(layoutInflater.inflate(R.layout.layout_document, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        }

    }

}
