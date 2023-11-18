package com.example.ok2

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ok2.ui.theme.Ok2Theme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import org.apache.commons.lang3.StringUtils.substringBefore
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

//Sources for GPS stuff are https://stackoverflow.com/questions/45958226/get-location-android-kotlin and ChatGPT
private lateinit var fusedLocationClient: FusedLocationProviderClient

var lat = 0.0
var long = 0.0
var selectedOption_global = ""
var random_lat_global = 0.0
var random_long_global = 0.0
var distance_global = 0.0
var tstart_global: Long = 0
var tend_global: Long = 0
var datestart_global = ""
var dateend_global = ""
var rank_global = ""
var points_global = 0
var currlat_global = 0.0
var currlong_global = 0.0
var accept_chk = false
var update_chk = false
var world_counter = 0
// Settings
var range_option_global = "SQUARE"
var gp_option_global = "STANDARD"

class MainActivity : ComponentActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        val dbHandler = DBHandler(this)
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            Ok2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var geocoder: Geocoder
                    geocoder = Geocoder(this)
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Request the permission
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_PERMISSION_REQUEST_CODE
                        )
                        getLastKnownLocation()
                        Navigation(dbHandler, geocoder)
                    } else {
                        getLastKnownLocation()
                        Navigation(dbHandler, geocoder)
                    }
                }
            }
        }
    }
}

@Composable
fun Navigation(dbHandler: DBHandler, geocoder: Geocoder){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            MainScreen(navController = navController)
        }
        composable(route = Screen.CoordsScreen.route){
            CoordsScreen(navController = navController, dbHandler)
        }
        composable(route = Screen.LogScreen.route){
            LogScreen(dbHandler)
        }
        composable(route = Screen.AboutScreen.route){
            AboutScreen()
        }
        composable(route = Screen.AcceptScreen.route){
            AcceptScreen(navController = navController)
        }
        composable(route = Screen.FinishScreen.route){
            FinishScreen(navController = navController, dbHandler, geocoder)
        }
        composable(route = Screen.CheevoScreen.route){
            CheevoScreen(dbHandler)
        }
        composable(route = Screen.SettingsScreen.route){
            SettingsScreen(navController = navController)
        }
    }
}

@Composable
fun GD_Logo() {
    val image: Painter = painterResource(id = R.drawable.geodoom)
    Image(painter = image,contentDescription = "")
}

@Composable
fun MainScreen(navController: NavController) {
    Log.d("RANGE OPTION GLOBAL", "$range_option_global")
    if (update_chk){
        getLastKnownLocation()
        update_chk = false
        navController.navigate(Screen.AcceptScreen.route)
    }
    val gradient = Brush.verticalGradient(
        0.0f to Color.Black,
        1.0f to Color.Red,
        startY = 0.0f,
        endY = 1500.0f
    )
    Box(modifier = Modifier.background(gradient))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        GD_Logo()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 20.sp,
            text = "THE WORLD's most hardcore exploration game\n\n",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedButton(
                onClick = {
                    if (!accept_chk){
                        navController.navigate(Screen.CoordsScreen.route)
                    } else if (accept_chk){
                        navController.navigate(Screen.AcceptScreen.route)
                    }

                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White)
            ) {
                if (!accept_chk){
                    Text(text = "BEGIN MISSION", fontSize = 30.sp)
                } else if (accept_chk){
                    Text(text = "RESUME MISSION", fontSize = 30.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.LogScreen.route)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White)
            ) {
                Text(text = "MISSION LOGS", fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.CheevoScreen.route)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White)
            ) {
                Text(text = "ACHIEVEMENTS", fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.AboutScreen.route)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White)
            ) {
                Text(text = "ABOUT", fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.SettingsScreen.route)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White)
            ) {
                Text(text = "SETTINGS", fontSize = 30.sp)
            }
        }
    }

}



