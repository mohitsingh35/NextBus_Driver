package com.ncs.nextbus_driver

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.ncs.tradezy.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewmodel: AuthViewmodel= hiltViewModel()
){
    var email = remember {
        mutableStateOf("")
    }
    var password = remember {
        mutableStateOf("")
    }
    var email1 = remember {
        mutableStateOf("")
    }
    var password1 = remember {
        mutableStateOf("")
    }
    val scope= rememberCoroutineScope()
    val context= LocalContext.current
    var isDialog by remember {
        mutableStateOf(false)
    }
    var showregister by remember{
        mutableStateOf(true)
    }
    var driverreg by remember{
        mutableStateOf(false)
    }
    var showLogin by remember{
        mutableStateOf(false)
    }
    if(isDialog){
        loadingscreen()
    }
    var imageUris by remember { mutableStateOf(emptyList<Uri>()) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        uris?.let {
            imageUris = it.take(1)
        }
    }
    LazyColumn(
        Modifier
            .padding(20.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.Center) {
        if (showregister && !showLogin) {
            item() {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Register")
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = email.value, onValueChange = {
                            email.value = it
                        }, placeholder = { Text(text = " Enter Email") })
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = password.value, onValueChange = {
                            password.value = it
                        }, placeholder = { Text(text = " Enter password") })
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {
                            if (email.value.isNotEmpty() && password.value.isNotEmpty()){
                                scope.launch(Dispatchers.Main) {
                                    viewmodel.createUser(
                                        AuthUser(email.value, password.value)
                                    ).collect {
                                        when (it) {
                                            is ResultState.Success -> {
                                                context.showMsg(it.data)
                                                isDialog = false
                                                driverreg=true
                                                showregister=false
                                            }

                                            is ResultState.Failure -> {
                                                context.showMsg(it.msg.toString())
                                                isDialog = false
                                            }

                                            ResultState.Loading -> {
                                                isDialog = true
                                            }

                                            else -> {}
                                        }
                                    }
                                }
                            }
                            else{
                                context.showMsg("Fill all fields")
                            }

                        }) {
                            Text(text = "Register")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {
                            showregister = false
                            showLogin = true
                        }) {
                            Text(text = "Login")
                        }

                    }
                }
            }
        }
        if (!showregister && !showLogin && driverreg){

            item {
                var driveremail by remember {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser?.email)
                }
                var drivername by remember {
                    mutableStateOf("")
                }
                var driverphnum by remember {
                    mutableStateOf("")
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Complete Your Driver Registration")
                        Spacer(modifier = Modifier.height(50.dp))
                        Box(modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center){
                            if (imageUris.isEmpty()){
                                Image(imageVector = Icons.Filled.Face, contentDescription = "")
                            }
                            else{
                                val bitmap = loadImageBitmap(imageUris[0], context)
                                bitmap?.let { btm ->
                                    Image(
                                        bitmap = btm.asImageBitmap(),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                        TextField(value = drivername, onValueChange = {
                            drivername = it
                        }, placeholder = { Text(text = "Name") })
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = driverphnum, onValueChange = {
                            driverphnum = it
                        }, placeholder = { Text(text = "Ph. Num") })
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = driveremail!!, onValueChange = {
                            driveremail = it
                        }, placeholder = { Text(text = "Enter Email") }, enabled = false)
                        Spacer(modifier = Modifier.height(10.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {
                            if (drivername.isNotEmpty() && driverphnum.isNotEmpty() && driveremail!!.isNotEmpty()) {
                                scope.launch(Dispatchers.Main) {
                                    viewmodel
                                        .insertDriver(
                                            DriverData.DriverDataItem(
                                                name = drivername,
                                                email = driveremail,
                                                phNum = driverphnum,
                                                userId = FirebaseAuth.getInstance().currentUser?.uid
                                            ),
                                            images = imageUris,
                                        )
                                        .collect {
                                            when (it) {
                                                is ResultState.Success -> {
                                                    isDialog = false
                                                    context.showMsg(
                                                        msg = it.data
                                                    )
                                                    context.startActivity(Intent(context,MainActivity::class.java))
                                                }

                                                is ResultState.Failure -> {
                                                    isDialog = false
                                                    context.showMsg(
                                                        msg = it.msg.toString()
                                                    )
                                                }

                                                ResultState.Loading -> {
                                                    isDialog = true
                                                }

                                                else -> {}
                                            }

                                        }
                                }
                            }else{
                                context.showMsg("Enter all the fields")
                            }
                        }) {
                            Text(text = "Register")
                        }

                    }
                }
            }
        }
        if (!showregister && showLogin) {
            item {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Login")
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = email1.value, onValueChange = {
                            email1.value = it
                        }, placeholder = { Text(text = " Enter Email") })
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = password1.value, onValueChange = {
                            password1.value = it
                        }, placeholder = { Text(text = " Enter password") })
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {
                            if(email1.value.isNotEmpty() && password1.value.isNotEmpty()) {
                                scope.launch(Dispatchers.Main) {
                                    viewmodel.loginUser(
                                        AuthUser(email1.value, password1.value)
                                    ).collect {
                                        when (it) {
                                            is ResultState.Success -> {
                                                context.showMsg(it.data)
                                                isDialog = false
                                                context.startActivity(Intent(context,MainActivity::class.java))
                                            }

                                            is ResultState.Failure -> {
                                                context.showMsg(it.msg.toString())
                                                isDialog = false
                                            }

                                            ResultState.Loading -> {
                                                isDialog = true
                                            }

                                            else -> {}
                                        }
                                    }
                                }
                            }
                            else{
                                context.showMsg("Fill all fields")
                            }
                        }) {
                            Text(text = "Login")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {
                            showregister = true
                            showLogin = false
                        }) {
                            Text(text = "Register")
                        }

                    }
                }
            }
        }
    }
}