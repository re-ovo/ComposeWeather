package me.rerere.composeweather.ui.route

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.rerere.composeweather.WeatherViewModel
import me.rerere.composeweather.model.Area
import me.rerere.composeweather.util.WindUtil
import java.util.*

@ExperimentalFoundationApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Index(navController: NavController, weatherViewModel: WeatherViewModel) {
    val weathers by weatherViewModel.weatherData.observeAsState(emptyList())
    val pagerState = rememberPagerState(weathers.size)

    if (weathers.isEmpty()) {
        return
    }

    val currentArea = weathers[pagerState.currentPage].weather
    val title = if(currentArea != null) "${currentArea.location.region} - ${currentArea.location.name}" else "?????????"

    Scaffold(
        topBar = {
            WeatherTopBar(title, navController)
        }
    ) {
        WeatherInfo(weatherViewModel, weathers, pagerState)
    }
}

@ExperimentalFoundationApi
@ExperimentalPagerApi
@Composable
fun WeatherInfo(weatherViewModel: WeatherViewModel, weathers: List<Area>, pagerState: PagerState) {
    if (weathers.isNotEmpty()) {
        Column {
            if(weathers.size > 1) {

                // Pager ?????????
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    val scope = rememberCoroutineScope()
                    weathers.forEach {
                        Tab(
                            modifier = Modifier.height(40.dp),
                            selected = it == weathers[pagerState.currentPage],
                            onClick = {
                                // ?????????????????????Page
                                scope.launch { pagerState.animateScrollToPage(weathers.indexOf(it)) }
                            }) {
                            Row(verticalAlignment = CenterVertically) {
                                Text(it.name)
                                if (it.isCurrentLocation) {
                                    Icon(
                                        modifier = Modifier
                                            .size(25.dp)
                                            .padding(horizontal = 4.dp),
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalPager(modifier = Modifier.fillMaxSize(), state = pagerState) { page ->
                val weather = weathers[page].weather
                if (weather != null) {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(weatherViewModel.isRefreshing),
                        onRefresh = {
                            weatherViewModel.update(weathers[page])
                        }) {
                        LazyColumn(Modifier.fillMaxSize()) {
                            // ????????????
                            stickyHeader {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp), contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = "????????????",
                                        style = TextStyle.Default.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    )
                                }
                            }
                            item {
                                Card(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    elevation = 2.dp
                                ) {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            Modifier
                                                .padding(16.dp)
                                        ) {
                                            Image(
                                                modifier = Modifier.size(100.dp),
                                                painter = rememberCoilPainter("https:" + weather.current.condition.icon),
                                                contentDescription = "ICON"
                                            )
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "${weather.current.tempC} ??? ${weather.current.condition.text}",
                                                    style = TextStyle.Default.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 30.sp
                                                    )
                                                )


                                                Row(Modifier.padding(vertical = 8.dp)) {
                                                    // ???
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = WindUtil.windSpeedToWindLevel(
                                                                weather.current.windKph * 1000f / 3600f
                                                            )
                                                        )
                                                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                            Text(
                                                                WindUtil.windAngleToWindDirection(
                                                                    weather.current.windDegree
                                                                ),
                                                                maxLines = 1,
                                                                fontSize = 12.sp
                                                            )
                                                        }
                                                    }
                                                    // ??????
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text("${weather.current.humidity}")
                                                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                            Text("??????", fontSize = 12.sp)
                                                        }
                                                    }
                                                    // ????????????
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text("${weather.current.feelslikeC} ???")
                                                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                            Text("??????", fontSize = 12.sp)
                                                        }
                                                    }
                                                    // ???????????????
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text("${weather.current.uv}")
                                                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                            Text("?????????", fontSize = 12.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ????????????
                            stickyHeader {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp), contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = "???????????????",
                                        style = TextStyle.Default.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }

                            items(weather.forecast.forecastday) {
                                Card(
                                    Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Box(modifier = Modifier.padding(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                modifier = Modifier.size(40.dp),
                                                painter = rememberCoilPainter("https:" + it.day.condition.icon),
                                                contentDescription = null
                                            )
                                            Column(Modifier.padding(8.dp)) {
                                                Text(
                                                    "${it.day.avgtempC} ??? ${it.day.condition.text}",
                                                    style = TextStyle.Default.copy(fontWeight = FontWeight.Bold)
                                                )
                                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                    Text(it.date.substring(5))
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                            // ??????????????????
                            stickyHeader {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp), contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = "????????????",
                                        style = TextStyle.Default.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }
                            item {
                                val state = rememberLazyListState()
                                LaunchedEffect(weather.forecast.forecastday.first().hour) {
                                    delay(500)
                                    // ?????????????????????
                                    state.animateScrollToItem(Date().hours)
                                }
                                LazyRow(state = state) {
                                    items(weather.forecast.forecastday.first().hour) {
                                        Card(Modifier.padding(8.dp)) {
                                            Box(modifier = Modifier.padding(8.dp)) {
                                                Column(verticalArrangement = Arrangement.Center) {
                                                    Text(
                                                        text = it.time.substring(11),
                                                        style = TextStyle.Default.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp
                                                        )
                                                    )
                                                    Row {
                                                        Image(
                                                            rememberCoilPainter("https:" + it.condition.icon),
                                                            null
                                                        )
                                                        Text(it.condition.text)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Image(modifier = Modifier.size(50.dp), painter = rememberCoilPainter("https://cdn.weatherapi.com/v4/images/weatherapi_logo.png"), contentDescription = null)
                                        Text(text = "???????????? weatherapi.com")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("?????????????????????")
        }
    }
}

@ExperimentalPagerApi
@Composable
fun WeatherTopBar(title: String, navController: NavController) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate("area")
            }) {
                Icon(Icons.Default.Add, null)
            }
        },
        title = {
            Text(title)
        },
        actions = {
            IconButton(onClick = {

            }) {
                Icon(Icons.Default.MenuOpen, null)
            }
        },
        elevation = 0.dp
    )
}