package com.ncs.nextbus_driver

data class DriverState(
    val item:List<DriverDataResponse> = emptyList(),
    val error:String = "",
    val isLoading:Boolean=false
)