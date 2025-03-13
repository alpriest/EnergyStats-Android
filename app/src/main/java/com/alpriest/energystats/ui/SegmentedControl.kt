package com.alpriest.energystats.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun SegmentedControl(
    items: List<String>,
    defaultSelectedItemIndex: Int = 0,
    useFixedWidth: Boolean = false,
    itemWidth: Dp = 120.dp,
    cornerRadius: Int = 8,
    color: Color = Color.Red,
    contentPadding: PaddingValues = PaddingValues(
        start = 12.dp,
        top = 8.dp,
        end = 12.dp,
        bottom = 8.dp
    ),
    onItemSelection: (selectedItemIndex: Int) -> Unit
) {
    val selectedIndex = remember { mutableStateOf(defaultSelectedItemIndex) }

    items.forEachIndexed { index, item ->
        OutlinedButton(
            contentPadding = contentPadding,
            modifier = when (index) {
                0 -> {
                    if (useFixedWidth) {
                        Modifier
                            .width(itemWidth)
                            .offset(0.dp, 0.dp)
                            .zIndex(if (selectedIndex.value == index) 1f else 0f)
                    } else {
                        Modifier
                            .wrapContentSize()
                            .offset(0.dp, 0.dp)
                            .zIndex(if (selectedIndex.value == index) 1f else 0f)
                    }
                }

                else -> {
                    if (useFixedWidth)
                        Modifier
                            .width(itemWidth)
                            .offset((-1 * index).dp, 0.dp)
                            .zIndex(if (selectedIndex.value == index) 1f else 0f)
                    else Modifier
                        .wrapContentSize()
                        .offset((-1 * index).dp, 0.dp)
                        .zIndex(if (selectedIndex.value == index) 1f else 0f)
                }
            },
            onClick = {
                selectedIndex.value = index
                onItemSelection(selectedIndex.value)
            },
            shape = when (index) {
                /**
                 * left outer button
                 */
                0 -> RoundedCornerShape(
                    topStartPercent = cornerRadius,
                    topEndPercent = 0,
                    bottomStartPercent = cornerRadius,
                    bottomEndPercent = 0
                )
                /**
                 * right outer button
                 */
                items.size - 1 -> RoundedCornerShape(
                    topStartPercent = 0,
                    topEndPercent = cornerRadius,
                    bottomStartPercent = 0,
                    bottomEndPercent = cornerRadius
                )
                /**
                 * middle button
                 */
                else -> RoundedCornerShape(
                    topStartPercent = 0,
                    topEndPercent = 0,
                    bottomStartPercent = 0,
                    bottomEndPercent = 0
                )
            },
            border = BorderStroke(
                1.dp, if (selectedIndex.value == index) {
                    color
                } else {
                    color.copy(alpha = 0.75f)
                }
            ),
            colors = if (selectedIndex.value == index) {
                ButtonDefaults.outlinedButtonColors(
                    containerColor = color
                )
            } else {
                ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
            },
        ) {
            Text(
                text = item,
                fontWeight = FontWeight.Normal,
                color = if (selectedIndex.value == index) {
                    Color.White
                } else {
                    color.copy(alpha = 0.9f)
                },
            )
        }
    }
}

@Preview
@Composable
fun SegmentedControlPreview() {
    val genders = listOf("2", "3", "4", "5")
    SegmentedControl(
        items = genders,
        defaultSelectedItemIndex = 0
    ) {

    }
}