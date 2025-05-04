package com.project.postalapp.PostOfficeList

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.project.postalapp.databinding.ActivitySearchViewBinding
import com.project.postalapp.room.AppDatabase
import kotlinx.coroutines.launch

class SearchViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchViewBinding
    private val viewmodel: PostOfficeViewmodel by viewModels()
    private val viewmodel2: OfficeViewModel by viewModels() // Fixed: Corrected type from inferred to explicit
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val db by lazy { AppDatabase.getDatabase(this).reviewDao() }
    private lateinit var adapter: OfficeAdapter
    private var originalList: ArrayList<Office> = ArrayList() // Fixed: Removed nullable type Office?

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        adapter = OfficeAdapter(emptyList())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        // Get location and load nearest offices
        fused.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewmodel2.loadNearestOffices(this)

                // Observe nearest offices
                viewmodel2.nearestOffices.observe(this@SearchViewActivity) { offices ->
                    originalList.clear()
                    originalList.addAll(offices)
                    adapter.updatelist(offices) // Update adapter with new data
                    Log.d("SearchViewActivity", "Nearest offices: $offices")
                }
            }
        }

        // Setup search functionality
        binding.searchView.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            filterData(query)
        }
    }

    private fun filterData(query: String) {
        if (query.isEmpty()) {
            adapter.updatelist(originalList)
        } else {
            viewmodel2.search(this@SearchViewActivity, query.lowercase())
            viewmodel2.searchResults.observe(this@SearchViewActivity) { searchResults ->
                adapter.updatelist(searchResults)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fused.flushLocations()
    }
}