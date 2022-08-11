package com.example.bookapplicationkotlin

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationkotlin.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory:RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable{

    private val context:Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList:ArrayList<ModelCategory>

    private var filter:FilterCategory? = null

    private lateinit var binding:RowCategoryBinding
    //constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }




    //viewHolder class /init UI view for row_category.xml
    inner class HolderCategory(itemView: View):RecyclerView.ViewHolder(itemView){
        //init ui views
        var categoryTv:TextView = binding.categoryTv
        var deleteBtn:ImageButton = binding.deleteBtn

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //bind row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent, false)

        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        /*Get Data, Set Data, Clicks etc*/

        //get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //set data
        holder.categoryTv.text = category

        // clicks handling,delete category
        holder.deleteBtn.setOnClickListener {
            //confirmation
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure in deleting this category?")
                .setPositiveButton("Confirm"){a, d->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteCat(model, holder)

                }
                .setNegativeButton("Cancel"){a,d->
                    a.dismiss()

                }
                .show()
        }
    }

    private fun deleteCat(model: ModelCategory, holder: HolderCategory) {
        //get category id
        val id = model.id
        //Firebase DB
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                Toast.makeText(context, "Unable to delete error:${e.message}", Toast.LENGTH_SHORT).show()

            }


    }

    override fun getItemCount(): Int {
        return categoryArrayList.size //number of items in list
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }
}