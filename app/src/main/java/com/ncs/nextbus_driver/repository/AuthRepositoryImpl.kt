package com.ncs.authenticationapp.firebaseauth.repository

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.ncs.nextbus_driver.AuthUser

import com.ncs.nextbus_driver.repository.AuthRepository
import com.ncs.tradezy.ResultState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authdb:FirebaseAuth
): AuthRepository {
    private lateinit var onVerificationCode:String

    override fun createUser(auth: AuthUser): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        authdb.createUserWithEmailAndPassword(
            auth.email!!,
            auth.password!!
        ).addOnCompleteListener {
            if (it.isSuccessful){
                trySend(ResultState.Success("User Created Successfully"))
                //userid
                Log.d("mohit","User is is: ${authdb.currentUser?.uid}")
            }
        }.addOnFailureListener {
            trySend(ResultState.Failure(it))
        }
        awaitClose {
            close()
        }

    }

    override fun loginUser(auth: AuthUser): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)

        authdb.signInWithEmailAndPassword(
            auth.email!!,
            auth.password!!
        ).addOnSuccessListener {
            trySend(ResultState.Success("Login Successfull"))
        }.addOnFailureListener {
            trySend(ResultState.Failure(it))
        }
        awaitClose {
            close()
        }
    }


}