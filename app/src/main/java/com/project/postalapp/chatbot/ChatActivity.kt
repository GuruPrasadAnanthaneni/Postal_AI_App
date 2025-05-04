package com.project.postalapp.chatbot

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Geocoder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cropiq.chatbot.GeminiAdapter
import com.example.cropiq.model.DataResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.gms.location.LocationServices
import com.project.postalapp.databinding.ActivityChatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private val binding by lazy { ActivityChatBinding.inflate(layoutInflater) }
    private var bitmap: Bitmap? = null
    private val responseData = arrayListOf<DataResponse>()
    private val viewModel: PincodeViewModel by viewModels()
    private lateinit var adapter: GeminiAdapter
    private var currentPostalData: List<PostOffice>? = null
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    var city = "New Delhi" // Default value
    var pincode1 = ""
    var ad = ""

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                binding.selectImage.setImageURI(it)
            } ?: Log.d("Photopicker", "No media selected")
        }

    @SuppressLint("NotifyDataSetChanged", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupLocation()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupLocation() {
        getUserLocation()
        viewModel.pincodeData.observe(this) { responses ->
            if (responses.isNullOrEmpty()) {
                Log.d("PincodeData", "No postal data received")
                return@observe
            }

            responses.firstOrNull()?.let { response ->
                currentPostalData = response.PostOffice?.sortedBy { it.Name }
                response.PostOffice?.forEach { postOffice ->
                    Log.d("PincodeData", "PostOffice: ${postOffice.Name} in ${postOffice.District}")
                    city = postOffice.District ?: city
                } ?: run {
                    Log.d("PincodeData", "PostOffice list is null")
                }
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            addAiMessageToChat("I couldn't access postal data. Please check your connection or try again later.")
        }
    }

    private fun setupRecyclerView() {
        adapter = GeminiAdapter(this, responseData)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = this@ChatActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.selectImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.sendButton.setOnClickListener {
            handleUserQuery()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fused.lastLocation.addOnSuccessListener { location ->
            try {
                val geocoder = Geocoder(this@ChatActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                ad = addresses.toString()

                if (!addresses.isNullOrEmpty()) {
                    city = addresses[0].locality ?: "New Delhi"
                    val pincode = addresses[0].postalCode
                    pincode1 = pincode ?: ""

                    if (!pincode.isNullOrEmpty()) {
                        viewModel.fetchPincodeData(pincode)
                        addAiMessageToChat("📍 I've detected you're near $city (Pincode: $pincode). How can I help with postal services?")
                    } else {
                        Log.d("Location", "No pincode found for this location")
                        addAiMessageToChat("I couldn't detect your pincode automatically. Please tell me your location or pincode.")
                    }
                }
            } catch (e: Exception) {
                Log.e("Location", "Error getting location: ${e.message}")
                addAiMessageToChat("⚠️ I couldn't access your location. Please enable location services or tell me your pincode.")
            }
        }.addOnFailureListener { exception ->
            Log.e("Location", "Error getting location: ${exception.message}")
            addAiMessageToChat("⚠️ Location error: ${exception.message}. Please tell me your pincode manually.")
        }
    }

    private fun handleUserQuery() {
        val userQuery = binding.chatInput.text.toString().trim()
        when {
            userQuery.isBlank() -> {
                showToast("Please enter your question")
                return
            }
            userQuery.length < 3 -> {
                showToast("Please enter a more detailed question")
                return
            }
            else -> {
                binding.chatInput.setText("")
                addUserMessageToChat(userQuery)
                processUserQuery(userQuery)
            }
        }
    }

    private fun processUserQuery(query: String) {
        // Handle common queries directly for better UX
        when {
            query.contains("list", ignoreCase = true) &&
                    query.contains("post office", ignoreCase = true) -> {
                listPostOfficesDirectly()
                return
            }
            query.contains("pincode", ignoreCase = true) && pincode1.isNotEmpty() -> {
                addAiMessageToChat("Your detected pincode is: $pincode1")
                return
            }
            query.contains("location", ignoreCase = true) -> {
                addAiMessageToChat("You're currently detected in: $city (Pincode: ${pincode1.takeIf { it.isNotEmpty() } ?: "unknown"})")
                return
            }
        }

        // Use AI for other queries
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyBfRYU6WewsoDqT6eiUwMiG7jQLBybdJMs"
        )

        CoroutineScope(IO).launch {
            try {
                val response = generateAIResponse(generativeModel, query)
                runOnUiThread {
                    addAiMessageToChat(response)
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "Error generating content", e)
                runOnUiThread {
                    showToast("Failed to generate response")
                    addAiMessageToChat("I'm having trouble answering. Please try again or ask differently.")
                }
            }
        }
    }

    private fun listPostOfficesDirectly() {
        currentPostalData?.let { postOffices ->
            if (postOffices.isEmpty()) {
                addAiMessageToChat("No post offices found for your pincode.")
                return
            }

            val response = StringBuilder()
            response.append("📮 Found ${postOffices.size} post offices in $city:\n\n")

            postOffices.forEachIndexed { index, po ->
                response.append("${index + 1}. ${po.Name ?: "Unnamed Post Office"}\n")
                response.append("   📍 District: ${po.District ?: "Unknown"}\n")
                response.append("   🏛️ State: ${po.State ?: "Unknown"}\n")
                response.append("   #️⃣ Pincode: ${po.Pincode ?: "Unknown"}\n\n")
            }

            response.append("How can I assist you further with these post offices?")
            addAiMessageToChat(response.toString())
        } ?: run {
            addAiMessageToChat("Post office data isn't available. Please check your pincode or connection.")
        }
    }

    private suspend fun generateAIResponse(model: GenerativeModel, query: String): String {
        val locationContext = buildLocationContext()
        val currentTime = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault()).format(Date())

        val prompt = """
        ROLE: You are an expert India Post assistant. Provide accurate, helpful responses about postal services.Additionally if image is upload provide description of the image

        USER CONTEXT:
        $locationContext
        Current Time: $currentTime
        
        User Location: $ad

        USER QUERY: "$query"

        RESPONSE RULES:
        1. Be concise but complete (1-3 sentences unless listing items)
        2. Use simple, clear language
        3. Format lists with numbers/bullets
        4. Include relevant emojis (📍, 🏤, ⏰ etc.)
        5. For service questions, mention:
           - Availability
           - Timings if known
           - Requirements
        6. For unknown info, say "I couldn't find that information" 
           and suggest alternatives

        RESPONSE:
        """.trimIndent()

        return try {
            model.generateContent(content { text(prompt) }).text?.takeIf { it.isNotBlank() }
                ?: "I couldn't generate a response. Please try again."
        } catch (e: Exception) {
            Log.e("AI_Response", "Error: ${e.message}")
            "I'm having trouble answering. Please try again later."
        }
    }

    private fun buildLocationContext(): String {
        return if (currentPostalData.isNullOrEmpty()) {
            "📍 Approximate Location: $city" +
                    if (pincode1.isNotEmpty()) " (Pincode: $pincode1)" else ""
        } else {
            val firstOffice = currentPostalData!!.first()
            """
            📍 Confirmed Location Details:
            City/District: ${firstOffice.District ?: city}
            State: ${firstOffice.State ?: "Unknown"}
            Pincode: ${firstOffice.Pincode ?: pincode1.takeIf { it.isNotEmpty() } ?: "Unknown"}
            """.trimIndent()
        }
    }

    private fun addUserMessageToChat(message: String) {
        responseData.add(DataResponse(0, message, ""))
        adapter.notifyItemInserted(responseData.size - 1)
        binding.chatRecyclerView.scrollToPosition(responseData.size - 1)
    }

    private fun addAiMessageToChat(message: String) {
        responseData.add(DataResponse(1, message, ""))
        adapter.notifyItemInserted(responseData.size - 1)
        binding.chatRecyclerView.scrollToPosition(responseData.size - 1)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        fused.flushLocations()
    }
}