@Composable
fun CoordsScreen(navController: NavController, dbHandler: DBHandler) {
    val gradient = Brush.verticalGradient(
        0.0f to Color.Black,
        1.0f to Color.Red,
        startY = 0.0f,
        endY = 1500.0f
    )
    Box(modifier = Modifier.background(gradient))


    val current_location = lat.toString() + ", " + long.toString()
    var random_lat by remember {mutableStateOf(0.0)}
    var random_long by remember {mutableStateOf(0.0)}


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())

    ) {
        GD_Logo()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 40.sp,
            text = "NEW MISSION\n",
            color = Color.Green,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 40.sp,
            text = "Your current\n\nlocation:",
            color = Color.Green,
            textAlign = TextAlign.Center
        )
        Text(
            fontSize = 40.sp,
            text = lat.toString() + ",\n\n" + long.toString(),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            fontSize = 40.sp,
            text = "Generated\n\nlocation:",
            color = Color.Green,
            textAlign = TextAlign.Center
        )
        Text(
            fontSize = 40.sp,
            text = random_lat.toString() + ",\n\n" + random_long.toString(),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        var accept by remember { mutableStateOf(false) }
       var distance = getDistance()
        if (accept){
            Text(
                fontSize = 16.sp,
                text = "Distance: $distance km\n",
                color = Color.Yellow

            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        var gm_text = ""
        if (gp_option_global == "STANDARD") { gm_text = "Difficulty" }
        if (gp_option_global == "CONTINENTAL") { gm_text = "Continents" }

        Text(
            fontSize = 40.sp,
            text = "$gm_text",
            color = Color.Green
        )
        val radioOptions_standard = mutableListOf("I'M TOO YOUNG TO DIE", "HEY, NOT TOO ROUGH", "HURT ME PLENTY", "ULTRA-VIOLENCE", "NIGHTMARE")
        val radioOptions_continental = mutableListOf("CONTINENTAL/AFRICA", "CONTINENTAL/ASIA", "CONTINENTAL/NORTH AMERICA"
            , "CONTINENTAL/SOUTH AMERICA", "CONTINENTAL/AUSTRALIA", "CONTINENTAL/EUROPE")
        var radioOptions = mutableListOf<String>()
        if (gp_option_global == "STANDARD"){
            radioOptions = radioOptions_standard
        } else if (gp_option_global == "CONTINENTAL"){
            radioOptions = radioOptions_continental
        }
        val size = radioOptions.size
        val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[size/2]) }
        val cheevos_n = dbHandler.cheevos_Name
        val cheevos_list_n = cheevos_n.split("*")
        if (world_counter >= 13 || "TOKI WO TOMARE" in cheevos_list_n && gp_option_global != "CONTINENTAL"){
            radioOptions.add("THE WORLD")
            if ("TOKI WO TOMARE" in cheevos_list_n == false){
                dbHandler.addEntry2("TOKI WO TOMARE", getDate())
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            radioOptions.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (text == selectedOption),
                            onClick = {
                                onOptionSelected(text)
                                if (selectedOption == "NIGHTMARE") {
                                    world_counter++
                                }
                            }
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    RadioButton(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                    )

                    Text(
                        text = text,
                        color = Color.White,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        var lat_long_chk = 0
        if (accept == false) {
            OutlinedButton(
                onClick = {
                    tstart_global = System.currentTimeMillis()
                    getLastKnownLocation()
                    random_lat = generateCoords(lat, long, lat_long_chk, selectedOption)
                    lat_long_chk = 1
                    random_long = generateCoords(lat, long, lat_long_chk, selectedOption)
                    lat_long_chk = 0
                    random_lat_global = random_lat
                    random_long_global = random_long
                    accept = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = "GENERATE", fontSize = 30.sp)
            }
        } else {
            OutlinedButton(
                onClick = {
                    tstart_global = System.currentTimeMillis()
                    getLastKnownLocation()
                    random_lat = generateCoords(lat, long, lat_long_chk, selectedOption)
                    lat_long_chk = 1
                    random_long = generateCoords(lat, long, lat_long_chk, selectedOption)
                    lat_long_chk = 0
                    random_lat_global = random_lat
                    random_long_global = random_long
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = "GENERATE", fontSize = 30.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            MapsButton(random_lat, random_long)
            Spacer(modifier = Modifier.height(20.dp))
            AcceptButton(navController, random_lat, random_long, selectedOption, distance, current_location)
        }
    }
}

@Composable
fun AcceptScreen(navController: NavController) {

    var rank = getRank()
    var points = getPoints(rank, selectedOption_global, "current")
    var tpp = getPoints(rank, selectedOption_global, "total")

    val gradient = Brush.verticalGradient(
        0.0f to Color.Black,
        1.0f to Color.Red,
        startY = 0.0f,
        endY = 1500.0f
    )
    Box(modifier = Modifier.background(gradient))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())

    ) {
        GD_Logo()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 40.sp,
            text = "CURRENT\n\nMISSION\n",
            color = Color.Green,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 40.sp,
            text = "Your current\n\nlocation:",
            color = Color.Green,
            textAlign = TextAlign.Center
        )
        var current_location = lat.toString() + ", " + long.toString()
        Text(
            fontSize = 40.sp,
            text = lat.toString() + ",\n\n" + long.toString(),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        OutlinedButton(
            onClick = {
                update_chk = true
                getLastKnownLocation()
                navController.navigate(Screen.MainScreen.route)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White)
        ) {
            Text(text = "UPDATE LOCATION", fontSize = 30.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        var distance = getDistance()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 40.sp,
            text = "Your destination:",
            color = Color.Green,
            textAlign = TextAlign.Center
        )
        Text(
            fontSize = 40.sp,
            text = random_lat_global.toString() + ",\n\n" + random_long_global.toString(),
            color = Color.White,
            textAlign = TextAlign.Center

        )
        MapsButton(random_lat_global, random_long_global)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 20.sp,
            text = "Total Possible Points: $tpp\n",
            color = Color.Yellow,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 20.sp,
            text = "You are approximately $distance km away!\n",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            fontSize = 20.sp,
            text = "Current Points: $points\nCurrent Rank: $rank\n",
            color = Color.Yellow,
            textAlign = TextAlign.Center
        )
        getRank_IMG(rank)
        Spacer(modifier = Modifier.height(40.dp))
        FinishButton(navController, rank, points)
    }
}

@Composable
fun FinishButton(navController: NavController, rank: String, points: Int){
    OutlinedButton(
        onClick = {
            rank_global = rank
            points_global = points
            tend_global = System.currentTimeMillis()
            dateend_global = getDate()
            navController.navigate(Screen.FinishScreen.route)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White)
    ) {
        Text(text = "FINISH MISSION", fontSize = 30.sp)
    }
}

fun resetGlobalVariables(){
    lat = 0.0
    long = 0.0
    selectedOption_global = ""
    random_lat_global = 0.0
    random_long_global = 0.0
    distance_global = 0.0
    points_global = 0
    rank_global = ""
    tstart_global = 0
    tend_global = 0
    datestart_global = ""
    dateend_global = ""
    currlat_global = 0.0
    currlong_global = 0.0
    accept_chk = false
    update_chk = false
}

@Composable
fun FinishScreen(navController: NavController, dbHandler: DBHandler, geocoder: Geocoder){
    val gradient = Brush.verticalGradient(
        0.0f to Color.Black,
        1.0f to Color.Red,
        startY = 0.0f,
        endY = 1500.0f
    )
    Box(modifier = Modifier.background(gradient))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())

    ) {
        var text_resetter = false
        if (!text_resetter) {
            if (rank_global == "D") {
                Text(
                    fontSize = 40.sp,
                    text = "MISSION\n\nFAILED!\n",
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    fontSize = 40.sp,
                    text = "MISSION\n\nSUCCESS!\n",
                    color = Color.Green,
                    textAlign = TextAlign.Center
                )
            }
        }
        val time_taken = getTotalTime()
        getRank_IMG(rank_global)
        val distance = getDistance()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 20.sp,
            text = "You were $distance km away!\nIt took you $time_taken!",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            fontSize = 20.sp,
            text = "Points Earned: $points_global\n",
            color = Color.Yellow,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(
            onClick = {
                text_resetter = true
                dbHandler.addEntry(selectedOption_global, currlat_global, currlong_global, random_lat_global, random_long_global, rank_global, points_global, datestart_global, dateend_global, time_taken)
                cheevoChecker(dbHandler, geocoder, time_taken)
                resetGlobalVariables()
                navController.navigate(Screen.MainScreen.route)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White)
        ) {
            Text(text = "ACCEPT", fontSize = 30.sp)
        }
    }
}

