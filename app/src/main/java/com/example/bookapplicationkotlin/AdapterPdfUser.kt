package com.example.bookapplicationkotlin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationkotlin.databinding.RowPdfUserBinding

class AdapterPdfUser :RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable{

    //context
    private var context:Context

    //arraylist to hold pdfs
    public var pdfArrayList:ArrayList<ModelPdf>
    private var filterList:ArrayList<ModelPdf>

    //viewBinding
    private lateinit var binding: RowPdfUserBinding

    //filter object
    private var filter: FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        //view binding
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent, false)
        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        /*-----Get data,Set data,Handle click etc.-----*/

        //get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val url = model.url
        val timestamp = model.timestamp
        //convert timestamp to dd/MM/yyyy format
        val formattedDate = MyApp.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text =description
        holder.dateTv.text =formattedDate

        //load further details, Category id pdf size etc.
        MyApp.loadCategory(categoryId,holder.categoryTv)
        MyApp.loadPdfFromUrlSinglePage(url,title, holder.pdfView, holder.progressBar, null)
        MyApp.loadPdfSize(url, title, holder.sizeTv)

        //handle item click, open Pdf Activity
        holder.itemView.setOnClickListener{
            //intent with book id
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",bookId) //used to load book details
            context.startActivity(intent)
        }



    }

    override fun getItemCount(): Int {
        return pdfArrayList.size //return number of records
    }

    /*viewHolder class row_pdf_user.xml*/
    inner class HolderPdfUser(itemView: View): RecyclerView.ViewHolder(itemView){
        //init ui views of row_pdf_user.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn

    }

    override fun getFilter(): Filter {
        if (filter==null){
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }
}