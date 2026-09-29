package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GuestEntity
import com.example.data.model.PersonType
import com.example.data.model.RsvpStatus
import com.example.util.PhoneContactHelper
import com.example.util.PhoneContactItem
import java.util.UUID

@Composable
fun ImportPhoneContactsDialog(
    eventId: String,
    onDismiss: () -> Unit,
    onImportGuests: (List<GuestEntity>) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var contacts by remember { mutableStateOf<List<PhoneContactItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedContactIds = remember { mutableStateOf(setOf<String>()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            contacts = PhoneContactHelper.fetchPhoneContacts(context)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phoneNumber.contains(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import from Phone Contacts") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!hasPermission) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Permission required to read phone contacts.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (contacts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No phone contacts found on device.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search phone contacts...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedContactIds.value.size == filteredContacts.size && filteredContacts.isNotEmpty(),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedContactIds.value = filteredContacts.map { it.id }.toSet()
                                    } else {
                                        selectedContactIds.value = emptySet()
                                    }
                                }
                            )
                            Text(
                                text = "Select All (${selectedContactIds.value.size}/${filteredContacts.size})",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Text(
                            text = "Total: ${contacts.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredContacts, key = { it.id }) { contact ->
                            val isSelected = selectedContactIds.value.contains(contact.id)
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                val current = selectedContactIds.value.toMutableSet()
                                                if (checked) current.add(contact.id) else current.remove(contact.id)
                                                selectedContactIds.value = current
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = contact.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(text = contact.phoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selectedItems = contacts.filter { selectedContactIds.value.contains(it.id) }
                    val newGuests = selectedItems.map { c ->
                        GuestEntity(
                            id = "phone_${UUID.randomUUID()}",
                            eventId = eventId,
                            name = c.name,
                            phoneNumber = c.phoneNumber,
                            whatsAppNumber = c.phoneNumber,
                            email = c.email,
                            personType = PersonType.FRIEND.name,
                            rsvpStatus = RsvpStatus.PENDING.name
                        )
                    }
                    onImportGuests(newGuests)
                    onDismiss()
                },
                enabled = selectedContactIds.value.isNotEmpty()
            ) {
                Text("Import Selected (${selectedContactIds.value.size})")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