@Composable
fun TransitionScreen(navController: NavController){

}

fun getTotalTime(): String{
    var seconds = ((tend_global - tstart_global) / 1000)
    var minutes = 0.0
    var hours = 0.0
    var t_kwrd = "$seconds seconds"
    if (seconds >= 60){
        minutes = (seconds / 60).toDouble()
        minutes = minutes.toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
        val minutes_s = NumberFormat.getInstance().format(minutes)
        seconds %= 60
        t_kwrd = "$minutes_s minutes, $seconds seconds"
    }
    if (minutes >= 60){
        minutes = minutes.toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
        val minutes_s = NumberFormat.getInstance().format(minutes)
        hours = (minutes / 60)
        hours = hours.toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
        val hours_s = NumberFormat.getInstance().format(hours)
        minutes %= 60
        t_kwrd = "$hours_s hours, $minutes_s minutes"
    }
    return t_kwrd
}

fun getDistance(): Double{
    val debug = false
    val debug_distance = 0.05
    if (!debug){
        val distance_r = floatArrayOf(.1f)
        android.location.Location.distanceBetween(lat, long, random_lat_global, random_long_global, distance_r)
        var distance = distance_r[0]*0.001
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        distance = df.format(distance).toDouble()
        return distance
    } else {
        return debug_distance
    }
}

fun getDistance_Custom(custom_lat: Double, custom_long: Double): Double{
    // for achievements, was easier than refactoring
    val distance_r = floatArrayOf(.1f)
    android.location.Location.distanceBetween(lat, long, custom_lat, custom_long, distance_r)
    var distance = distance_r[0]*0.001
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    distance = df.format(distance).toDouble()
    return distance
}

fun getRank(): String{
    val ranks = arrayOf("SS", "S", "A", "B", "C", "D")
    var rank = ""
    if (getDistance() <= 0.1) { rank = ranks[0] }
    if (getDistance() > 0.1 && getDistance() <= 0.25){ rank = ranks[1] } // S -> 100m-250m
    if (getDistance() > 0.25 && getDistance() <= 0.5){ rank = ranks[2] } // A -> 250m-500m
    if (getDistance() > 0.5 && getDistance() <= 0.75){ rank = ranks[3] } // B -> 500-750m
    if (getDistance() > 0.75 && getDistance() < 1.0){ rank = ranks[4] } // C -> 750-1500m
    if (getDistance() >= 1.0){ rank = ranks[5] } // D -> 1500+m
    return rank
}

@Composable
fun getRank_IMG(rank: String){
    if (rank == "SS"){
        val image: Painter = painterResource(id = R.drawable.ss_big)
        Image(painter = image,contentDescription = "")
    }
    if (rank == "S"){
        val image: Painter = painterResource(id = R.drawable.s_big)
        Image(painter = image,contentDescription = "")
    }
    if (rank == "A"){
        val image: Painter = painterResource(id = R.drawable.a_big)
        Image(painter = image,contentDescription = "")
    }
    if (rank == "B"){
        val image: Painter = painterResource(id = R.drawable.b_big)
        Image(painter = image,contentDescription = "")
    }
    if (rank == "C"){
        val image: Painter = painterResource(id = R.drawable.c_big)
        Image(painter = image,contentDescription = "")
    }
    if (rank == "D"){
        val image: Painter = painterResource(id = R.drawable.d_big)
        Image(painter = image,contentDescription = "")
    }
}

fun getPoints(rank: String, option: String, flag: String): Int{
    var points_modifier = 0.0
    var rank_modifier = 0.0
    var points = 0.0
    if (rank == "SS") { rank_modifier = 1.0 }
    if (rank == "S") { rank_modifier = 0.1666666666666667*5.75 }
    if (rank == "A") { rank_modifier = 0.1666666666666667*4.5 }
    if (rank == "B") { rank_modifier = 0.1666666666666667*3.25 }
    if (rank == "C") { rank_modifier = 0.1666666666666667*2 }
    if (rank == "D") { rank_modifier = 0.0 }
    if (option == "I'M TOO YOUNG TO DIE") { points_modifier = 1.5 }
    if (option == "HEY, NOT TOO ROUGH") { points_modifier = 2.0 }
    if (option == "HURT ME PLENTY") { points_modifier = 2.5 }
    if (option == "ULTRA-VIOLENCE") { points_modifier = 3.0 }
    if (option == "NIGHTMARE") { points_modifier = 3.5 }
    if (option == "THE WORLD") { points_modifier = 5.0 }
    if (option.substringBefore("/") == "CONTINENTAL") { points_modifier = 1.0 }
    if (flag == "current"){
        points = ((points_modifier * getDistance()) * rank_modifier)
    } else if (flag == "total"){
        points = (points_modifier * distance_global)
    }
    return points.toInt()
}

