package com.bnyro.clock.util

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.bnyro.clock.domain.model.ClockBackup
import kotlinx.serialization.json.Json
import java.io.File
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ClockBackupArchive {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    fun exportBackup(context: Context, uri: Uri, backup: ClockBackup) {
        val soundUris = (backup.alarms.map { it.soundUri } +
                backup.timers.map { it.soundUri } +
                backup.activeTimers.map { it.settings.soundUri })
            .filterNotNull().distinct().filter {
                it.isNotEmpty() && it.toUri().authority != "settings"
            }
        ZipOutputStream(requireNotNull(context.contentResolver.openOutputStream(uri))).use { zip ->
            val sounds = soundUris.associateWith { soundUri ->
                val name = "sounds/" + MessageDigest.getInstance("SHA-256")
                    .digest(soundUri.toByteArray()).joinToString("") { "%02x".format(it) }
                zip.putNextEntry(ZipEntry(name))
                requireNotNull(context.contentResolver.openInputStream(soundUri.toUri())).use {
                    it.copyTo(zip)
                }
                zip.closeEntry()
                name
            }
            zip.putNextEntry(ZipEntry("clock.json"))
            zip.write(json.encodeToString(backup.copy(sounds = sounds)).toByteArray())
            zip.closeEntry()
        }
    }

    fun importBackup(context: Context, uri: Uri): ClockBackup {
        val archive = File.createTempFile("clock-backup-", ".zip", context.cacheDir)
        try {
            requireNotNull(context.contentResolver.openInputStream(uri)).use { input ->
                archive.outputStream().use { input.copyTo(it) }
            }
            return ZipFile(archive).use { zip ->
                val backup = zip.getInputStream(requireNotNull(zip.getEntry("clock.json")))
                    .bufferedReader().use { json.decodeFromString<ClockBackup>(it.readText()) }
                require(backup.version == 1)
                val soundDirectory = File(context.filesDir, "clock_sounds").apply { mkdirs() }
                val restoredSounds = backup.sounds.mapValues { (_, name) ->
                    val entry = requireNotNull(zip.getEntry(name))
                    val staged = File.createTempFile("clock-sound-", ".audio", context.cacheDir)
                    try {
                        val digest = MessageDigest.getInstance("SHA-256")
                        DigestInputStream(zip.getInputStream(entry), digest).use { input ->
                            staged.outputStream().use { input.copyTo(it) }
                        }
                        val sound = File(soundDirectory, digest.digest().joinToString("") { "%02x".format(it) })
                        if (!sound.exists()) staged.copyTo(sound)
                        sound.toUri().toString()
                    } finally {
                        staged.delete()
                    }
                }
                backup.copy(
                    alarms = backup.alarms.map { it.copy(soundUri = restoredSounds[it.soundUri] ?: it.soundUri) },
                    timers = backup.timers.map { it.copy(soundUri = restoredSounds[it.soundUri] ?: it.soundUri) },
                    activeTimers = backup.activeTimers.map { timer ->
                        timer.copy(settings = timer.settings.copy(
                            soundUri = restoredSounds[timer.settings.soundUri] ?: timer.settings.soundUri
                        ))
                    }
                )
            }
        } finally {
            archive.delete()
        }
    }
}
