@file:OptIn(ExperimentalMaterial3Api::class)

package com.alpriest.energystats.ui.paramsgraph.editing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPadding
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.PaleWhite
import com.alpriest.energystats.ui.theme.PowerFlowNegative
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun Header(viewModel: ParameterVariableGroupEditorViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGroup = viewModel.selected.collectAsState().value
    val groups = viewModel.groups.collectAsState().value
    val renameDialogShowing = remember { mutableStateOf(false) }
    val createDialogShowing = remember { mutableStateOf(false) }
    val dialogText = remember { mutableStateOf("") }
    val canDelete = viewModel.canDelete.collectAsState().value

    Column {
        SettingsColumnWithChild {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.choose_group_to_edit),
                    color = colorScheme.onSecondary
                )

                Box(contentAlignment = Alignment.TopEnd) {
                    ESButton(onClick = { expanded = !expanded }) {
                        Text(
                            selectedGroup.title,
                            fontSize = 12.sp,
                            color = colorScheme.onPrimary,
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = colorScheme.onPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(onClick = {
                                expanded = false
                                viewModel.select(group)
                            }, text = {
                                Text(group.title)
                            })
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(SettingsPadding.PANEL_INNER_HORIZONTAL),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ESButton(
                onClick = {
                    dialogText.value = viewModel.selected.value.title
                    renameDialogShowing.value = true
                }
            ) {
                Text(
                    stringResource(R.string.rename),
                    color = colorScheme.onPrimary
                )
            }

            ESButton(onClick = {
                createDialogShowing.value = true
            }) {
                Text(
                    stringResource(R.string.create_new),
                    color = colorScheme.onPrimary
                )
            }

            ESButton(
                onClick = {
                    viewModel.delete()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PowerFlowNegative,
                    contentColor = PaleWhite
                ),
                enabled = canDelete
            ) {
                Image(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        TextEntryDialog(stringResource(R.string.create_new), createDialogShowing.value, "", { createDialogShowing.value = false }) {
            createDialogShowing.value = false
            viewModel.create(it)
        }
        TextEntryDialog(stringResource(R.string.rename), renameDialogShowing.value, dialogText.value, { renameDialogShowing.value = false }) {
            renameDialogShowing.value = false
            viewModel.rename(it)
        }
    }
}

@Composable
fun ParameterVariableGroupEditorView(viewModel: ParameterVariableGroupEditorViewModel, navController: NavHostController) {
    val variables = viewModel.variables.collectAsState().value

    ContentWithBottomButtonPair(navController = navController, onSave = {
        viewModel.save()
        navController.popBackStack()
    }, { modifier ->
        SettingsPage(modifier) {
            Header(viewModel)

            SettingsColumn(
                header = "Choose parameters"
            ) {
                ParameterVariableListView(variables = variables, onTap = { viewModel.toggle(it) })
            }
        }
    }, Modifier)
}

@ExperimentalMaterial3Api
@Composable
fun TextEntryDialog(
    title: String,
    isOpen: Boolean,
    text: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (isOpen) {
        var dialogText by remember { mutableStateOf(text) }

        BasicAlertDialog(onDismissRequest = onCancel) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = dialogText,
                        onValueChange = { dialogText = it },
                        label = { Text(title) },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row {
                        ESButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1.0f)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        ESButton(
                            onClick = { onConfirm(dialogText) },
                            modifier = Modifier.weight(1.0f)
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun ParameterVariableGroupEditorViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        ParameterVariableGroupEditorView(
            viewModel = ParameterVariableGroupEditorViewModel(FakeConfigManager(), MutableStateFlow(previewParameterGraphVariables())),
            NavHostController(LocalContext.current)
        )
    }
}