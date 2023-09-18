package com.ncs.nextbus_driver

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncs.tradezy.ResultState
import com.ncs.tradezy.repository.RealtimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusRegViewModel @Inject constructor(
    private val repo: RealtimeRepository
) : ViewModel(){
    private val _res: MutableState<BusState> = mutableStateOf(
        BusState()
    )
    val res: State<BusState> = _res
    fun insertBus(item: BusData.BusDataItem)=repo.insertBus(item)
    init {
        viewModelScope.launch {
            repo.getBuses().collect{
                when(it){
                    is ResultState.Success->{
                        _res.value= BusState(
                            item = it.data
                        )
                    }
                    is ResultState.Failure->{
                        _res.value= BusState(
                            error = it.msg.toString()
                        )
                    }
                    ResultState.Loading->{
                        _res.value= BusState(
                            isLoading = true
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}