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
    focusedLabelColor = Color(0xFF0288D1),
    unfocusedLabelColor = Color(0xFF000000),
    cursorColor = Color(0xFF0288D1),
    focusedBorderColor = Color(0xFF29B6F6),
    unfocusedBorderColor = Color(0xFF90A4AE),
    focusedPlaceholderColor = Color(0xFF546E7A),
    unfocusedPlaceholderColor = Color(0xFF546E7A),
    focusedLeadingIconColor = Color(0xFF0288D1),
    unfocusedLeadingIconColor = Color(0xFF000000),
    focusedTrailingIconColor = Color(0xFF0288D1),
    unfocusedTrailingIconColor = Color(0xFF000000),
    errorTextColor = Color.Black,
    errorBorderColor = Color(0xFFD32F2F),
    errorLabelColor = Color(0xFFD32F2F),
    errorCursorColor = Color(0xFFD32F2F)
)

/**
 * Reusable high-contrast AppTextField for standard forms, dialogs, and screens.
 * Ensures consistent font sizes (14sp for input, 13.5sp for labels) and avoids label/placeholder overlap.
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
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        },
        placeholder = if (placeholder != null) {
            {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontSize = 13.5.sp,
                        color = Color(0xFF546E7A),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        } else null,
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
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
