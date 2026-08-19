package com.spinel.zicola.zicola.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zicola_settings")

class PreferencesManager(private val context: Context) {

    val fontSizeFlow: Flow<Int> = context.dataStore.data.map { it[FONT_SIZE] ?: 20 }
    val lineSpacingFlow: Flow<Float> = context.dataStore.data.map { it[LINE_SPACING] ?: 1.8f }
    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME] ?: "SEPIA" }
    val appThemeFlow: Flow<String> = context.dataStore.data.map { it[APP_THEME] ?: "SYSTEM" }

    suspend fun saveAppTheme(theme: String) {
        context.dataStore.edit { it[APP_THEME] = theme }
    }

    suspend fun saveFontSize(size: Int) {
        context.dataStore.edit { it[FONT_SIZE] = size }
    }

    suspend fun saveLineSpacing(spacing: Float) {
        context.dataStore.edit { it[LINE_SPACING] = spacing }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }

    fun getBookProgress(bookId: String): Flow<Float> = context.dataStore.data.map { it[floatPreferencesKey("${bookId}_progress")] ?: 0f }
    fun getBookBlockIndex(bookId: String): Flow<Int> = context.dataStore.data.map { it[intPreferencesKey("${bookId}_block")] ?: 0 }
    fun getBookScrollOffset(bookId: String): Flow<Int> = context.dataStore.data.map { it[intPreferencesKey("${bookId}_offset")] ?: 0 }

    suspend fun saveBookProgress(bookId: String, blockIndex: Int, offset: Int, progress: Float) {
        context.dataStore.edit {
            it[intPreferencesKey("${bookId}_block")] = blockIndex
            it[intPreferencesKey("${bookId}_offset")] = offset
            it[floatPreferencesKey("${bookId}_progress")] = progress
        }
    }


    fun getBookmarkChapter(bookId: String): Flow<Int> = context.dataStore.data.map { it[intPreferencesKey("${bookId}_bookmark_chapter")] ?: -1 }
    fun getBookmarkOffset(bookId: String): Flow<Int> = context.dataStore.data.map { it[intPreferencesKey("${bookId}_bookmark_offset")] ?: -1 }

    suspend fun saveBookmark(bookId: String, chapterIndex: Int, offset: Int) {
        context.dataStore.edit {
            it[intPreferencesKey("${bookId}_bookmark_chapter")] = chapterIndex
            it[intPreferencesKey("${bookId}_bookmark_offset")] = offset
        }
    }

    
    suspend fun getOrCreateDeviceId(): String {
        val preferences = context.dataStore.data.first()
        val existingId = preferences[DEVICE_ID]
        if (existingId != null) {
            return existingId
        }
        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { it[DEVICE_ID] = newId }
        return newId
    }

    companion object {
        private val FONT_SIZE = intPreferencesKey("font_size")
        private val LINE_SPACING = floatPreferencesKey("line_spacing")
        private val THEME = stringPreferencesKey("theme")
        private val APP_THEME = stringPreferencesKey("app_theme")
        private val DEVICE_ID = stringPreferencesKey("device_id")
    }
}
