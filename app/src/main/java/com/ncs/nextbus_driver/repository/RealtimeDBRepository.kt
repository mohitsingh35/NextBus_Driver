package com.ncs.tradezy.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.ncs.nextbus_driver.BusData
import com.ncs.nextbus_driver.DriverData
import com.ncs.nextbus_driver.DriverDataResponse
import com.ncs.nextbus_driver.RealtimeDB
import com.ncs.tradezy.ResultState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RealtimeDBRepository @Inject constructor(
    private val db:DatabaseReference
): RealtimeRepository {
    private var storageReference=Firebase.storage

    private val currentUserID=FirebaseAuth.getInstance().currentUser?.uid
    override fun insertLocation(item: RealtimeDB.locationData): Flow<ResultState<String>> =
        callbackFlow {
            trySend(ResultState.Loading)
            db.child("bus_data").push().setValue(
                item
            ).addOnCompleteListener {
                if (it.isSuccessful)
                    trySend(ResultState.Success("Successfully"))
            }.addOnFailureListener {
                trySend(ResultState.Failure(it))
            }
            awaitClose {
                close()
            }
        }


    override fun insertDriver(item: DriverData.DriverDataItem, images: List<Uri>): Flow<ResultState<String>> =
        callbackFlow {
            trySend(ResultState.Loading)
            storageReference = FirebaseStorage.getInstance()
            val totalImages = images.size
            val imageUrls = mutableListOf<String>()
            for (i in 0 until totalImages) {
                val imageRef = storageReference.getReference("images").child(System.currentTimeMillis().toString())
                imageRef.putFile(images[i]).addOnSuccessListener { task ->
                    task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { imageUrl ->
                        imageUrls.add(imageUrl.toString())

                        if (imageUrls.size == totalImages) {
                            val itemRef = db.child("drivers").push()
                            itemRef.setValue(item)
                            itemRef.child("profilepic").setValue(imageUrls)
                            trySend(ResultState.Success("Inserted Successfully"))
                            close()
                        }
                    }
                }
            }

            awaitClose {
                close()
            }
        }

    override fun insertBus(item: BusData.BusDataItem): Flow<ResultState<String>> =
        callbackFlow {
            trySend(ResultState.Loading)
            db.child("buses").push().setValue(
                item
            ).addOnCompleteListener {
                if (it.isSuccessful)
                    trySend(ResultState.Success("Successfully"))
            }.addOnFailureListener {
                trySend(ResultState.Failure(it))
            }
            awaitClose {
                close()
            }
        }

    override fun getBuses(): Flow<ResultState<List<BusData>>> = callbackFlow{
        trySend(ResultState.Loading)

        val valueEvent=object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val items=snapshot.children.map {
                    BusData(
                        it.getValue(BusData.BusDataItem::class.java),
                        key = it.key
                    )
                }
                trySend(ResultState.Success(items))
            }


            override fun onCancelled(error: DatabaseError) {
                trySend(ResultState.Failure(error.toException()))
            }

        }
        db.child("buses").addValueEventListener(valueEvent)
        awaitClose{
            db.child("buses").removeEventListener(valueEvent)
            close()
        }
    }
    override fun getDriver(): Flow<ResultState<List<DriverDataResponse>>> = callbackFlow{
        trySend(ResultState.Loading)

        val valueEvent=object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val items=snapshot.children.map {
                    DriverDataResponse(
                        it.getValue(DriverDataResponse.DriverDataResponseItem::class.java),
                        key = it.key
                    )
                }
                trySend(ResultState.Success(items))
            }


            override fun onCancelled(error: DatabaseError) {
                trySend(ResultState.Failure(error.toException()))
            }

        }
        db.child("drivers").addValueEventListener(valueEvent)
        awaitClose{
            db.child("drivers").removeEventListener(valueEvent)
            close()
        }
    }
}