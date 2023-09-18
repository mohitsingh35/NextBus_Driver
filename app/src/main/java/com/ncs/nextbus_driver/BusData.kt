package com.ncs.nextbus_driver

import java.io.Serializable

data class BusData(
    val item: BusDataItem?,
    val key: String?="",

    ):Serializable{
    data class BusDataItem(
        val name:String?="",
        val start:String?="",
        val destination:String?="",
        val busNum:String?="",
        val fueltype:String?="",
        ): Serializable

    constructor() : this(null, "")
}