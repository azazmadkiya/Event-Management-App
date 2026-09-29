package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.util.BiometricHelper

@Composable
fun SecurityLockScreen(
    isSecurityEnabled: Boolean,
    onUnlockSuccess: () -> Unit,
    onToggleSecurity: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var inputPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun triggerBiometrics() {
        if (context is FragmentActivity && BiometricHelper.isBiometricHardwareAvailable(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                onSuccess = {
                    errorMessage = null
                    onUnlockSuccess()
                },
                onError = { err ->
                    errorMessage = err
                }
            )
        } else {
            // Emulate biometric success or prompt PIN
            errorMessage = "Biometrics sensor simulated: Enter PIN '1234' or tap Unlock Directly."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Vault Icon
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = "Security Vault",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Guest Privacy Vault",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Authenticate to access sensitive guest contact information, WhatsApp numbers, mailing addresses, and medical dietary allergies.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PIN Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val filled = i < inputPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Keypad Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { digit ->
                        when (digit) {
                            "BIO" -> {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { triggerBiometrics() }
                                        .testTag("biometric_auth_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Fingerprint,
                                            contentDescription = "Biometrics",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            }
                            "DEL" -> {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable {
                                            if (inputPin.isNotEmpty()) {
                                                inputPin = inputPin.dropLast(1)
                                                errorMessage = null
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Backspace,
                                            contentDescription = "Backspace",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            else -> {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable {
                                            if (inputPin.length < 4) {
                                                inputPin += digit
                                                errorMessage = null
                                                if (inputPin.length == 4) {
                                                    if (BiometricHelper.verifyPin(context, inputPin)) {
                                                        onUnlockSuccess()
                                                    } else {
                                                        errorMessage = "Incorrect PIN. Default PIN is '1234'."
                                                        inputPin = ""
                                                    }
                                                }
                                            }
                                        }
                                        .testTag("keypad_digit_$digit")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = digit,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Direct Unlock & Hint Button
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onUnlockSuccess,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("quick_unlock_btn")
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Unlock Directly (Testing PIN: 1234)", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Security Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Require Vault Lock",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = isSecurityEnabled,
                onCheckedChange = onToggleSecurity
            )
        }
    }
}
