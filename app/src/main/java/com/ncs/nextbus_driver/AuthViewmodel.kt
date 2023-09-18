package com.ncs.nextbus_driver

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncs.nextbus_driver.repository.AuthRepository
import com.ncs.tradezy.ResultState
import com.ncs.tradezy.repository.RealtimeRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewmodel @Inject constructor(
    private val repo: AuthRepository,private val repo2: RealtimeRepository
) :ViewModel(){
    fun createUser(authUser:AuthUser) = repo.createUser((authUser))
    fun loginUser(authUser: AuthUser)=repo.loginUser(authUser)
    fun insertDriver(items:DriverData.DriverDataItem,images:List<Uri>)=repo2.insertDriver(items,images)
    private val _res: MutableState<DriverState> = mutableStateOf(
        DriverState()
    )
    val res: State<DriverState> = _res
    init {
        viewModelScope.launch {
            repo2.getDriver().collect{
                when(it){
                    is ResultState.Success->{
                        _res.value= DriverState(
                            item = it.data
                        )
                    }
                    is ResultState.Failure->{
                        _res.value= DriverState(
                            error = it.msg.toString()
                        )
                    }
                    ResultState.Loading->{
                        _res.value= DriverState(
                            isLoading = true
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}