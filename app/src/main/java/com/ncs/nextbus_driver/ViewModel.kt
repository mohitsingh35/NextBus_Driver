package com.ncs.nextbus_driver



import androidx.lifecycle.ViewModel
import com.ncs.tradezy.repository.RealtimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val repo: RealtimeRepository
) : ViewModel(){
    fun insertLocation(item: RealtimeDB.locationData)=repo.insertLocation(item)
}