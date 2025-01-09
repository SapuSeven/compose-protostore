# Compose-ProtoStore

A compose library for storing typed objects with Proto DataStore and building a matching UI.

Useful for application preferences including multi-profile support.

## Installation

_⚠ The library is not published yet - this is just a placeholder!_

```groovy
dependencies {
	implementation "com.sapuseven.compose:protostore:1.0.0"
	//...
}
```

## DataStore setup

### Setup Protobuf and DataStore

Set up Protobuf to generate code in your module-level `build.gradle`:

```groovy
plugins {
	id 'com.google.protobuf' version '0.9.4'
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:4.28.0"
	}

	// Generates the java Protobuf-lite code for the Protobufs in this project. See
	// https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
	// for more information.
	generateProtoTasks {
		all().each { task ->
			task.builtins {
				java {
					option 'lite'
				}
			}
		}
	}
}

dependencies {
	implementation "androidx.datastore:datastore:1.0.0"
	implementation "com.google.protobuf:protobuf-javalite:4.28.0"
	//...
}
```

### Define a schema

Proto DataStore requires a predefined schema in a proto file in the `app/src/main/proto/` directory.
This schema defines the type for the objects that you persist in your Proto DataStore.
To learn more about defining a proto schema, see the [protobuf language guide](https://developers.google.com/protocol-buffers/docs/proto3).

For an example implementation, see below.

### Create a Proto DataStore

#### Serializer

```kotlin
object SettingsSerializer : Serializer<Settings> {
  override val defaultValue: Settings = Settings.getDefaultInstance()

  override suspend fun readFrom(input: InputStream): Settings {
	try {
	  return Settings.parseFrom(input)
	} catch (exception: InvalidProtocolBufferException) {
	  throw CorruptionException("Cannot read proto", exception)
	}
  }

  override suspend fun writeTo(
	t: Settings,
	output: OutputStream) = t.writeTo(output)
}
```

#### DataStore Object

You have several options to construct a `DataStore` object.

The easiest is to use the `dataStore` delegate, with the `Context` as receiver (see [this tutorial](https://developer.android.com/codelabs/android-proto-datastore)):
```kotlin
private const val DATA_STORE_FILE_NAME = "settings.pb"

val Context.settingsDataStore: DataStore<Settings> by dataStore(
	fileName = DATA_STORE_FILE_NAME,
	serializer = SettingsSerializer,
	corruptionHandler = ReplaceFileCorruptionHandler {
		Settings.getDefaultInstance()
	}
)
```

If you are using Hilt, you can also take advantage of dependency injection to provide the DataStore object:
```kotlin
private const val DATA_STORE_FILE_NAME = "settings.pb"

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {
	@Singleton
	@Provides
	fun provideProtoDataStore(@ApplicationContext appContext: Context): DataStore<Settings> {
		return DataStoreFactory.create(
			serializer = SettingsSerializer,
			produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
			corruptionHandler = ReplaceFileCorruptionHandler {
				Settings.getDefaultInstance()
			},
			scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
		)
	}
}

// It can then be injected like this:
class SettingsRepository @Inject constructor(
	private val dataStore: DataStore<Settings>
)
```

> [!WARNING]
> Whichever way you choose to construct the DataStore object, _make sure to use a singleton_.
> Multiple instances of DataStore for one given file can break all DataStore functionality.

### Define a settings model and repository

> [!IMPORTANT]
> Since this library focuses on a use case where dependency injection is available,
> this section requires your app to utilize Hilt and ViewModels.
> It may be possible to implement it without these dependencies, but it is untested and currently not supported.

#### Single-user settings

For single-user preferences, you just need a schema that consists of an object:

```protobuf
syntax = "proto3";

option java_package = "com.example.application";
option java_multiple_files = true;

message Settings {
  bool exampleValue = 1;
}

```

Then you can define a ViewModel extending `SingleUserSettingsRepository` with your defined type:

```kotlin
@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
	dataStore: DataStore<Settings>
) : SingleUserSettingsRepository<Settings, Settings.Builder>(dataStore) {
	override fun getSettingsDefaults() = UserSettings.newBuilder().apply {
		// assign default values here
	}
}
```

#### Multi-user settings

For multi-user preferences, you need a schema that consists of two objects:
1. A holder for settings per user
2. A 'global' object with a map to the user-specific settings

```protobuf
syntax = "proto3";

option java_package = "com.example.application";
option java_multiple_files = true;

message UserSettings {
  bool exampleValue = 1;
}

message Settings {
  int64 activeUser = 1;
  map<int64, UserSettings> users = 2;
}
```

Then you can define a ViewModel extending `MultiUserSettingsRepository` with your defined types:

```kotlin
@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
	dataStore: DataStore<Settings>
) : MultiUserSettingsRepository<Settings, Settings.Builder, UserSettings, UserSettings.Builder>(dataStore) {
	private val userId = -1;// set your active user id here

	override fun getSettings(dataStore: Settings) : UserSettings {
		return dataStore.usersMap.getOrDefault(userId, getSettingsDefaults())
	}

	override fun getSettingsDefaults() = UserSettings.newBuilder().apply {
		// assign default values here
	}
	
	override fun updateSettings(currentData : Settings, userSettings: UserSettings) : Settings {
		return currentData.toBuilder()
			.putUsers(userId, userSettings)
			.build()
	}
}
```

## Building a settings screen

Once your model is defined, you can use the composables from the `com.sapuseven.protostore.ui.preferences` package
to build your settings screen:

```kotlin
@Composable
fun Settings() {
	VerticalScrollColumn {
		SwitchPreference(
			title = { Text("Example switch") },
			settingsRepository = viewModel,
			value = { it.exampleValue },
			onCheckedChange = { exampleValue = it }
		)
	}
}
```

## Accessing settings in your app

To access the stored values, you need to get an instance of your `SettingsRepository` implementation.

You can then call `getSettings()` to get a flow containing all settings, which will update if the settings are changed:

```kotlin
scope.launch {
    settingsRepository.getSettings().collect { settings ->
        val exampleValue = settings.exampleValue
        // TODO: Do something with exampleValue
    }
    
    // You can transform and process the flow however you need, for example:
    settingsRepository.getSettings().map { it.exampleValue }.first()
}
```

## About

### A note on naming

In this library, the term "Settings" is used to refer to data-related objects and configurations,
in line with modern practices and components like Proto DataStore.

However, for consistency and familiarity, the term "Preferences" is still used to describe user interface elements,
such as `Preference` and `PreferenceGroup`.

In summary, a "Preference" is a UI element, while "Settings" pertains to the stored data.

This approach ensures familiarity with traditional terminology while incorporating updated standards for data management.
