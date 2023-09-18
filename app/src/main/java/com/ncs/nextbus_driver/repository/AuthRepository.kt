package com.ncs.nextbus_driver.repository

import android.app.Activity
import com.ncs.nextbus_driver.AuthUser
import com.ncs.tradezy.ResultState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun createUser(
        auth: AuthUser
    ): Flow<ResultState<String>>
    fun loginUser(
        auth: AuthUser
    ): Flow<ResultState<String>>
}
