package com.project.postalapp.ui


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.postalapp.adapter.SchemesAdapter
import com.project.postalapp.databinding.ActivityViewAnnouncementsBinding
import com.project.postalapp.response.LoginResponse
import com.project.postalapp.response.RetrofitInstance
import com.project.postalapp.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewAnnouncements : AppCompatActivity() {
    private val bind by lazy { ActivityViewAnnouncementsBinding.inflate(layoutInflater) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        CoroutineScope(IO).launch {
            RetrofitInstance.instance.getAnnouncements().enqueue(object : Callback<LoginResponse?> {
                override fun onResponse(
                    p0: Call<LoginResponse?>,
                    p1: Response<LoginResponse?>,
                ) {
                    val response = p1.body()!!
                    if (response.error) {
                        showToast("Error Occurred")
                    } else {
                        val list = response.data3
                        if (list != null) {
                            bind.progressBar2.isVisible = false

                            val adapter = SchemesAdapter(list) { scheme ->
                                startActivity(
                                    Intent(
                                        this@ViewAnnouncements,
                                        WebActivity::class.java
                                    ).apply {
                                        putExtra("url", scheme.aUrl)
                                    })
                            }
                            bind.rvzlIst.adapter = adapter
                        } else {
                            bind.progressBar2.isVisible = false

                            showToast("Empty List")
                        }

                    }
                }

                override fun onFailure(p0: Call<LoginResponse?>, p1: Throwable) {
                    showToast(p1.message!!)
                    bind.progressBar2.isVisible = false

                }
            })
        }




        bind.rvzlIst.layoutManager = LinearLayoutManager(this)


    }
}