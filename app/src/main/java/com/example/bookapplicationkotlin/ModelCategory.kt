package com.example.bookapplicationkotlin

class ModelCategory {

    //same variables in Fb
    var id :String = ""
    var category:String = ""
    var timestamp:Long = 0
    var uid:String =""

    //empty constructor
    constructor()

    // normal constructor
    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }




}