@Composable
fun LogScreen(dbHandler: DBHandler) {
    val data = dbHandler.data
    val points = dbHandler.points
    val gradient = Brush.verticalGradient(
        0.0f to Color.Black,
        1.0f to Color.Red,
        startY = 0.0f,
        endY = 1500.0f
    )
    Box(modifier = Modifier.background(gradient))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        GD_Logo()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 20.sp,
            text = "MISSION LOGS\n\n",
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 30.sp,
            text = "Total Points:",
            color = Color.Yellow,
            textAlign = TextAlign.Center
        )
        Text(
            fontSize = 30.sp,
            text = "$points",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 20.sp,
            text = "$data",
            color = Color.White,
            textAlign = TextAlign.Center
        )

    }
}

fun cheevoChecker(dbHandler: DBHandler, geocoder: Geocoder, time_taken: String){
    // general cheevos
    if (rank_global != "D") { dbHandler.addEntry2("WENT OUTSIDE", dateend_global) }
    if (rank_global == "D") { dbHandler.addEntry2("FISSION MAILED", dateend_global) }
    var i = 0
    // difficulty-based cheevos
    val difficulties = listOf("I'M TOO YOUNG TO DIE", "HEY, NOT TOO ROUGH", "HURT ME PLENTY", "ULTRA-VIOLENCE", "NIGHTMARE", "THE WORLD")
    val difficulty_cheevos = listOf("A LOVELY STROLL THROUGH THE PARK", "UNDERHALLS", "KNEE-DEEP IN THE DEAD", "REVENANT", "INTO THE ABYSS", "OVERDRIVE")
    var cheevo = ""
    for (item in difficulties) {
        cheevo = difficulty_cheevos[i]
        if (selectedOption_global == item && rank_global != "D") {
            dbHandler.addEntry2("$cheevo", dateend_global)
        }
        i++
    }
    // continental cheevos
    /*
    i = 0
    val continents = listOf("AFRICA", "ASIA", "NORTH AMERICA", "SOUTH AMERICA", "AUSTRALIA", "EUROPE")
    val continents_cheevos = listOf()
    cheevo = ""
    for (item in continents) {
        cheevo = continents_cheevos[i]
        if (selectedOption_global == item && rank_global != "D") {
            dbHandler.addEntry2("$cheevo", dateend_global)
        }
        i++
    }
    */
    // time cheevos
    var time_ss = time_taken.substringBefore(",")
    var t_kword = ""
    if (time_ss.substringAfter(" ") == "minutes"){
        t_kword = time_ss.substringBefore(" ")
        if (t_kword.toInt() > 336 && rank_global != "D"){
            dbHandler.addEntry2("I DON'T LIKE THIS GAME THAT MUCH, ANYWAY", dateend_global)
        }
        if (t_kword.toInt() < 24 && selectedOption_global == "THE WORLD" && rank_global != "D"){
            dbHandler.addEntry2("IDCLIP", dateend_global)
        }
    }
    if (time_ss.substringAfter(" ") == "minutes"){
        t_kword = time_ss.substringBefore(" ")
        if (t_kword.toInt() < 5 && selectedOption_global == "I'M TOO YOUNG TO DIE!" && rank_global != "D"){
            dbHandler.addEntry2("SPEEDWALKER", dateend_global)
        }
    }
    // secret cheevos
    if (getDistance_Custom(37.4175, -81.490556) < 0.05) {
        dbHandler.addEntry2("SIGIL CASTER", dateend_global)
    }
    if (getDistance_Custom(41.0, 93.0) < 0.05) {
        dbHandler.addEntry2("MAP REF", dateend_global)
    }
    if (getDistance_Custom(40.109972, -88.216556) < 0.05) {
        dbHandler.addEntry2("NEVER MEANT", dateend_global)
    }
    val addresses = geocoder.getFromLocation(lat, long, 10)
    val address: Address? = addresses?.get(0)
    val zip = address?.postalCode
    if (zip != null) {
        if (zip.toInt() >= 15001 && zip.toInt() <= 19612 && rank_global != "D") { dbHandler.addEntry2("QUAKER", dateend_global) }
        if ((zip.toInt() == 98024 || zip.toInt() == 98065 || zip.toInt() == 98045)) { dbHandler.addEntry2("FIRE WALK WITH ME", dateend_global) }
        if (zip.toInt() == 89001) { dbHandler.addEntry2("FAAIP DE OIAD", dateend_global) }
        if (zip.toInt() >= 73301 && zip.toInt() <= 88595 && rank_global != "D") { dbHandler.addEntry2("KING OF THE HILL", dateend_global) }
        if (zip.toInt() >= 87101 && zip.toInt() <= 87199 && rank_global != "D") { dbHandler.addEntry2("FELINA", dateend_global) }
    }
}

