package com.example.bookapplicationkotlin

import android.widget.Filter

/*filter or search data from recycler viewer*/
class FilterPdfAdmin : Filter{

    //arraylist for search
    var filterList:ArrayList<ModelPdf>
    //adapter for filter
    var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint // search value
        val results = FilterResults()
        //search value != null and not empty
        if (constraint!=null && constraint.isEmpty()){
            //avoid case sensitivity
            constraint =constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                //validate
                if (filterList[i].title.lowercase().contains(constraint)){
                    //add to list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            //searched value is either null or empty
            results.count = filterList.size
            results.values = filterList
        }
        return results //return here
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf> /* = java.util.ArrayList<com.example.bookapplicationkotlin.ModelPdf> */

        //notify
        adapterPdfAdmin.notifyDataSetChanged()

    }
}