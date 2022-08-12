package com.example.bookapplicationkotlin

import android.widget.Filter

class FilterPdfUser: Filter {

    //arraylist for search
    var filterList:ArrayList<ModelPdf>
    //adapter for filter
    var adapterPdfUser: AdapterPdfUser

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint // search value
        val results = FilterResults()
        //search value != null and not empty
        if (constraint!=null && constraint.isEmpty()){
            //avoid case sensitivity
            constraint =constraint.toString().uppercase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                //validate
                if (filterList[i].title.uppercase().contains(constraint)){
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
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfUser.notifyDataSetChanged()

    }
}