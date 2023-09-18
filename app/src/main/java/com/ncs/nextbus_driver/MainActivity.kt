package com.ncs.nextbus_driver

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ncs.nextbus_driver.ui.theme.NextBus_DriverTheme
import com.ncs.tradezy.ResultState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var backPressedCount by mutableStateOf(0L)
    private var backPressedToast: Toast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )
        setContent {
            NextBus_DriverTheme {
                var showBusDialog by remember {
                    mutableStateOf(false)
                }
                val viewmodel:BusRegViewModel= hiltViewModel()
                val viewmodel2:AuthViewmodel= hiltViewModel()

                val res=viewmodel.res.value
                val res2=viewmodel2.res.value
                val currentUser = remember { mutableStateOf<DriverDataResponse?>(null) }

                if (res2.item.isNotEmpty()){
                    for (i in 0 until res2.item.size){
                        if (res2.item[i].item?.userId==FirebaseAuth.getInstance().currentUser?.uid){
                            currentUser.value=res2.item[i]
                        }
                    }
                }
                val selectedBus = remember { mutableStateOf<BusData?>(null) }
                if (FirebaseAuth.getInstance().currentUser?.email != "admin@email.com") {
                    if (showBusDialog) {
                        if (res.item.isNotEmpty()) {
                            Dialog(
                                onDismissRequest = {
                                    showBusDialog = false
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(25.dp))
                                        .background(Color.White),
                                ) {
                                    Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Spacer(modifier = Modifier.height(30.dp))
                                        Text(text = "These are Available buses")
                                        Spacer(modifier = Modifier.height(30.dp))
                                        LazyColumn {
                                            items(res.item) { item ->
                                                eachRow(item = item, onClick = {
                                                    selectedBus.value = item
                                                    showBusDialog = false
                                                })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            loadingscreen()
                        }


                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Select the bus you are driving:")
                        Spacer(modifier = Modifier.height(30.dp))
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showBusDialog = true
                            }
                            .background(
                                Color.LightGray
                            ), contentAlignment = Alignment.Center){
                            Text(text = if (selectedBus.value==null) "Select Bus" else selectedBus.value!!.item?.busNum!!)
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                        if (selectedBus.value!=null) {
                            eachRow(item = selectedBus.value!!) {

                            }
                        }
                        Button(onClick = {
                            if (selectedBus.value != null && currentUser.value != null) {
                                val serviceIntent = Intent(applicationContext, LocationService::class.java).apply {
                                    action = LocationService.ACTION_START
                                    putExtra("busDetails", selectedBus.value)
                                    putExtra("driverDetails", currentUser.value)
                                }
                                startService(serviceIntent)
                            }
                        }) {
                            Text(text = "Start")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            Intent(applicationContext, LocationService::class.java).apply {
                                action = LocationService.ACTION_STOP
                                startService(this)
                            }
                        }) {
                            Text(text = "Stop")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            FirebaseAuth.getInstance().signOut()
                            this@MainActivity.startActivity(
                                Intent(
                                    this@MainActivity,
                                    AuthActivity::class.java
                                )
                            )
                            finishAffinity()
                        }) {
                            Text(text = "SignOut")
                        }
                    }
                }
                else{
                    adminScreen()
                }
            }
            
        }
    }
    override fun onBackPressed() {
        if (backPressedCount == 1L) {
            backPressedToast?.cancel()
            finishAffinity()

        } else {
            backPressedCount++
            backPressedToast?.cancel()
            backPressedToast = Toast.makeText(
                this,
                "Press back again to exit",
                Toast.LENGTH_SHORT
            )
            backPressedToast?.show()
        }
    }
}
@Composable
fun eachRow(item:BusData,onClick:()->Unit){
    Column(
        Modifier
            .height(80.dp)
            .clickable { onClick() }
            .fillMaxWidth()) {
        Text(text = item.item?.busNum!!, fontSize = 20.sp)
        Row (Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Text(text = item.item?.name!!, fontSize = 14.sp)
            Text(text = item.item?.fueltype!!, fontSize = 14.sp)
        }
        Row (Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Text(text = "${item.item?.start!!} - ${item.item?.destination}", fontSize = 14.sp)

        }
    }
}


