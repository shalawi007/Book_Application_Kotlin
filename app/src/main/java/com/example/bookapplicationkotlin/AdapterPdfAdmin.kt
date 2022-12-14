package com.example.bookapplicationkotlin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationkotlin.databinding.RowPdfAdminBinding

class AdapterPdfAdmin :RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> , Filterable{

    //context
    private var context:Context
    //arraylists to hold pdfs
    public var pdfArrayList:ArrayList<ModelPdf>
    private var filterList:ArrayList<ModelPdf>

    //viewBinding
    private lateinit var binding:RowPdfAdminBinding

    //filter object
    private var filter: FilterPdfAdmin? = null

    //constructor

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    //viewHolder class /init UI view for row_category.xml

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        //bind row_pdf_admin.xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        /*-----Get data,Set data,Handle click etc.-----*/

        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        //convert timestamp to dd/MM/yyyy format
        val formattedDate = MyApp.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text =description
        holder.dateTv.text =formattedDate

        //load further details, Category id pdf size etc.
        MyApp.loadCategory(categoryId,holder.categoryTv)
        MyApp.loadPdfFromUrlSinglePage(pdfUrl,title, holder.pdfView, holder.progressBar, null)
        MyApp.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //handle click, show dialog
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        //handle item click, open Pdf Activity
        holder.itemView.setOnClickListener{
            //intent with book id
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",pdfId) //used to load book details
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: ModelPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) {
        //get id, url,title
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options){dialog, pos ->
                //handle item click
                if (pos == 0){
                    //edit is clicked
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId) //passed bookedId for edit
                    context.startActivity(intent)

                }
                else if (pos == 1){
                    //delete is clicked
                    MyApp.deleteBook(context,bookId,bookUrl,bookTitle)

                }
            }
            .show()

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size //items count
    }

    override fun getFilter(): Filter {
        if (filter==null){
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }
    inner class HolderPdfAdmin(itemView: View): RecyclerView.ViewHolder(itemView){
        //init ui views of row_pdf_admin.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn

    }

}