package com.ncs.nextbus_driver

import java.io.Serializable

data class DriverData(
    val item: DriverDataItem?,
    val key: String?="",

    ):Serializable{
    data class DriverDataItem(
        val name:String?="",
        val email:String?="",
        val phNum:String?="",
        val userId:String?=""
    ): Serializable

    constructor() : this(null, "")
}