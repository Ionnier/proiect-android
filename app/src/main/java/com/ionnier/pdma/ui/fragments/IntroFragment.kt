package com.ionnier.pdma.ui.fragments

import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ionnier.pdma.R
import com.ionnier.pdma.Settings
import com.ionnier.pdma.data.LanguageResponse
import com.ionnier.pdma.data.Languages
import com.ionnier.pdma.databinding.LayoutLanguageBinding
import com.ionnier.pdma.ui.recyclerview.LanguageAdapter
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class IntroFragment : Fragment() {
    private val languageViewModel: LanguageViewModel by viewModels()

    private var _binding: LayoutLanguageBinding? = null
    private val binding get() = _binding!!
    private lateinit var languageAdapter: LanguageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Settings.getPreferedLanguage() != null) {
            if (findNavController().currentDestination?.label == "fragment_intro") {
                findNavController().navigate(IntroFragmentDirections.openLogin())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        languageViewModel.loadInProgress.observe(viewLifecycleOwner) {
            binding.loading.visibility = if(it) View.VISIBLE else View.GONE
        }
        languageAdapter = LanguageAdapter(emptyList()) {
            languageViewModel.getCountry(it)?.let {
                Settings.setPreferedLanguage(it)
                if (findNavController().currentDestination?.label == "fragment_intro") {
                    findNavController().navigate(IntroFragmentDirections.openLogin())
                }
            }
        }
        languageViewModel.getSupportedLanguages.observe(viewLifecycleOwner) {
            languageAdapter.wifiList = it
            languageAdapter.notifyDataSetChanged()
        }

        binding.wifiList.apply {
            adapter = languageAdapter
            layoutManager = LinearLayoutManager(context)
        }
        binding.searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    Timber.w(p0)
                    languageViewModel.updateCurrentFilter(p0)
                    languageAdapter.notifyDataSetChanged()
                }
                return true
            }
        })
    }
}

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val client: HttpClient
): ViewModel() {
    private val _loadInProgress = MutableLiveData(false)
    val loadInProgress: LiveData<Boolean>
        get() {
            return _loadInProgress
        }

    var currentFilter = MutableLiveData("")
    init {
        viewModelScope.launch {
            updateValue(_loadInProgress, true)
            try {
                val request = client.get("https://wger.de/api/v2/language")
                val data: LanguageResponse = request.body()
                Timber.w(data.toString())

                updateValue(_loadInProgress, false)
                fullCountries = data.results
                updateValue(_supportedLanguages, data.results.filter { it.shortName != null }.map { it.shortName ?: "" }.toMutableList())
            } catch (e: java.lang.Exception) {
                updateValue(_loadInProgress, false)
                Timber.w(e)
            }

        }

    }

    private var fullCountries: List<Languages> = emptyList()
    fun getCountry(shortName: String) = fullCountries.firstOrNull { it.shortName == shortName }

    private val _supportedLanguages = MutableLiveData(mutableListOf<String>())
    private val supportedLanguages: LiveData<MutableList<String>>
        get() {
            return _supportedLanguages
        }


    val getSupportedLanguages = MediatorLiveData<MutableList<String>>().also {
        it.addSource(supportedLanguages) { supportedLanguages ->
            it.value = supportedLanguages
        }
        it.addSource(currentFilter) { filter ->
            Timber.w("$filter")
            it.value = supportedLanguages.value?.filter {
                if (filter == null) return@filter true
                if (filter.isBlank()) return@filter true
                it.contains(filter) || Locale(Locale.getDefault().language, it).displayCountry.lowercase().contains(filter)
            }?.toMutableList() ?: mutableListOf()
            Timber.w("${it.value}")
        }
    }

    fun updateCurrentFilter(filter: String) {
        updateValue(currentFilter, filter)
    }
}

private fun<T> updateValue(variable: MutableLiveData<T>, newValue: T) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        variable.value = newValue
    } else {
        variable.postValue(newValue)
    }
}