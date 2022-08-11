package com.example.bookapplicationkotlin

import android.widget.Filter

class FilterCategory: Filter {

    // arraylist for search
    private var filterList:ArrayList<ModelCategory>

    //adapter for filter implement
    private var adapterCategory:AdapterCategory

    //constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constriant: CharSequence?): FilterResults {
        var constraint = constriant
        val results = FilterResults()

        //no empty and null value
        if (constraint!= null && constraint.isNotEmpty()){
            //searched value is not null nor empty


            // change to uppercase
            constraint = constriant.toString().uppercase()
            val filteredModel:ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until filterList.size){
                //validate
                if (filterList[i].category.uppercase().contains(constraint)){
                    //add to filtered list
                    filteredModel.add(filterList[i])
                }
            }
            results.count = filteredModel.size
            results.values = filteredModel
        }
        else{
            //search value is either null or empty
            results.count = filterList.size
            results.values = filterList

        }

        return results //results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory> /* = java.util.ArrayList<com.example.bookapplicationkotlin.ModelCategory> */

        //notify changes
        adapterCategory.notifyDataSetChanged()
    }
}