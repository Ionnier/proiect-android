package com.ionnier.pdma.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.ionnier.pdma.Settings
import com.ionnier.pdma.ui.colors.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.internal.managers.ViewComponentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AddFragment: Fragment() {
    private val addViewModel: AddViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = if (context is ViewComponentManager.FragmentContextWrapper)
            (context as ViewComponentManager.FragmentContextWrapper).baseContext
        else
            context
        return context?.let {
            ComposeView(it).apply {
                setContent {
                    val goBack = {
                        if (findNavController().currentDestination?.label == "AddFragment") {
                            findNavController().navigate(com.ionnier.pdma.R.id.openMain)
                        }
                    }
                    BackHandler(onBack = goBack)
                    MyApplicationTheme {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text("Add entry")
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = goBack) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                )
                            }
                        ) {
                            val state = addViewModel.state.collectAsState().value
                            if (state.isLoading) {
                                Box(
                                    modifier = Modifier
                                        .padding(it)
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                                return@Scaffold
                            }
                            if (state.hadError) {
                                Box(
                                    modifier = Modifier
                                        .padding(it)
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column {
                                        Text("Something went wrong")
                                        Button(onClick = { addViewModel.getData() }) {
                                            Text("Try again")
                                        }
                                    }
                                }
                                return@Scaffold
                            }
                            Column (
                                modifier = Modifier.padding(it)
                            ) {
                                var searchValue by remember {
                                    mutableStateOf("")
                                }
                                TextField(
                                    value = searchValue,
                                    onValueChange = {
                                        searchValue = it
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .fillMaxWidth()
                                )
                                LazyColumn(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(
                                        if (searchValue.isBlank()) emptyList() else state.ingredients.filter { it.name?.indexOf(searchValue) != -1 },
                                    ) {
                                        ListItem(
                                            headlineText = {
                                                Text(it.name ?: "Name")
                                            },
                                            supportingText = {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        "energy: ${it.energy}"
                                                    )
                                                    Row(
                                                        modifier = Modifier.horizontalScroll(
                                                            rememberScrollState()),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            "protein: ${it.protein}"
                                                        )
                                                        Text(
                                                            "fat: ${it.fat}"
                                                        )
                                                        Text(
                                                            "carbs: ${it.carbohydrates}"
                                                        )
                                                    }
                                                }

                                            },
                                            trailingContent = {
                                                OutlinedButton(onClick = { /*TODO*/ }) {
                                                    Text("Pick")
                                                }
                                            }
                                        )


                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class AddViewModel @Inject constructor(
    private val client: HttpClient
): ViewModel() {
    private var job: Job? = null

    val state = MutableStateFlow(AddViewModelState())

    init {
        getData()
    }

    fun getData() {
        state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            var url = "https://wger.de/api/v2/ingredient?limit=1000000"
            Settings.getPreferedLanguage()?.id.toString().let{
                url += "&language=$it"
            }
            val data = client.get(url)
            if (data.status != HttpStatusCode.OK) {
                state.update {
                    it.copy(isLoading = false, hadError = true)
                }
            }

            val response: IngredientResult = data.body()
            Timber.w("$response")
            state.update {
                it.copy(isLoading = false, hadError = false, ingredients = response.results.toList())
            }
        }
    }

}

data class AddViewModelState(
    val isLoading: Boolean = false,
    val hadError: Boolean = false,
    val ingredients: List<Ingredient> = emptyList()
)

@kotlinx.serialization.Serializable
data class IngredientResult (
    @SerialName("count") var count: Int? = null,
    @SerialName("results") var results : ArrayList<Ingredient> = arrayListOf()
)

@kotlinx.serialization.Serializable
data class Ingredient (
    @SerialName("name") var name: String? = null,
    @SerialName("energy") var energy: Int?    = null,
    @SerialName("protein") var protein: String? = null,
    @SerialName("carbohydrates") var carbohydrates: String? = null,
    @SerialName("carbohydrates_sugar") var carbohydratesSugar: String? = null,
    @SerialName("fat") var fat: String? = null,
    @SerialName("fat_saturated") var fatSaturated: String? = null,
    @SerialName("fibres") var fibres: String? = null,
    @SerialName("sodium") var sodium: String? = null,

)