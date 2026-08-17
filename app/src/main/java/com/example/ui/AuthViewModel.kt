package com.example.ui

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class OtpSent(val verificationId: String) : AuthState()
    data class Authenticated(val userEmail: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(
        try {
            if (FirebaseAuth.getInstance().currentUser != null) {
                AuthState.Authenticated(FirebaseAuth.getInstance().currentUser?.email ?: "User")
            } else {
                AuthState.Idle
            }
        } catch (e: IllegalStateException) {
            AuthState.Error("Firebase is not configured. Please add google-services.json.")
        }
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated(result.user?.email ?: "User")
                onSuccess()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Sign-in failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated(result.user?.email ?: "User")
                onSuccess()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Sign-up failed")
            }
        }
    }

    fun sendPhoneOtp(phoneNumber: String, activity: Activity, onSignInSuccess: () -> Unit) {
        _authState.value = AuthState.Loading
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential, onSignInSuccess)
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    _authState.value = AuthState.Error(e.localizedMessage ?: "Verification failed")
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _authState.value = AuthState.OtpSent(verificationId)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneOtp(verificationId: String, code: String, onSignInSuccess: () -> Unit) {
        _authState.value = AuthState.Loading
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential, onSignInSuccess)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, onSignInSuccess: () -> Unit) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val phone = task.result?.user?.phoneNumber ?: "Phone User"
                    _authState.value = AuthState.Authenticated(phone)
                    onSignInSuccess()
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Sign in failed")
                }
            }
    }

    fun signInWithGoogle(context: Context, webClientId: String, onSignInSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                handleCredentialResult(result, onSignInSuccess)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Sign-in failed. Please check your Google Cloud Web Client ID configuration.")
            }
        }
    }

    private suspend fun handleCredentialResult(result: GetCredentialResponse, onSignInSuccess: () -> Unit) {
        val credential = result.credential
        if (credential is androidx.credentials.CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
                val email = authResult.user?.email ?: "User"
                _authState.value = AuthState.Authenticated(email)
                onSignInSuccess()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Firebase authentication failed")
            }
        } else {
            _authState.value = AuthState.Error("Unexpected credential type")
        }
    }

    fun signOut() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: IllegalStateException) {
            // Ignore
        }
        _authState.value = AuthState.Idle
    }
}
