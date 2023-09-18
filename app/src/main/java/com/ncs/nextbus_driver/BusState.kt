package com.ncs.nextbus_driver

data class BusState(
    val item:List<BusData> = emptyList(),
    val error:String = "",
    val isLoading:Boolean=false
)