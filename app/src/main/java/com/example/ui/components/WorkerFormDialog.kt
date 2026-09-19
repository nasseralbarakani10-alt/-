package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenButton

@Composable
fun WorkerFormDialog(
    isCutter: Boolean,
    initialName: String = "",
    initialPhone: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var phoneNumber by remember { mutableStateOf(initialPhone) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            val phone = extractPhoneNumberFromContactUri(context, contactUri)
            if (!phone.isNullOrBlank()) {
                phoneNumber = phone
                errorMessage = null
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            val activity = context as? Activity
            if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {
                showSettingsDialog = true
            } else {
                showRationaleDialog = true
            }
        }
    }

    fun handleContactPickerClick() {
        val permission = Manifest.permission.READ_CONTACTS
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            val activity = context as? Activity
            if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationaleDialog = true
            } else {
                permissionLauncher.launch(permission)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title Header
                val roleTitle = if (isCutter) "القصاص" else "الخياط"
                val dialogTitle = if (isEditing) "تعديل بيانات $roleTitle" else "إضافة $roleTitle جديد"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            fontSize = 20.sp
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_worker_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF212121),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error banner
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE),
                        border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // 1. Name field (اسم القصاص / اسم الخياط)
                val nameLabel = if (isCutter) "اسم القصاص" else "اسم الخياط"
                AppTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = nameLabel,
                    placeholder = if (isCutter) "مثال: محمود القصاص" else "مثال: عبد الله الخياط",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF1565C0)
                        )
                    },
                    modifier = Modifier.testTag("input_worker_name")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Phone field: label "رقم الهاتف", text input in middle, contacts icon on left
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        errorMessage = null
                    },
                    label = {
                        Text(
                            text = "رقم الهاتف",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    },
                    placeholder = {
                        Text(
                            text = "مثال: 0501234567 أو +966501234567",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color(0xFF757575),
                                fontWeight = FontWeight.Normal
                            )
                        )
                    },
                    // In RTL layout, the trailingIcon sits visually on the LEFT side of the field
                    trailingIcon = {
                        IconButton(
                            onClick = { handleContactPickerClick() },
                            modifier = Modifier.testTag("pick_contact_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = "اختيار من جهات الاتصال",
                                tint = Color(0xFF1565C0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    ),
                    colors = appTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_worker_phone")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color(0xFF757575)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("cancel_worker_button")
                    ) {
                        Text(
                            text = "إلغاء",
                            color = Color(0xFF424242),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val trimmedName = name.trim()
                            val trimmedPhone = phoneNumber.trim()

                            if (trimmedName.isBlank()) {
                                errorMessage = if (isCutter) "يرجى إدخال اسم القصاص" else "يرجى إدخال اسم الخياط"
                            } else if (trimmedPhone.isBlank()) {
                                errorMessage = "يرجى إدخال رقم الهاتف"
                            } else if (!Regex("^\\+?[0-9]{7,15}$").matches(trimmedPhone)) {
                                errorMessage = "يرجى إدخال رقم هاتف صحيح (أرقام فقط مع إمكانية البدء بـ +، من 7 إلى 15 رقماً)"
                            } else {
                                onSave(trimmedName, trimmedPhone)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenButton),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_worker_button")
                    ) {
                        Text(
                            text = "حفظ",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Permission Rationale Dialog
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = {
                Text(
                    text = "إذن الوصول إلى جهات الاتصال",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BluePrimary
                )
            },
            text = {
                Text(
                    text = "يحتاج التطبيق إلى إذن الوصول لجهات الاتصال لتسهيل اختيار رقم هاتف الشخص مباشرة من هاتفك بدلاً من كتابته يدوياً.",
                    fontSize = 15.sp,
                    color = Color(0xFF212121)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationaleDialog = false
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("منح الإذن", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("إلغاء", color = Color(0xFF616161))
                }
            }
        )
    }

    // Permanently Denied Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "إذن جهات الاتصال مطلوب",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFC62828)
                )
            },
            text = {
                Text(
                    text = "تم تعطيل إذن جهات الاتصال. يمكنك تفعيله من إعدادات التطبيق لاستيراد الأرقام مباشرة من سجل الهاتف.",
                    fontSize = 15.sp,
                    color = Color(0xFF212121)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsDialog = false
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("فتح الإعدادات", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("إلغاء", color = Color(0xFF616161))
                }
            }
        )
    }
}

/**
 * Helper to safely extract a phone number from a picked contact URI.
 */
private fun extractPhoneNumberFromContactUri(context: Context, contactUri: Uri): String? {
    val cr = context.contentResolver
    var phoneNumber: String? = null

    try {
        val cursor = cr.query(contactUri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val hasPhoneIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                val hasPhone = if (hasPhoneIndex != -1) c.getInt(hasPhoneIndex) else 0
                val contactId = if (idIndex != -1) c.getString(idIndex) else null

                if (hasPhone > 0 && contactId != null) {
                    val phoneCursor = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )
                    phoneCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            val numIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numIdx != -1) {
                                phoneNumber = pc.getString(numIdx)
                            }
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        // Fallback
    }

    // If contactUri was a direct data phone URI
    if (phoneNumber.isNullOrBlank()) {
        try {
            val directCursor = cr.query(
                contactUri,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )
            directCursor?.use { dc ->
                if (dc.moveToFirst()) {
                    val numIdx = dc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (numIdx != -1) {
                        phoneNumber = dc.getString(numIdx)
                    }
                }
            }
        } catch (ignored: Exception) {}
    }

    return phoneNumber?.replace(Regex("[^0-9+]"), "")
}
