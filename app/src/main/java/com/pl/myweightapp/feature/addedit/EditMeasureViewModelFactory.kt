//package com.pl.myweightapp.feature.addedit
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.pl.myweightapp.data.repository.WeightMeasureRepository
//
//class EditMeasureViewModelFactory(
//    private val itemId: Long,
//    private val repository: WeightMeasureRepository
//) : ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(EditMeasureViewModel::class.java)) {
//            return EditMeasureViewModel(itemId, repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}