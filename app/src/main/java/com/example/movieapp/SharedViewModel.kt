package com.example.movieapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _itemButtonClickEvent = MutableLiveData<String>()
    val itemButtonClickEvent: LiveData<String> = _itemButtonClickEvent

    private val _replyButtonClickEvent = MutableLiveData<String>()
    val replyButtonClickEvent: LiveData<String> = _replyButtonClickEvent

    fun onItemButtonClicked(itemId: String) {
        _itemButtonClickEvent.value = itemId
    }

    fun onReplyButtonClicked(itemId: String) {
        _replyButtonClickEvent.value = itemId
    }
}