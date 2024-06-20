package com.example.goijo.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

class UIClassifyViewModel : ViewModel() {
    private val _fabIsAllVisible = MutableLiveData<Boolean>()

    init {
        _fabIsAllVisible.value = false
    }
}