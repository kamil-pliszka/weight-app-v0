package com.pl.myweightapp.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pl.myweightapp.R

@Composable
fun <T : Enum<T>> EnumDropdownButton(
    modifier: Modifier = Modifier,
    selected: T?,
    emptySelectedText: String = stringResource(R.string.enum_dropdown_choose),
    items: Array<T>,
    getLabel: @Composable (T) -> String,
    menuWidth: Dp? = null,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Box(modifier = modifier) {
        Button(onClick = { expanded = !expanded }) {
            Text(selected?.let { getLabel(it) } ?: emptySelectedText)
            Spacer(Modifier.width(8.dp))
            Icon(
                //imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            Modifier.width(menuWidth ?: Dp.Unspecified)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = getLabel(item), maxLines = 1) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    },
                )
            }
        }
    }
}
