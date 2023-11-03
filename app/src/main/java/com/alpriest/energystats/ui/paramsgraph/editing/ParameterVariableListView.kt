package com.alpriest.energystats.ui.paramsgraph.editing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable

@Composable
fun ParameterVariableListView(variables: List<ParameterGraphVariable>, onTap: (ParameterGraphVariable) -> Unit) {
    variables.forEach { variable ->
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onTap(variable) }
                .fillMaxWidth()) {
            Checkbox(
                checked = variable.isSelected, onCheckedChange = {
                    onTap(variable)
                }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )

            Text(
                variable.type.name, modifier = Modifier.weight(0.5f)
            )

            Text(
                variable.type.unit, modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}