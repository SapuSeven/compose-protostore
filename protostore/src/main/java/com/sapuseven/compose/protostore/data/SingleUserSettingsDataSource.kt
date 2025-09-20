package com.sapuseven.compose.protostore.data

import androidx.datastore.core.DataStore
import com.google.protobuf.MessageLite
import kotlinx.coroutines.flow.Flow

abstract class SingleUserSettingsDataSource <
	SettingsType : MessageLite,
	SettingsBuilderType : MessageLite.Builder
	> (
	private val _dataStore: DataStore<SettingsType>
) : SettingsDataSource<SettingsType, SettingsBuilderType> {
	override fun getSettings(): Flow<SettingsType> {
		return _dataStore.data
	}

	@Suppress("UNCHECKED_CAST")
	override suspend fun updateSettings(update: SettingsBuilderType.() -> Unit) {
		_dataStore.updateData { currentData ->
			val settingsBuilder = currentData.toBuilder() as SettingsBuilderType
			settingsBuilder.apply(update).build() as SettingsType
		}
	}
}
