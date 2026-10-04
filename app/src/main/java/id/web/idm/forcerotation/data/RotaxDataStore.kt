package id.web.idm.forcerotation.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.domain.OrientationSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rotax_settings")

class RotaxDataStore(private val context: Context) {

    private object Keys {
        val SELECTED_MODE = stringPreferencesKey("selected_mode")
        val HAS_ACTIVE_OVERRIDE = booleanPreferencesKey("has_active_override")
        val ORIGINAL_ACCELEROMETER = intPreferencesKey("original_accelerometer")
        val ORIGINAL_USER_ROTATION = intPreferencesKey("original_user_rotation")
        val SNAPSHOT_TIMESTAMP = longPreferencesKey("snapshot_timestamp")
        val LAST_WRITTEN_ACCELEROMETER = intPreferencesKey("last_written_accelerometer")
        val LAST_WRITTEN_USER_ROTATION = intPreferencesKey("last_written_user_rotation")
        val FLOATING_ENABLED = booleanPreferencesKey("floating_enabled")
        val FLOATING_POS_X = intPreferencesKey("floating_pos_x")
        val FLOATING_POS_Y = intPreferencesKey("floating_pos_y")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val FIRST_RUN_COMPLETED = booleanPreferencesKey("first_run_completed")
        val DARK_THEME = stringPreferencesKey("dark_theme")
        val SAFE_RESTORE_ENABLED = booleanPreferencesKey("safe_restore_enabled")
    }

    val selectedModeFlow: Flow<OrientationMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[Keys.SELECTED_MODE] ?: OrientationMode.AUTO.name
        try {
            OrientationMode.valueOf(modeStr)
        } catch (e: IllegalArgumentException) {
            OrientationMode.AUTO
        }
    }

    val hasActiveOverrideFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.HAS_ACTIVE_OVERRIDE] ?: false
    }

    val snapshotFlow: Flow<OrientationSnapshot?> = context.dataStore.data.map { preferences ->
        val accel = preferences[Keys.ORIGINAL_ACCELEROMETER]
        val rot = preferences[Keys.ORIGINAL_USER_ROTATION]
        val time = preferences[Keys.SNAPSHOT_TIMESTAMP]
        if (accel != null && rot != null && time != null) {
            OrientationSnapshot(accel, rot, time)
        } else {
            null
        }
    }

    val floatingEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.FLOATING_ENABLED] ?: false
    }

    val floatingPositionFlow: Flow<Pair<Int, Int>> = context.dataStore.data.map { preferences ->
        val x = preferences[Keys.FLOATING_POS_X] ?: 20
        val y = preferences[Keys.FLOATING_POS_Y] ?: 300
        Pair(x, y)
    }

    val notificationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.NOTIFICATION_ENABLED] ?: false
    }

    val firstRunCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.FIRST_RUN_COMPLETED] ?: false
    }

    val safeRestoreEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.SAFE_RESTORE_ENABLED] ?: true
    }

    val darkThemeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.DARK_THEME] ?: "dark"
    }

    suspend fun saveSelectedMode(mode: OrientationMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SELECTED_MODE] = mode.name
        }
    }

    suspend fun saveSnapshot(snapshot: OrientationSnapshot) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ORIGINAL_ACCELEROMETER] = snapshot.accelerometerRotation
            preferences[Keys.ORIGINAL_USER_ROTATION] = snapshot.userRotation
            preferences[Keys.SNAPSHOT_TIMESTAMP] = snapshot.timestamp
            preferences[Keys.HAS_ACTIVE_OVERRIDE] = true
        }
    }

    suspend fun saveLastWritten(accelerometer: Int, userRotation: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_WRITTEN_ACCELEROMETER] = accelerometer
            preferences[Keys.LAST_WRITTEN_USER_ROTATION] = userRotation
        }
    }

    suspend fun clearOverrideOwnership() {
        context.dataStore.edit { preferences ->
            preferences[Keys.HAS_ACTIVE_OVERRIDE] = false
            preferences.remove(Keys.LAST_WRITTEN_ACCELEROMETER)
            preferences.remove(Keys.LAST_WRITTEN_USER_ROTATION)
        }
    }

    suspend fun getOwnershipRecord(): OwnershipRecord {
        var record = OwnershipRecord(false, null, null, null, null)
        context.dataStore.edit { preferences ->
            val hasActive = preferences[Keys.HAS_ACTIVE_OVERRIDE] ?: false
            val lastAccel = preferences[Keys.LAST_WRITTEN_ACCELEROMETER]
            val lastRot = preferences[Keys.LAST_WRITTEN_USER_ROTATION]
            val origAccel = preferences[Keys.ORIGINAL_ACCELEROMETER]
            val origRot = preferences[Keys.ORIGINAL_USER_ROTATION]
            record = OwnershipRecord(hasActive, lastAccel, lastRot, origAccel, origRot)
        }
        return record
    }

    suspend fun setFloatingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.FLOATING_ENABLED] = enabled
        }
    }

    suspend fun saveFloatingPosition(x: Int, y: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.FLOATING_POS_X] = x
            preferences[Keys.FLOATING_POS_Y] = y
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setFirstRunCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.FIRST_RUN_COMPLETED] = completed
        }
    }

    suspend fun setSafeRestoreEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SAFE_RESTORE_ENABLED] = enabled
        }
    }

    suspend fun setDarkTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DARK_THEME] = theme
        }
    }
}

data class OwnershipRecord(
    val hasActiveOverride: Boolean,
    val lastWrittenAccelerometer: Int?,
    val lastWrittenUserRotation: Int?,
    val originalAccelerometer: Int?,
    val originalUserRotation: Int?
)
