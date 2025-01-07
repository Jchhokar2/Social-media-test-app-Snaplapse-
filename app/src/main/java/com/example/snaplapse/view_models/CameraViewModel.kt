package com.example.snaplapse.view_models

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel: ViewModel() {
    private val _imageBitmap = MutableLiveData<Bitmap>()
    val imageBitmap: LiveData<Bitmap> get() = _imageBitmap

    private val _profilePhotos = MutableLiveData<List<ItemsViewModel2>>()
    val profilePhotos: LiveData<List<ItemsViewModel2>> get() = _profilePhotos

    fun setImageBitmap(imageBitmap: Bitmap) {
        _imageBitmap.value = imageBitmap
    }

    fun appendProfilePhotos(profilePhoto: ItemsViewModel2) {
        if (_profilePhotos.value == null) {
            _profilePhotos.value = listOf(profilePhoto)
        } else {
            _profilePhotos.value = _profilePhotos.value as List<ItemsViewModel2> + profilePhoto
        }
    }
}
