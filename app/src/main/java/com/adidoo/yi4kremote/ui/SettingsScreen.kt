@file:OptIn(ExperimentalMaterial3Api::class)

package com.adidoo.yi4kremote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.adidoo.yi4kremote.settings.CATEGORY_ORDER
import com.adidoo.yi4kremote.settings.SettingCategory
import com.adidoo.yi4kremote.settings.groupSettings
import com.adidoo.yi4kremote.settings.isSensitiveKey
import com.adidoo.yi4kremote.settings.specFor

@Composable
fun SettingsScreen(
    settings: Map<String, String>,
    onSetSetting: (String, String) -> Unit,
    onLoadChoices: suspend (String) -> List<String>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = remember(settings) { groupSettings(settings) }
    var allowOtherEdits by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
            }
            Spacer(Modifier.width(8.dp))
            Text("Réglages avancés", style = MaterialTheme.typography.titleLarge)
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            CATEGORY_ORDER.forEach { category ->
                val entries = grouped[category] ?: return@forEach
                item(key = "header_${category.name}") {
                    CategoryHeader(
                        category = category,
                        allowOtherEdits = allowOtherEdits,
                        onAllowOtherEditsChange = { allowOtherEdits = it },
                    )
                }
                items(entries, key = { it.first }) { (key, value) ->
                    SettingRow(
                        settingKey = key,
                        value = value,
                        category = category,
                        forceEditable = allowOtherEdits,
                        onSetSetting = onSetSetting,
                        onLoadChoices = onLoadChoices,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    category: SettingCategory,
    allowOtherEdits: Boolean,
    onAllowOtherEditsChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp)) {
        Text(
            category.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        if (category == SettingCategory.AUTRES) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Autoriser la modification (non vérifié pour ce modèle)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = allowOtherEdits, onCheckedChange = onAllowOtherEditsChange)
            }
        }
        Spacer(Modifier.width(4.dp))
        HorizontalDivider()
    }
}

@Composable
private fun SettingRow(
    settingKey: String,
    value: String,
    category: SettingCategory,
    forceEditable: Boolean,
    onSetSetting: (String, String) -> Unit,
    onLoadChoices: suspend (String) -> List<String>,
) {
    val spec = remember(settingKey) { specFor(settingKey) }
    val sensitive = remember(settingKey) { isSensitiveKey(settingKey) }
    val isEditable = if (category == SettingCategory.AUTRES) forceEditable else !spec.readOnly

    when {
        !isEditable -> ReadOnlySettingRow(label = spec.label, value = value, sensitive = sensitive)
        category == SettingCategory.AUTRES -> FreeTextSettingRow(
            label = spec.label,
            settingKey = settingKey,
            currentValue = value,
            warning = "Réglage non vérifié pour ce modèle — à vos risques.",
            sensitive = sensitive,
            onSetSetting = onSetSetting,
        )
        else -> EditableChoiceRow(
            label = spec.label,
            settingKey = settingKey,
            currentValue = value,
            onSetSetting = onSetSetting,
            onLoadChoices = onLoadChoices,
        )
    }
}

@Composable
private fun ReadOnlySettingRow(label: String, value: String, sensitive: Boolean = false) {
    var revealed by remember(label) { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (sensitive && !revealed) "••••••••" else value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (sensitive) {
                IconButton(onClick = { revealed = !revealed }) {
                    Icon(
                        if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (revealed) "Masquer" else "Afficher",
                    )
                }
            }
        }
    }
}

/** Loads the setting's valid choices lazily; renders a dropdown once known, or falls back to
 * free-text entry once loading is done and the camera reported no choices for this key. */
@Composable
private fun EditableChoiceRow(
    label: String,
    settingKey: String,
    currentValue: String,
    onSetSetting: (String, String) -> Unit,
    onLoadChoices: suspend (String) -> List<String>,
) {
    var choices by remember(settingKey) { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(settingKey) { choices = onLoadChoices(settingKey) }

    when (val loaded = choices) {
        null -> ReadOnlySettingRow(label = "$label…", value = currentValue)
        else -> if (loaded.isNotEmpty()) {
            DropdownSettingRow(
                label = label,
                settingKey = settingKey,
                currentValue = currentValue,
                choices = loaded,
                onSetSetting = onSetSetting,
            )
        } else {
            FreeTextSettingRow(
                label = label,
                settingKey = settingKey,
                currentValue = currentValue,
                warning = null,
                onSetSetting = onSetSetting,
            )
        }
    }
}

@Composable
private fun DropdownSettingRow(
    label: String,
    settingKey: String,
    currentValue: String,
    choices: List<String>,
    onSetSetting: (String, String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        OutlinedTextField(
            value = currentValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            choices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(choice) },
                    onClick = {
                        expanded = false
                        if (choice != currentValue) onSetSetting(settingKey, choice)
                    },
                )
            }
        }
    }
}

@Composable
private fun FreeTextSettingRow(
    label: String,
    settingKey: String,
    currentValue: String,
    warning: String?,
    sensitive: Boolean = false,
    onSetSetting: (String, String) -> Unit,
) {
    var draft by remember(currentValue) { mutableStateOf(currentValue) }
    var revealed by remember(settingKey) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text(label) },
                singleLine = true,
                visualTransformation = if (sensitive && !revealed) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                trailingIcon = if (sensitive) {
                    {
                        IconButton(onClick = { revealed = !revealed }) {
                            Icon(
                                if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (revealed) "Masquer" else "Afficher",
                            )
                        }
                    }
                } else null,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { if (draft != currentValue) onSetSetting(settingKey, draft) },
                enabled = draft != currentValue,
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Valider")
            }
        }
        warning?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
