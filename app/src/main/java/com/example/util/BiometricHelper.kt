package com.example.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    private const val PREFS_NAME = "eventvite_security_prefs"
    private const val KEY_IS_LOCKED = "key_is_locked"
    private const val KEY_SECURITY_PIN = "key_security_pin"
    private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
    private const val DEFAULT_PIN = "1234"

    fun isBiometricHardwareAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun isSecurityLockEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setSecurityLockEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getSavedPin(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SECURITY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setSavedPin(context: Context, pin: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SECURITY_PIN, pin).apply()
    }

    fun verifyPin(context: Context, inputPin: String): Boolean {
        return inputPin == getSavedPin(context)
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock Sensitive Guest Data",
        subtitle: String = "Confirm your identity via fingerprint or face recognition",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use Security PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Biometric authentication failed. Try again or enter PIN.")
            }
        })

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(e.message ?: "Authentication error")
        }
    }
}