@Composable
fun CheevoScreen(dbHandler: DBHandler) {
    val cheevos_n = dbHandler.cheevos_Name
    val cheevos_list_n = cheevos_n.split("*")
    val cheevos_d = dbHandler.cheevos_Date
    val cheevos_list_d = cheevos_d.split("*")
    val gradient = Brush.verticalGradient(
        0.0f to Color.Black,
        1.0f to Color.Red,
        startY = 0.0f,
        endY = 1500.0f
    )
    Box(modifier = Modifier.background(gradient))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        GD_Logo()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 20.sp,
            text = "ACHIEVEMENTS\n\n",
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(20.dp))

        var achievement_name = ""
        var achievement_description = ""
        var index = 0
        Text(
            color = Color.Green,
            text = "Gameplay",
            fontSize = 40.sp,
        )
        Spacer(modifier = Modifier.height(20.dp))
        val imgm = Modifier
            .border(BorderStroke(2.dp, Color.Black))
        // Source: https://stackoverflow.com/questions/67818155/how-to-change-color-image-into-black-white-in-jetpack-compose
        val matrix = ColorMatrix()
        matrix.setToSaturation(0F)
        val achievement_pics_generic = arrayOf(R.drawable.went_outside,R.drawable.fission_mailed,R.drawable.toki_wo_tomare)
        val achievement_names_generic: List<String> = listOf("WENT OUTSIDE", "FISSION MAILED", "TOKI WO TOMARE")
        val achievement_descriptions_generic: List<String> = listOf("Successfully complete a mission on any difficulty.",
            "Fail a mission.", "Unlock the secret 'THE WORLD' difficulty."
        )
        while (index < achievement_names_generic.size) {
            achievement_name = achievement_names_generic[index]
            achievement_description = achievement_descriptions_generic[index]
            if (achievement_name in cheevos_list_n) {
                Image(painter = painterResource(id = achievement_pics_generic[index]),modifier=imgm,contentDescription = "")
            } else {
                Image(painter = painterResource(id = achievement_pics_generic[index]),colorFilter = ColorFilter.colorMatrix(matrix),modifier=imgm,contentDescription = "")
            }
            CheevoComp(achievement_name, achievement_description, cheevos_list_n, cheevos_list_d)
            index++
        }

        val achievement_pics_diffs = arrayOf(R.drawable.a_lovely_stroll,R.drawable.underhalls,R.drawable.knee_deep,
            R.drawable.revenant,R.drawable.into_the_abyss,R.drawable.overdrive)
        val achievement_names_diffs: List<String> = listOf("A LOVELY STROLL THROUGH THE PARK", "UNDERHALLS",
            "KNEE-DEEP IN THE DEAD", "REVENANT", "INTO THE ABYSS", "OVERDRIVE")
        val achievement_descriptions_diffs: List<String> = listOf("Complete a mission on 'I'M TOO YOUNG TO DIE' difficulty.",
            "Complete a mission on 'HEY, NOT TOO ROUGH' difficulty.",
            "Complete a mission on 'HURT ME PLENTY' difficulty.",
            "Complete a mission on 'ULTRA-VIOLENCE' difficulty.",
            "Complete a mission on 'NIGHTMARE' difficulty.",
            "Complete a mission on 'THE WORLD' difficulty.",
        )
        Text(
            color = Color.Green,
            text = "Difficulties",
            fontSize = 40.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        index = 0
        while (index < achievement_names_diffs.size) {
            achievement_name = achievement_names_diffs[index]
            achievement_description = achievement_descriptions_diffs[index]
            if (achievement_name in cheevos_list_n) {
                Image(painter = painterResource(id = achievement_pics_diffs[index]),modifier=imgm,contentDescription = "")
            } else {
                Image(painter = painterResource(id = achievement_pics_diffs[index]),colorFilter = ColorFilter.colorMatrix(matrix),modifier=imgm,contentDescription = "")
            }
            CheevoComp(achievement_name, achievement_description, cheevos_list_n, cheevos_list_d)
            index++
        }


        val achievement_pics_time = arrayOf(R.drawable.i_dont_like,R.drawable.speedwalker,R.drawable.idclip)
        val achievement_names_time: List<String> = listOf("I DON'T LIKE THIS GAME THAT MUCH, ANYWAY","SPEEDWALKER","IDCLIP")
        val achievement_descriptions_time: List<String> = listOf("Take 2 weeks to complete a mission.",
            "Complete a mission on 'I'M TOO YOUNG TO DIE!' difficulty in under 5 minutes.",
            "Complete a mission on 'THE WORLD' difficulty in under 24 hours."
        )
        Text(
            color = Color.Green,
            text = "Time",
            fontSize = 40.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        index = 0
        while (index < achievement_names_time.size) {
            achievement_name = achievement_names_time[index]
            achievement_description = achievement_descriptions_time[index]
            if (achievement_name in cheevos_list_n) {
                Image(painter = painterResource(id = achievement_pics_time[index]),modifier=imgm,contentDescription = "")
            } else {
                Image(painter = painterResource(id = achievement_pics_time[index]),colorFilter = ColorFilter.colorMatrix(matrix),modifier=imgm,contentDescription = "")
            }
            CheevoComp(achievement_name, achievement_description, cheevos_list_n, cheevos_list_d)
            index++
        }
        
        Text(
            color = Color.Green,
            text = "Secrets",
            fontSize = 40.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        val achievement_pics_secrets = arrayOf(R.drawable.quaker,R.drawable.fwwm,R.drawable.faaip_de_oiad,
            R.drawable.never_meant,R.drawable.koth,R.drawable.felina,R.drawable.map_ref,
            R.drawable.sigil_caster)
        val achievement_names_secrets: List<String> = listOf("QUAKER","FIRE WALK WITH ME","FAAIP DE OIAD",
            "NEVER MEANT","KING OF THE HILL","FELINA","MAP REF","SIGIL CASTER")
        val achievement_descriptions_secrets: List<String> = listOf("We can't stop here, this is Amish country!",
            "BOB is here.","Do you see what I see?","But that's life. It's so social.","What's so great about dumb ol' Texas?",
            "Guess I got what I deserved.","Witness the sinking of the Sun.","Come and see..."
        )
        index = 0
        while (index < achievement_names_secrets.size) {
            achievement_name = achievement_names_secrets[index]
            achievement_description = achievement_descriptions_secrets[index]
            if (achievement_name in cheevos_list_n) {
                Image(painter = painterResource(id = achievement_pics_secrets[index]),modifier=imgm,contentDescription = "")
            } else {
                Image(painter = painterResource(id = achievement_pics_secrets[index]),colorFilter = ColorFilter.colorMatrix(matrix),modifier=imgm,contentDescription = "")
            }
            CheevoComp(achievement_name, achievement_description, cheevos_list_n, cheevos_list_d)
            index++
        }
    }
}

@Composable
fun CheevoComp(achievement_name: String, achievement_description: String, cheevos_list_n: List<String>, cheevos_list_d: List<String>){
    var index = 0
    var date = ""
    var ach_msg = ""
    if (achievement_name in cheevos_list_n){
        index = cheevos_list_n.indexOf(achievement_name)
        date = cheevos_list_d[index].substringBefore(" ")
        ach_msg = "Achieved on $date"
    }
    Text(
        color = Color.Yellow,
        text = "$achievement_name",
        fontSize = 24.sp,
        textAlign = TextAlign.Center
    )
    Text(
        color = Color.White,
        text = "$achievement_description",
        fontSize = 20.sp,
        textAlign = TextAlign.Center
    )
    Text(
        color = Color.Green,
        text = "$ach_msg",
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
    Text(
        text = "\n\n"
    )
}

@Composable
fun AboutScreen() {
    val gradient = Brush.verticalGradient(
        0.0f to Color.Black,
        1.0f to Color.Red,
        startY = 0.0f,
        endY = 1500.0f
    )
    Box(modifier = Modifier.background(gradient))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        GD_Logo()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 30.sp,
            text = "WHAT IS GEODOOM\n\n",
            color = Color.Yellow,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 20.sp,
            text = "GeoDoom is a game that rewards with meaningless points for exploring real-world locations.\n\n" +
                    "There are 5 difficulties in GeoDoom, each generating a random location some distance away from the player's current real-world location:\n",
            color = Color.White
        )
        Text(fontSize = 20.sp,
            text = "I'M TOO YOUNG TO DIE - 2-5km\n" +
                    "HEY, NOT TOO ROUGH - 5-50km\n " +
                    "HURT ME PLENTY - 50-250km\n" +
                    "ULTRA-VIOLENCE - 250-1000km\n" +
                    "NIGHTMARE - 1000km+\n",
            color = Color.White,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        Text(
            fontSize = 20.sp,
            text = "\nThe aim of each mission is to get as close as possible to the generated location in order to recieve points.\n\n" +
                    "The player's final location in comparison to the exact generated location will be interpreted through a rank system and act as a modifier towards the final number of points for each mission.\n\n",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(fontSize = 20.sp,
            text = "SS RANK - Less than 100m away from target\n" +
                    "S RANK - 100-250m away from target\n" +
                    "A RANK - 250-500m away from target\n" +
                    "B RANK - 500-750m away from target\n" +
                    "C RANK - 750-1000m away from target\n" +
                    "D RANK - 1000m+ away from target\n",
            color = Color.White,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        Text(
            fontSize = 30.sp,
            text = "TROUBLESHOOTING/\n\nHELP\n\n",
            color = Color.Yellow,
            textAlign = TextAlign.Center
        )
        Text(
            fontSize = 20.sp,
            text = "My location is showing as 0.0, 0.0 when generating coordinates!\n\n",
            color = Color.White,
            textAlign = TextAlign.Center,
            fontStyle = (androidx.compose.ui.text.font.FontStyle.Italic)
        )
        Text(
            fontSize = 20.sp,
            text = "Solution:\n-Make sure you have a good mobile connection\n-Spam the Generate button until " +
                    "your current location appears\n\n",
            color = Color.White,
            textAlign = TextAlign.Center,
            fontStyle = (androidx.compose.ui.text.font.FontStyle.Italic)
        )
        Text(
            fontSize = 20.sp,
            text = "My location isn't updating when I hit the Update Location button!\n\n",
            color = Color.White,
            textAlign = TextAlign.Center,
            fontStyle = (androidx.compose.ui.text.font.FontStyle.Italic)
        )
        Text(
            fontSize = 20.sp,
            text = "Solution:\n-Make it to where Google Maps has access to your location\n" +
                    "-Switch from GeoDoom to Google Maps\n" + "-Wait until the gray dot showing your" +
                    " location turns blue.\n\n" ,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

fun getDate(): String {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}

@Composable
fun AcceptButton(navController: NavController, random_lat: Double, random_long: Double, selectedOption: String, distance: Double, current_location: String){
    OutlinedButton(
        onClick = {
            datestart_global = getDate()
            accept_chk = true
            random_lat_global = random_lat
            random_long_global = random_long
            selectedOption_global = selectedOption
            distance_global = distance
            currlat_global = lat
            currlong_global = long
            navController.navigate(Screen.MainScreen.route)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White)
    ) {
        Text(text = "ACCEPT", fontSize = 30.sp)
    }
}

@Composable
fun MapsButton(random_lat: Double, random_long: Double){
    var maps_confirm by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = {
            maps_confirm = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White)
    ) {
        Text(text = "OPEN IN MAPS", fontSize = 30.sp,textAlign = TextAlign.Center)
    }
    if(maps_confirm){
        OpenInMaps(random_lat, random_long)
    }
    maps_confirm = false
}

// Source: https://stackoverflow.com/questions/54660224/android-kotlin-open-link-with-button
@Composable
fun OpenInMaps(random_lat: Double, random_long: Double){
    var url: String = "https://www.google.com/maps/place/" + random_lat + "," + random_long
    var parsedUrl: Uri? = null
    parsedUrl = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = parsedUrl
    LocalContext.current.startActivity(intent)
}

fun generateCoords(lat: Double, long: Double, lat_long_chk: Int, selectedOption: String): Double {
    var random_lat: Double = 0.0
    var random_long: Double = 0.0
    var result = 0.0000000
    if (gp_option_global == "STANDARD") {
        val range_1_start = 0.01 // 2km
        val range_1_end = 0.045 // 5 km
        val range_2_start = range_1_end + 0.001
        val range_2_end = 0.446 // 50km
        val range_3_start = range_2_end + 0.001
        val range_3_end = 2.25 // 250km
        val range_4_start = range_3_end + 0.001
        val range_4_end = 9 // 1000km
        val range_5_start = range_4_end + 0.001
        val range_5_end = 31.5 // 3500km

        val range_6_start = range_5_end + 0.001
        val range_6_end_lat = 45.0
        val range_6_end_long = 90.0

        var modlat = 1.0
        var modlong = 1.0
        if (range_option_global == "H RECTANGLE") {
            modlat = 0.5
            modlong = 2.0
        }
        if (range_option_global == "V RECTANGLE") {
            modlat = 2.0
            modlong = 0.5
        }

        if (lat_long_chk == 0) {
            var random_lat_N_S = (1..2).random()
            if (random_lat_N_S == 1) {
                if (selectedOption == "I'M TOO YOUNG TO DIE") {
                    random_lat =
                        lat + (Random.nextFloat() * (range_1_end * modlat - range_1_start * modlat) + range_1_start * modlat)
                } else if (selectedOption == "HEY, NOT TOO ROUGH") {
                    random_lat =
                        lat + (Random.nextFloat() * (range_2_end * modlat - range_2_start * modlat) + range_2_start * modlat)
                } else if (selectedOption == "HURT ME PLENTY") {
                    random_lat =
                        lat + (Random.nextFloat() * (range_3_end * modlat - range_3_start * modlat) + range_3_start * modlat)
                } else if (selectedOption == "ULTRA-VIOLENCE") {
                    random_lat =
                        lat + (Random.nextFloat() * (range_4_end * modlat - range_4_start * modlat) + range_4_start * modlat)
                } else if (selectedOption == "NIGHTMARE") {
                    random_lat =
                        lat + (Random.nextFloat() * (range_5_end * modlat - range_5_start * modlat) + range_5_start * modlat)
                } else if (selectedOption == "THE WORLD") {
                    random_lat =
                        lat + (Random.nextFloat() * (range_6_end_lat * modlat - range_6_start * modlat) + range_6_start * modlat)
                }
            } else if (random_lat_N_S == 2) {
                if (selectedOption == "I'M TOO YOUNG TO DIE") {
                    random_lat =
                        lat - (Random.nextFloat() * (range_1_end * modlat - range_1_start * modlat) + range_1_start * modlat)
                } else if (selectedOption == "HEY, NOT TOO ROUGH") {
                    random_lat =
                        lat - (Random.nextFloat() * (range_2_end * modlat - range_2_start * modlat) + range_2_start * modlat)
                } else if (selectedOption == "HURT ME PLENTY") {
                    random_lat =
                        lat - (Random.nextFloat() * (range_3_end * modlat - range_3_start * modlat) + range_3_start * modlat)
                } else if (selectedOption == "ULTRA-VIOLENCE") {
                    random_lat =
                        lat - (Random.nextFloat() * (range_4_end * modlat - range_4_start * modlat) + range_4_start * modlat)
                } else if (selectedOption == "NIGHTMARE") {
                    random_lat =
                        lat - (Random.nextFloat() * (range_5_end * modlat - range_5_start * modlat) + range_5_start * modlat)
                } else if (selectedOption == "THE WORLD") {
                    random_lat =
                        lat - (Random.nextFloat() * (range_6_end_lat * modlat - range_6_start * modlat) + range_6_start * modlat)
                }
            }
            result = random_lat
            if (result > 90.0) {
                result -= 90.0
                result = 90.0 - result
                result *= -1
            }
            if (result < -90.0) {
                result += 90.0
                result *= -1
                result = 90.0 - result
            }
        } else if (lat_long_chk == 1) {
            var random_long_E_W = (1..2).random()
            if (random_long_E_W == 1) {
                if (selectedOption == "I'M TOO YOUNG TO DIE") {
                    random_long =
                        long + (Random.nextFloat() * (range_1_end * modlong - range_1_start * modlong) + range_1_start * modlong)
                } else if (selectedOption == "HEY, NOT TOO ROUGH") {
                    random_long =
                        long + (Random.nextFloat() * (range_2_end * modlong - range_2_start * modlong) + range_2_start * modlong)
                } else if (selectedOption == "HURT ME PLENTY") {
                    random_long =
                        long + (Random.nextFloat() * (range_3_end * modlong - range_3_start * modlong) + range_3_start * modlong)
                } else if (selectedOption == "ULTRA-VIOLENCE") {
                    random_long =
                        long + (Random.nextFloat() * (range_4_end * modlong - range_4_start * modlong) + range_4_start * modlong)
                } else if (selectedOption == "NIGHTMARE") {
                    random_long =
                        long + (Random.nextFloat() * (range_5_end * modlong - range_5_start * modlong) + range_5_start * modlong)
                } else if (selectedOption == "THE WORLD") {
                    random_long =
                        long + (Random.nextFloat() * (range_6_end_long * modlong - range_6_start * modlong) + range_6_start * modlong)
                }
            } else if (random_long_E_W == 2) {
                if (selectedOption == "I'M TOO YOUNG TO DIE") {
                    random_long =
                        long - (Random.nextFloat() * (range_1_end * modlong - range_1_start * modlong) + range_1_start * modlong)
                } else if (selectedOption == "HEY, NOT TOO ROUGH") {
                    random_long =
                        long - (Random.nextFloat() * (range_2_end * modlong - range_2_start * modlong) + range_2_start * modlong)
                } else if (selectedOption == "HURT ME PLENTY") {
                    random_long =
                        long - (Random.nextFloat() * (range_3_end * modlong - range_3_start * modlong) + range_3_start * modlong)
                } else if (selectedOption == "ULTRA-VIOLENCE") {
                    random_long =
                        long - (Random.nextFloat() * (range_4_end * modlong - range_4_start * modlong) + range_4_start * modlong)
                } else if (selectedOption == "NIGHTMARE") {
                    random_long =
                        long - (Random.nextFloat() * (range_5_end * modlong - range_5_start * modlong) + range_5_start * modlong)
                } else if (selectedOption == "THE WORLD") {
                    random_long =
                        long - (Random.nextFloat() * (range_6_end_long * modlong - range_6_start * modlong) + range_6_start * modlong)
                }
            }
            result = random_long
            if (result > 180.0) {
                result -= 180.0
                result = 180.0 - result
                result *= -1
            }
            if (result < -180.0) {
                result += 180.0
                result *= -1
                result = 180.0 - result
            }
        }
    } else if (gp_option_global == "CONTINENTAL"){
        if (lat_long_chk == 0){
            if (selectedOption == "CONTINENTAL/AFRICA"){
                random_lat = (Random.nextFloat() * (35.5 - -35.5) + -35.5)
            }
            if (selectedOption == "CONTINENTAL/ASIA"){
                random_lat = (Random.nextFloat() * (77.0 - -10.0) + -10.0)
            }
            if (selectedOption == "CONTINENTAL/AUSTRALIA"){
                random_lat = (Random.nextFloat() * (-10 - -45.0) + -45.0)
            }
            if (selectedOption == "CONTINENTAL/EUROPE"){
                random_lat = (Random.nextFloat() * (72.0 - 35.5) + 35.5)
            }
            if (selectedOption == "CONTINENTAL/NORTH AMERICA"){
                random_lat = (Random.nextFloat() * (75.0 - 10.0) + 10.0)
            }
            if (selectedOption == "CONTINENTAL/SOUTH AMERICA"){
                random_lat = (Random.nextFloat() * (10.0 - -55.0) + -55.0)
            }
            result = random_lat
        }
        if (lat_long_chk == 1){
            if (selectedOption == "CONTINENTAL/AFRICA"){
                random_long = (Random.nextFloat() * (50.0 - -15.0) + -15.0)
            }
            if (selectedOption == "CONTINENTAL/ASIA"){
                random_long = (Random.nextFloat() * (180.0 - 60.0) + 60.0)
            }
            if (selectedOption == "CONTINENTAL/AUSTRALIA"){
                random_long = (Random.nextFloat() * (175.0 - 115.0) + 115.0)
            }
            if (selectedOption == "CONTINENTAL/EUROPE"){
                random_long = (Random.nextFloat() * (60.0 - -30.0) + -30.0)
            }
            if (selectedOption == "CONTINENTAL/NORTH AMERICA"){
                random_long = (Random.nextFloat() * (-60.0 - -175.0) + -175.0)
            }
            if (selectedOption == "CONTINENTAL/SOUTH AMERICA"){
                random_long = (Random.nextFloat() * (-81.0 - -33.0) + -33.0)
            }

            result = random_long
        }
    }

    val df = DecimalFormat("#.#######")
    df.roundingMode = RoundingMode.CEILING
    return df.format(result).toDouble()
}

fun getLastKnownLocation() {
    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location->
            if (location != null) {
                lat = location.latitude
                long = location.longitude
            }
        }
}

@Composable
fun SettingsScreen(navController: NavController) {
    val gameplay_options = mutableListOf("STANDARD", "CONTINENTAL")
    val (selectedOption_gp, onOptionSelected_gp) = remember { mutableStateOf(gameplay_options[gameplay_options.indexOf(gp_option_global)] ) }
    val range_options = mutableListOf("SQUARE", "H RECTANGLE", "V RECTANGLE")
    val (selectedOption_ro, onOptionSelected_ro) = remember { mutableStateOf(range_options[range_options.indexOf(range_option_global)] ) }
    val gradient = Brush.verticalGradient(
        0.0f to Color.Black,
        1.0f to Color.Red,
        startY = 0.0f,
        endY = 1500.0f
    )
    Box(modifier = Modifier.background(gradient))
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())) {
        GD_Logo()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 20.sp,
            text = "SETTINGS\n\n",
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 30.sp,
            text = "Gameplay Modes:",
            color = Color.Yellow,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        gameplay_options.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (text == selectedOption_gp),
                        onClick = {
                            onOptionSelected_gp(text)
                        }
                    )
                    .padding(horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = (text == selectedOption_gp),
                    onClick = {
                        onOptionSelected_gp(text)
                    },
                )
                gp_option_global = selectedOption_gp
                Text(
                    text = text,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        var info = "These options change the way GeoDoom will be played.\n\n" +
                "STANDARD: Generate coordinates based on separate ranges between 5 difficulties\n" +
                "*CONTINENTAL: Generate coordinates in one of six continents\n"
        Text(
            fontSize = 14.sp,
            text = "$info",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            fontSize = 8.sp,
            text = "*(Currently no achievements)",
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 30.sp,
            text = "Coordinate Range\n\nModes:",
            color = Color.Yellow,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        range_options.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (text == selectedOption_ro),
                        onClick = {
                            onOptionSelected_ro(text)
                        }
                    )
                    .padding(horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = (text == selectedOption_ro),
                    onClick = {
                        onOptionSelected_ro(text)
                              },
                )
                range_option_global = selectedOption_ro
                Text(
                    text = text,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            fontSize = 14.sp,
            text = "These options change the way coordinates will be generated.\n\n" +
                    "SQUARE: Latitude/Longitude are the same range\n" +
                    "H RECTANGLE: Latitude range is halved, longitude range is doubled\n" +
                    "V RECTANGLE: Latitude range is doubled, longitude range is halved\n\n" +
                    "If playing on higher difficulties from a location close to the sea, " +
                    "using either Rectangle mode is best.",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}