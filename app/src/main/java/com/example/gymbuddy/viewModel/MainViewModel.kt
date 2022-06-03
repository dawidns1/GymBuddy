package com.example.gymbuddy.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel:ViewModel() {
    private val _rewardGranted = MutableLiveData(false)
    var rewardGranted: LiveData<Boolean> = _rewardGranted
    fun setRewardGranted(rewardGranted: Boolean){
        _rewardGranted.value=rewardGranted
    }
}