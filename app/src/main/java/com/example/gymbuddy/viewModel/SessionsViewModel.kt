package com.example.gymbuddy.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SessionsViewModel:ViewModel() {

    private val _isInputOngoing=MutableLiveData(false)
    var isInputOngoing: LiveData<Boolean> = _isInputOngoing
    fun setIsInputOngoing(isInputOngoing:Boolean){
        _isInputOngoing.postValue(isInputOngoing)
    }

    private val _setNo=MutableLiveData(1)
    var setNo: LiveData<Int> = _setNo
    fun setSetNo(setNo:Int){
        _setNo.value=setNo
    }

    private val _position=MutableLiveData(0)
    var position: LiveData<Int> = _position
    fun setPosition(position:Int){
        _position.value=position
    }
}