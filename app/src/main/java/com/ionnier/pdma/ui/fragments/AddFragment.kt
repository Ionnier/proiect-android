package com.ionnier.pdma.ui.fragments

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.ionnier.pdma.R
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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class AddFragment: Fragment() {
    private var imageCapture: ImageCapture? = null
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
                            var selectedIngredient: Ingredient? by remember {
                                mutableStateOf(null)
                            }
                            if (selectedIngredient != null) {
                                imageCapture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .build()
                                Column {
                                    CameraPreview(
                                        modifier = Modifier.weight(1f),
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                selectedIngredient = null
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                // Create time stamped name and MediaStore entry.
                                                val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                                                    .format(System.currentTimeMillis())
                                                val contentValues = ContentValues().apply {
                                                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                                                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                                                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                                                    }
                                                }

                                                // Create output options object which contains file + metadata
                                                val outputOptions = ImageCapture.OutputFileOptions
                                                    .Builder(context.contentResolver,
                                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                        contentValues)
                                                    .build()

                                                imageCapture?.takePicture(
                                                    outputOptions,
                                                    ContextCompat.getMainExecutor(context),
                                                    object : ImageCapture.OnImageSavedCallback {
                                                        override fun onError(exc: ImageCaptureException) {
                                                            Timber.e("Photo capture failed: ${exc.message}")
                                                        }

                                                        override fun
                                                                onImageSaved(output: ImageCapture.OutputFileResults){
                                                            val msg = "Photo capture succeeded: ${output.savedUri}"
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                            Timber.w(msg)
                                                        }
                                                    }
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                            )

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
                                                OutlinedButton(onClick = {
                                                    for (permission in REQUIRED_PERMISSIONS) {
                                                        when {
                                                            ContextCompat.checkSelfPermission(
                                                                context,
                                                                permission
                                                            ) == PackageManager.PERMISSION_GRANTED -> {
                                                                continue
                                                            }
                                                        else -> {
                                                            ActivityCompat.requestPermissions(
                                                                activity as Activity,
                                                                REQUIRED_PERMISSIONS,
                                                                2
                                                            )
                                                        }
                                                        }
                                                    }
                                                    if (!allPermissionsGranted(context)) {
                                                        Toast.makeText(context, "Permissions not granted", Toast.LENGTH_SHORT)
                                                            .show()
                                                        return@OutlinedButton
                                                    }
                                                    selectedIngredient = it
                                                }) {
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

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        private fun allPermissionsGranted(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                context, it) == PackageManager.PERMISSION_GRANTED
        }
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        private fun hasBackCamera(cameraProvider: CameraProvider): Boolean {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        }

        private fun hasFrontCamera(cameraProvider: CameraProvider): Boolean {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        }
    }

    @Composable
    fun CameraPreview(
        modifier: Modifier = Modifier,
        scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val lensFacing = when {
                        hasBackCamera(cameraProvider) -> CameraSelector.LENS_FACING_BACK
                        hasFrontCamera(cameraProvider) -> CameraSelector.LENS_FACING_FRONT
                        else -> throw IllegalStateException("Back and front camera are unavailable")
                    }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    cameraProvider.unbindAll()

                    // Create time stamped name and MediaStore entry.
                    val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                        .format(System.currentTimeMillis())
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                        }
                    }


                    // Create output options object which contains file + metadata
                    val outputOptions = ImageCapture.OutputFileOptions
                        .Builder(context.contentResolver,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues)
                        .build()

                    imageCapture?.let {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            it
                        )
                    }
                }, executor)
                previewView
            },
            modifier = modifier,
        )
    }
}

@HiltViewModel
class AddViewModel @Inject constructor(
    private val client: HttpClient
): ViewModel() {
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