@Composable
fun adminScreen(viewmodel: BusRegViewModel= hiltViewModel()){
    val context= LocalContext.current
    var busName by remember {
        mutableStateOf("")
    }
    var start by remember {
        mutableStateOf("")
    }
    var destination by remember {
        mutableStateOf("")
    }
    var busNumber by remember {
        mutableStateOf("")
    }
    var fueltype by remember {
        mutableStateOf("")
    }
    val focusRequester = remember { FocusRequester() }
    val scope= rememberCoroutineScope()
    var isDialog by remember {
        mutableStateOf(false)
    }
    if (isDialog){
        loadingscreen()
    }
    LazyColumn(){
        items(1) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                Text(text = "Admin Screen")
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Register Buses")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = busName,
                    onValueChange = {
                        if (it.length <= 50) {
                            busName = it
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusRequester.requestFocus()
                        }
                    ),
                    label = {
                        androidx.compose.material3.Text(text = "Bus Name")
                    },
                    shape = RoundedCornerShape(15.dp),
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Black,
                        focusedLeadingIconColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        focusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray,
                        unfocusedLeadingIconColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp), contentAlignment = Alignment.TopEnd
                ) {
                    androidx.compose.material3.Text(
                        text = "${busName.length}/50 ",
                        color = if (busName.length > 50) Color.Red else Color.Gray,
                        modifier = Modifier.padding(start = 16.dp),
                        fontWeight = FontWeight.Thin,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = {
                            if (it.length <= 50) {
                                start = it
                            }
                        },

                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusRequester.requestFocus()
                            }
                        ),
                        label = {
                            androidx.compose.material3.Text(text = "Start Station")
                        }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(15.dp),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = Color.Black,
                            focusedLeadingIconColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            focusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            unfocusedLabelColor = Color.Gray,
                            unfocusedBorderColor = Color.Gray,
                            unfocusedLeadingIconColor = Color.Gray
                        )
                    )
                    Box(modifier = Modifier
                        .width(50.dp)
                        .height(70.dp)
                        .clickable {
                            val temp = start
                            start = destination
                            destination = temp
                        }
                        .clip(CircleShape), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "")
                    }
                    OutlinedTextField(
                        value = destination,
                        onValueChange = {
                            if (it.length <= 50) {
                                destination = it
                            }
                        },modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusRequester.requestFocus()
                            }
                        ),
                        label = {
                            androidx.compose.material3.Text(text = "Destination")
                        },
                        shape = RoundedCornerShape(15.dp),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = Color.Black,
                            focusedLeadingIconColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            focusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            unfocusedLabelColor = Color.Gray,
                            unfocusedBorderColor = Color.Gray,
                            unfocusedLeadingIconColor = Color.Gray
                        )
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "Route: $start <-> $destination")
                }
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = busNumber,
                    onValueChange = {
                        if (it.length <= 10) {
                            busNumber = it
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusRequester.requestFocus()
                        }
                    ),
                    label = {
                        androidx.compose.material3.Text(text = "Bus Number")
                    },
                    shape = RoundedCornerShape(15.dp),
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Black,
                        focusedLeadingIconColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        focusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray,
                        unfocusedLeadingIconColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp), contentAlignment = Alignment.TopEnd
                ) {
                    androidx.compose.material3.Text(
                        text = "${busNumber.length}/10 ",
                        color = if (busNumber.length > 10) Color.Red else Color.Gray,
                        modifier = Modifier.padding(start = 16.dp),
                        fontWeight = FontWeight.Thin,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = fueltype,
                    onValueChange = {
                        if (it.length <= 10) {
                            fueltype = it
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusRequester.requestFocus()
                        }
                    ),
                    label = {
                        androidx.compose.material3.Text(text = "Fuel Type")
                    },
                    shape = RoundedCornerShape(15.dp),
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Black,
                        focusedLeadingIconColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        focusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray,
                        unfocusedLeadingIconColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp), contentAlignment = Alignment.TopEnd
                ) {
                    androidx.compose.material3.Text(
                        text = "${fueltype.length}/10 ",
                        color = if (fueltype.length > 10) Color.Red else Color.Gray,
                        modifier = Modifier.padding(start = 16.dp),
                        fontWeight = FontWeight.Thin,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { if (busName.isNotEmpty() && start.isNotEmpty() && destination!!.isNotEmpty() && busNumber.isNotEmpty() && fueltype.isNotEmpty()) {
                    scope.launch(Dispatchers.Main) {
                        viewmodel
                            .insertBus(
                                BusData.BusDataItem(name = busName,start = start,destination = destination, busNum = busNumber,fueltype = fueltype)
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
                } }) {
                    Text(text = "Submit")
                }
                Spacer(modifier = Modifier.height(50.dp))
                Button(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(context, AuthActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }) {
                    Text(text = "SignOut")
                }
            }
        }
    }
}