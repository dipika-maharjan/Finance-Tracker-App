package com.talhaatif.financeapk.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.firebase.firestore.SetOptions
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.firebase.Variables
import java.io.ByteArrayOutputStream
import android.provider.MediaStore
import android.util.Log

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Variables.db
    private val auth = Variables.auth
    private val storageRef = Variables.storageRef
    private val utils = Util()

    private val _profileData = MutableLiveData<Map<String, String>>()
    val profileData: LiveData<Map<String, String>> get() = _profileData

    private val _profileImageUri = MutableLiveData<Uri>()
    val profileImageUri: LiveData<Uri> get() = _profileImageUri

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> get() = _updateSuccess

    fun logout(){
        Util().saveLocalData(getApplication(), "uid", "")
        Util().saveLocalData(getApplication(), "auth", "false")
        Util().saveLocalData(getApplication(),"currency","")
        auth.signOut()
    }

    fun getCurrency() : String?{
        return utils.getLocalData(getApplication(),"currency")
    }

    fun fetchUserProfile() {
        val userId = utils.getLocalData(getApplication(), "uid") ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val profileData = hashMapOf(
                    "name" to (document.getString("name") ?: ""),
                    "currency" to (document.getString("currency") ?: ""),
                    "image" to (document.getString("image") ?: "")
                )
                _profileData.value = profileData
            }
            .addOnFailureListener { _ -> _profileData.value = emptyMap() }
    }

    fun updateUserProfile(name: String, currency: String, bitmap: Bitmap?, imgChange: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val userUpdates = hashMapOf("name" to name, "currency" to currency)

        if (imgChange && bitmap != null) {
            val imageUri = getImageUri(bitmap)
            val uploadTask = storageRef.child("users/$userId").putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                storageRef.child("users/$userId").downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userUpdates["image"] = task.result.toString()
                    saveToFirestore(userId, userUpdates)
                    utils.saveLocalData(getApplication(), "currency", currency)
//                    Log.d("ProfileViewModel", "Currency saved: ${utils.getLocalData(getApplication(),"currency")}")
                } else {
                    _updateSuccess.value = false
                }
            }
        } else {
            saveToFirestore(userId, userUpdates)
            utils.saveLocalData(getApplication(), "currency", currency)
        }
    }

    private fun saveToFirestore(userId: String, userUpdates: Map<String, String>) {
        db.collection("users").document(userId)
            .set(userUpdates, SetOptions.merge())
            .addOnSuccessListener { _updateSuccess.value = true }
            .addOnFailureListener { _updateSuccess.value = false }
    }

    private fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            getApplication<Application>().contentResolver, inImage, "Title", null
        )
        return Uri.parse(path)
    }
}