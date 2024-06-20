package com.example.goijo.core

sealed class UIState<out T>(){
    data object Loading : UIState<Nothing>()
    data class Success<out T>(val data: T) : UIState<T>()
    data class Failure(val message: String) : UIState<Nothing>()
}