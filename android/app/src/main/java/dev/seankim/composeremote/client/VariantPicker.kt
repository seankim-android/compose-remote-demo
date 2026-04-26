package dev.seankim.composeremote.client

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantPicker(
    selected: Variant,
    onSelect: (Variant) -> Unit,
    modifier: Modifier = Modifier,
) {
    val variants = Variant.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        variants.forEachIndexed { index, v ->
            SegmentedButton(
                selected = v == selected,
                onClick = { onSelect(v) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = variants.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    activeBorderColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(v.label)
            }
        }
    }
}
