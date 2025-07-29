package com.sapuseven.compose.protostore.data

import com.google.protobuf.MessageLite
import kotlinx.coroutines.flow.Flow

interface SettingsDataSource<SettingsType : MessageLite, SettingsBuilderType : MessageLite.Builder> {
	fun getSettings(): Flow<SettingsType>

	suspend fun updateSettings(update: SettingsBuilderType.() -> Unit)
}
