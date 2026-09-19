package com.example.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary

/**
 * Global high-contrast color specification for all text fields across the app.
 */
@Composable
fun appTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedLabelColor = Color(0xFF1565C0),
    unfocusedLabelColor = Color(0xFF616161),
    cursorColor = Color(0xFF1565C0),
    focusedBorderColor = Color(0xFF1565C0),
    unfocusedBorderColor = Color(0xFF9E9E9E),
    focusedPlaceholderColor = Color(0xFF757575),
    unfocusedPlaceholderColor = Color(0xFF757575),
    focusedLeadingIconColor = Color(0xFF1565C0),
    unfocusedLeadingIconColor = Color(0xFF616161),
    focusedTrailingIconColor = Color(0xFF1565C0),
    unfocusedTrailingIconColor = Color(0xFF616161),
    errorTextColor = Color.Black,
    errorBorderColor = Color(0xFFD32F2F),
    errorLabelColor = Color(0xFFD32F2F),
    errorCursorColor = Color(0xFFD32F2F)
)

/**
 * Reusable high-contrast AppTextField for standard forms, dialogs, and screens.
 * Ensures consistent font sizes (>= 16sp for input, >= 14sp for labels) and avoids label/placeholder overlap.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            val cleanLabel = label.replace("*", "").trim()
            Text(
                text = cleanLabel,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        },
        placeholder = if (placeholder != null) {
            {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        } else null,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Normal
        ),
        colors = appTextFieldColors(),
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        readOnly = readOnly,
        enabled = enabled,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource
    )
}
