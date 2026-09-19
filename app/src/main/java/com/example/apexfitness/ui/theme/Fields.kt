package com.example.apexfitness.ui.theme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement

// Text field styling shared by the sign in, sign up and onboarding forms.
// Unfocused borders use the muted colour at 50% so they are easy to see, focused ones are gold.

@Composable
fun apexFieldColors(container: Color = MaterialTheme.colorScheme.surface) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.apex.accent,
    unfocusedBorderColor = MaterialTheme.apex.mutedText.copy(alpha = 0.5f),
    errorBorderColor = MaterialTheme.apex.errorText,
    focusedLabelColor = MaterialTheme.apex.accentText,
    unfocusedLabelColor = MaterialTheme.apex.mutedText,
    errorLabelColor = MaterialTheme.apex.errorText,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    errorTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.apex.accent,
    focusedLeadingIconColor = MaterialTheme.apex.accentText,
    unfocusedLeadingIconColor = MaterialTheme.apex.mutedText,
    focusedTrailingIconColor = MaterialTheme.apex.mutedText,
    unfocusedTrailingIconColor = MaterialTheme.apex.mutedText,
    focusedContainerColor = container,
    unfocusedContainerColor = container,
    errorContainerColor = container
)

// Text field with a label
@Composable
fun ApexTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    isError: Boolean = false,
    container: Color = MaterialTheme.colorScheme.surface
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = 56.dp),
        label = { Text(text = label) },
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(imageVector = icon, contentDescription = null) }
        },
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        isError = isError,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        colors = apexFieldColors(container),
        shape = ApexShapes.small
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FieldsPreview() {
    ApexFitnessTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(Dimens.ScreenEdge),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space2)
        ) {
            ApexTextField(
                value = "alex@example.com",
                onValueChange = {},
                label = "Email Address",
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            ApexTextField(value = "", onValueChange = {}, label = "Empty field", modifier = Modifier.fillMaxWidth())
            ApexTextField(value = "oops", onValueChange = {}, label = "Error field", isError = true, modifier = Modifier.fillMaxWidth())
        }
    }
}
