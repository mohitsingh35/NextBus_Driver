package com.ncs.nextbus_driver


import java.io.Serializable

data class RealtimeDB(
    val item: locationData,
    val key: String
)  {

    data class locationData(
        val startinglat:Double?=28.404609,
        val startinglong:Double?=77.8261555,
        val endinglat:Double?= 28.407135,
        val endinglong:Double?=77.826489,
        val accuracy: Double,
        val latitude: Double,
        val longitude: Double,
        val speed: Double,
        val time: Long,
        val busNum:String,
        val busID:String,
        val fueltype:String,
        val start:String,
        val destination:String,
        val driverName:String,
        val driverphNum:String,
        val driverprofilepic:String,
        val driverId:String,
        val busName:String,
    )
}