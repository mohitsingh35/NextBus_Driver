package com.ncs.nextbus_driver

import java.io.Serializable

data class DriverDataResponse(
    val item: DriverDataResponseItem?,
    val key: String?="",

    ): Serializable {
    data class DriverDataResponseItem(
        val name:String?="",
        val email:String?="",
        val phNum:String?="",
        val userId:String?="",
        val profilepic:List<String>? = emptyList()
    ): Serializable

    constructor() : this(null, "")
}