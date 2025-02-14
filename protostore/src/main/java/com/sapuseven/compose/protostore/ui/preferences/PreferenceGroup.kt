package com.sapuseven.compose.protostore.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceGroup(
	title: String,
	modifier: Modifier = Modifier,
	children: (@Composable () -> Unit) = {},
) {
	Text(
		text = title,
		style = MaterialTheme.typography.labelMedium,
		color = MaterialTheme.colorScheme.primary,
		modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
	)
	Column(
		modifier = modifier
	) {
		children()
	}
	HorizontalDivider(
		color = MaterialTheme.colorScheme.outlineVariant,
		modifier = Modifier.padding(vertical = 8.dp)
	)
}
