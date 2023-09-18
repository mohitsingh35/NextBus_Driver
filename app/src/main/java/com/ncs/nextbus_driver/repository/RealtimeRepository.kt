package com.ncs.tradezy.repository

import android.net.Uri
import com.ncs.nextbus_driver.BusData
import com.ncs.nextbus_driver.DriverData
import com.ncs.nextbus_driver.DriverDataResponse
import com.ncs.nextbus_driver.RealtimeDB
import com.ncs.tradezy.ResultState
import kotlinx.coroutines.flow.Flow


interface RealtimeRepository {

    fun insertLocation(
        item: RealtimeDB.locationData
    ): Flow<ResultState<String>>
    fun insertDriver(
        item: DriverData.DriverDataItem, images:List<Uri>
    ): Flow<ResultState<String>>
    fun insertBus(
        item: BusData.BusDataItem
    ): Flow<ResultState<String>>
    fun getBuses():Flow<ResultState<List<BusData>>>
    fun getDriver():Flow<ResultState<List<DriverDataResponse>>>

}