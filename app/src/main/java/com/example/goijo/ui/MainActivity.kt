package com.example.goijo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.goijo.databinding.ActivityMainBinding
import com.example.goijo.ml.GoIjo
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var goIjo: GoIjo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        goIjo.close()
    }
}