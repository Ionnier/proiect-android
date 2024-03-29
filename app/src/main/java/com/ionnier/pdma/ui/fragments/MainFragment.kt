package com.ionnier.pdma.ui.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.ionnier.pdma.IntentReceiver
import com.ionnier.pdma.R
import com.ionnier.pdma.Settings
import com.ionnier.pdma.data.AppDatabase
import com.ionnier.pdma.ui.colors.MyApplicationTheme
import com.ionnier.pdma.ui.screens.DrawSettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.internal.managers.ViewComponentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


private const val HOME_ROUTE = "Home"
private const val LOG_ROUTE = "History"
private const val SETTINGS_ROUTE = "Settings"

@AndroidEntryPoint
class MainFragment : Fragment() {
    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = if (context is ViewComponentManager.FragmentContextWrapper)
            (context as ViewComponentManager.FragmentContextWrapper).baseContext
        else
            context
        return ComposeView(context!!).apply {
            setContent {
                MyApplicationTheme {
                    val currentSelectedRoute = rememberSaveable {
                        mutableStateOf(HOME_ROUTE)
                    }
                    val scope = rememberCoroutineScope()
                    val windowSize = activity?.let { calculateWindowSizeClass(it) }
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val drawerItems = listOf(
                        Pair(HOME_ROUTE, Icons.Outlined.Home),
                        Pair(LOG_ROUTE, Icons.Outlined.List),
                        Pair(SETTINGS_ROUTE, Icons.Outlined.Settings)
                    )
                    fun closeDrawer() {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                    BackHandler {
                        if (drawerState.isOpen) {
                            closeDrawer()
                        } else {
                            if (currentSelectedRoute.value != HOME_ROUTE) {
                                currentSelectedRoute.value = HOME_ROUTE
                            } else {
                                activity?.finishAndRemoveTask()

                            }
                        }
                    }
                    val dialogValue = mainViewModel.showDialog.collectAsState().value
                    if (dialogValue != 0) {
                        Surface {
                            val dismiss = {
                                mainViewModel.showDialog.update {
                                    0
                                }
                            }
                            Dialog(
                                onDismissRequest = {
                                    dismiss()
                                }
                            ) {
                                when (dialogValue) {
                                    1 -> ReminderDialog(dismiss)
                                    2 -> CaloriesDialog(dismiss = dismiss)
                                }
                            }
                        }
                    }
                    if (windowSize?.widthSizeClass == WindowWidthSizeClass.Compact) {
                        ModalNavigationDrawer(
                            drawerContent = {
                                Column(
                                    modifier = Modifier.alpha(
                                        if (drawerState.isOpen || drawerState.isAnimationRunning || drawerState.offset.value != 0f) 1.0f else 0.0f
                                    )
                                ) {
                                    TopAppBar(
                                        title = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(0.0f)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                closeDrawer()
                                            },
                                    )
                                    for (item in drawerItems) {
                                        NavigationDrawerItem(
                                            label = {
                                                DrawerItem(item)
                                            },
                                            selected = currentSelectedRoute.value == item.first,
                                            onClick = {
                                                currentSelectedRoute.value = item.first
                                                closeDrawer()
                                            },
                                            shape = RectangleShape
                                        )

                                    }
                                }
                            },
                            drawerState = drawerState,
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column {
                                    TopAppBar(
                                        title = {
                                            Text(currentSelectedRoute.value)
                                        },
                                        actions = {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        drawerState.open()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Menu,
                                                    tint = Color.White,
                                                    contentDescription = null,
                                                )
                                            }
                                        }
                                    )
                                    MainContent(currentSelectedRoute)
                                }

                            }

                        }
                    }
                    else {
                        Row(

                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                for (item in drawerItems) {
                                    DrawerItem(
                                        item = item,
                                        modifier = Modifier.clickable {
                                            currentSelectedRoute.value = item.first
                                        }.then (if (currentSelectedRoute.value == item.first) {
                                            Modifier.background(Color.Gray)
                                        } else {
                                            Modifier
                                        }.fillMaxWidth()
                                        )
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(3f)
                            ) {
                                MainContent(currentSelectedRoute)
                            }
                        }

                    }
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun ComposeView.MainContent(currentSelectedRoute: MutableState<String>) {
        when (currentSelectedRoute.value) {
            SETTINGS_ROUTE -> DrawSettingsScreen(
                recreate = {
                    activity?.recreate()
                },
                scheduleReminder = {
                    mainViewModel.showDialog.update {
                        1
                    }
                },
                setCalories = {
                    mainViewModel.showDialog.update {
                        2
                    }
                },
                changeLanguage = {
                    if (findNavController().currentDestination?.label == "MainFragment") {
                        findNavController().navigate(
                            MainFragmentDirections.actionMainFragmentToIntroFragment()
                        )
                    }
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    findNavController().popBackStack(R.id.introFragment, false)
                }
            )
            HOME_ROUTE -> {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                if (findNavController().currentDestination?.label == "MainFragment") {
                                    findNavController().navigate(
                                        MainFragmentDirections.actionMainFragmentToAddFragment()
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null
                            )
                        }
                    }
                ) {
                    val items =
                        mainViewModel.getTodayAll().collectAsState(
                            initial = emptyList()
                        ).value
                    val all = items.map { it.ingredient }
                    val totalIngredient = Ingredient(
                        name = null,
                        energy = all.sumOf { it.energy ?: 0 },
                        protein = all.sumOf {
                            try {
                                Integer.parseInt(it.protein!!.split(".")[0])
                            } catch (e: Exception) {
                                0
                            }
                        }.toString(),
                        carbohydrates = all.sumOf {
                            try {
                                Integer.parseInt(
                                    it.carbohydrates!!.split(
                                        "."
                                    )[0]
                                )
                            } catch (e: Exception) {
                                0
                            }
                        }.toString(),
                        fat = all.sumOf {
                            try {
                                Integer.parseInt(it.fat!!.split(".")[0])
                            } catch (e: Exception) {
                                0
                            }
                        }.toString(),
                    )
                    Surface {
                        Column(
                            modifier = Modifier.padding(it),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            if (Settings.goal_calories != 0) {
                                CaloriesCircle(
                                    caloriesConsumed = animateIntAsState(
                                        targetValue = totalIngredient.energy
                                            ?: 0,
                                        animationSpec = tween(2000)
                                    ).value,
                                    goalCalories = Settings.goal_calories
                                )
                            }
                            IngredientListItem(
                                it = totalIngredient,
                                trailingContent = {},
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                }
            }
            LOG_ROUTE -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    val items = mainViewModel.getAll()
                        .collectAsState(initial = emptyList()).value
                    for (item in items) {
                        IngredientListItem(
                            it = item.ingredient,
                            supportingText = {
                                Text(
                                    SimpleDateFormat(
                                        "E, dd LLLL yyyy",
                                        Locale.getDefault()
                                    ).format(item.addedAt)
                                )
                            },
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    item.photo?.let {
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                item.photo
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(128.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val sendIntent: Intent =
                                                Intent().apply {
                                                    action =
                                                        Intent.ACTION_SEND
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "Hey!\n I ate on ${
                                                            SimpleDateFormat(
                                                                "E, dd LLLL yyyy",
                                                                Locale.getDefault()
                                                            ).format(
                                                                item.addedAt
                                                            )
                                                        } some ${item.ingredient.name} it had ${item.ingredient.energy} calories."
                                                    )
                                                    type = "text/plain"
                                                }

                                            val shareIntent =
                                                Intent.createChooser(
                                                    sendIntent,
                                                    null
                                                )
                                            startActivity(shareIntent)

                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            }
                        )
                    }

                }
            }
            else -> {

            }
        }
    }

    @Composable
    private fun DrawerItem(item: Pair<String, ImageVector>, modifier: Modifier = Modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Icon(
                imageVector = item.second,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(item.first)
        }
    }

    @Composable
    private fun ReminderDialog(dismiss: () -> Unit) {
        val time = Calendar.getInstance().time

        val hours = remember {
            mutableStateOf(time.hours)
        }
        val minutes = remember {
            mutableStateOf(time.minutes)
        }
        val seconds = remember {
            mutableStateOf(time.seconds)
        }
        Surface(
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Choose time", modifier = Modifier.fillMaxWidth())
                Row {
                    GenerateRow(value = hours)
                    GenerateRow(value = minutes, minValue = 0, maxValue = 60)
                    GenerateRow(value = seconds, minValue = 0, maxValue = 60)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            val alarmManager =
                                context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                            // Set the alarm to start at 8:30 a.m.
                            val calendar: Calendar =
                                Calendar.getInstance().apply {
                                    timeInMillis = System.currentTimeMillis()
                                    set(Calendar.HOUR_OF_DAY, hours.value)
                                    set(Calendar.MINUTE, minutes.value)
                                    set(Calendar.SECOND, seconds.value)
                                }

                            val intent =
                                Intent(context, IntentReceiver::class.java)
                            intent.putExtra("myAction", "notify")
                            val pendingIntent =
                                PendingIntent.getBroadcast(
                                    context,
                                    0,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )

                            alarmManager?.let {
                                it.setRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    0,
                                    pendingIntent
                                )
                                dismiss()
                            }
                        }
                    ) {
                        Text(text = "Set reminder")
                    }

                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CaloriesDialog(dismiss: () -> Unit) {
        val time = Calendar.getInstance().time
        Surface(
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Set calories", modifier = Modifier.fillMaxWidth())
                val text = "You can use this calculator to get an estimate."
                val highlighted = "calculator"
                ClickableText(
                    text = AnnotatedString(
                        text = text,
                        spanStyles = listOf(
                            AnnotatedString.Range(
                                item = SpanStyle(
                                    color = Color.White,
                                ),
                                start = 0,
                                end = text.length
                            ),
                            AnnotatedString.Range(
                                item = SpanStyle(
                                    color = Color.Blue,
                                    fontStyle = FontStyle.Italic,
                                    textDecoration = TextDecoration.Underline,
                                ),
                                start = text.indexOf(highlighted),
                                end = text.indexOf(highlighted) + highlighted.length
                            )
                        )
                    ),
                    onClick = {
                        val browserIntent =
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.calculator.net/tdee-calculator.html"))
                        startActivity(browserIntent)
                    })
                var textFieldValue by remember {
                    mutableStateOf("")
                }

                var hasError by remember {
                    mutableStateOf("")
                }


                TextField(
                    value = textFieldValue,
                    onValueChange =  {
                        textFieldValue = it
                        hasError = try {
                            val value = Integer.parseInt(textFieldValue)
                            if (value <= 0 ) {
                                throw java.lang.NumberFormatException()
                            }
                            ""
                        } catch (e: java.lang.NumberFormatException) {
                            "Input not valid"
                        }
                    },
                    isError = hasError.isNotBlank(),
                    singleLine = true
                )
                if (hasError.isNotBlank()) {
                    Text(
                        text = hasError,
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }
                OutlinedButton(
                    onClick = {
                        Settings.goal_calories = Integer.parseInt(textFieldValue)
                        dismiss()
                    },
                    enabled = hasError.isBlank() && textFieldValue.isNotBlank(),
                ) {
                    Text(text = "Set calories")
                }
            }
        }
    }

    @Composable
    private fun GenerateRow(
        value: MutableState<Int>,
        maxValue: Int = 23,
        minValue: Int = 0,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = {
                    value.value += 1
                },
                enabled = value.value < maxValue
            ) {
                Icon (
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = null
                )
            }
            Text(
                text = value.value.toString()
            )
            IconButton(
                onClick = {
                    value.value -= 1
                },
                enabled = value.value > minValue
            ) {
                Icon (
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null
                )
            }

        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appDatabase: AppDatabase
): ViewModel() {
    val showDialog =  MutableStateFlow(0)

    fun getAll() = appDatabase.ingredientDao().getAll()

    fun getTodayAll() = getAll().map { it.filter { DateTime(it.addedAt).dayOfYear == DateTime.now().dayOfYear} }
}

@Composable
fun CaloriesCircle(
    caloriesConsumed: Int,
    goalCalories: Int,
) {
    Box (
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
    ) {
        val startAngle = 180f
        val maxSweep = 180f
        val total = maxSweep * caloriesConsumed
        var calculatedSweep = total / goalCalories
        var color = Color.White

        if (calculatedSweep > maxSweep) {
            calculatedSweep = maxSweep
            color = Color.Red
        }
        Canvas(
            Modifier
                .width(200.dp)
                .height(200.dp)
                .padding(3.dp)
                .align(Alignment.Center)
        ) {
            drawArc(
                color = color.copy(0.3f),
                startAngle = startAngle,
                sweepAngle = maxSweep,
                useCenter = false,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = calculatedSweep,
                useCenter = false,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
            )
        }

        Column {
            Spacer(Modifier.weight(1f))
            Text(
                text = caloriesConsumed.toString(),
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = color
                ),
            )
            Spacer(Modifier.weight(1f))
        }
    }
}