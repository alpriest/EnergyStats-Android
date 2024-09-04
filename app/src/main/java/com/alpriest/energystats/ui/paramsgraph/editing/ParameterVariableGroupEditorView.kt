package com.alpriest.energystats.ui.paramsgraph.editing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
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
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.PaleWhite
import com.alpriest.energystats.ui.theme.PowerFlowNegative
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun Header(viewModel: ParameterVariableGroupEditorViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGroup = viewModel.selected.collectAsState().value
    val groups = viewModel.groups.collectAsState().value
    val renameDialogState = rememberMaterialDialogState()
    val createDialogState = rememberMaterialDialogState()
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
                    Button(onClick = { expanded = !expanded }) {
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
            Button(
                onClick = {
                    dialogText.value = viewModel.selected.value.title
                    renameDialogState.show()
                }
            ) {
                Text(
                    stringResource(R.string.rename),
                    color = colorScheme.onPrimary
                )
            }

            Button(onClick = {
                createDialogState.show()
            }) {
                Text(
                    stringResource(R.string.create_new),
                    color = colorScheme.onPrimary
                )
            }

            Button(
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

        TextEntryDialog(createDialogState, "") {
            viewModel.create(it)
        }
        TextEntryDialog(renameDialogState, dialogText.value) {
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

@Composable
fun TextEntryDialog(dialogState: MaterialDialogState, text: String, onConfirm: (String) -> Unit) {
    var dialogText by remember { mutableStateOf(text) }

    MaterialDialog(dialogState = dialogState, buttons = {
        positiveButton("Ok", onClick = { onConfirm(dialogText) })
        negativeButton("Cancel")
    }) {
        input(label = "Name", prefill = text, placeholder = "Jon Smith") { inputString ->
            dialogText = inputString
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