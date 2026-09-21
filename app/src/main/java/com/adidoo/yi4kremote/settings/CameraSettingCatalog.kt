package com.adidoo.yi4kremote.settings

import com.adidoo.yi4k.sdk.YiProtocol

/**
 * Presentation-only catalog for the raw settings map the camera returns (~90 keys on the Yi 4K,
 * most of them undocumented). Lives in the app, not the SDK, because it's UI labels/grouping,
 * not protocol knowledge — and it's necessarily incomplete: only keys seen and confirmed so far
 * are curated here, everything else falls into [SettingCategory.AUTRES].
 */
enum class SettingCategory(val label: String) {
    VIDEO("Vidéo"),
    SYSTEME("Système"),
    STOCKAGE("Stockage"),
    ETAT("État"),
    AUTRES("Autres"),
}

data class SettingSpec(
    val label: String,
    val category: SettingCategory,
    val readOnly: Boolean = false,
)

private val KNOWN_SETTINGS: Map<String, SettingSpec> = mapOf(
    YiProtocol.KEY_VIDEO_RESOLUTION to SettingSpec("Résolution vidéo", SettingCategory.VIDEO),
    YiProtocol.KEY_VIDEO_QUALITY to SettingSpec("Qualité vidéo", SettingCategory.VIDEO),
    YiProtocol.KEY_VIDEO_STANDARD to SettingSpec("Standard vidéo", SettingCategory.VIDEO),
    YiProtocol.KEY_SYSTEM_MODE to SettingSpec("Mode système", SettingCategory.SYSTEME),
    YiProtocol.KEY_SD_CARD_STATUS to SettingSpec("État carte SD", SettingCategory.STOCKAGE, readOnly = true),
    YiProtocol.KEY_APP_STATUS to SettingSpec("État caméra", SettingCategory.ETAT, readOnly = true),
)

/** Falls back to [SettingCategory.AUTRES] read-only for any key not in [KNOWN_SETTINGS]. */
fun specFor(key: String): SettingSpec =
    KNOWN_SETTINGS[key] ?: SettingSpec(key, SettingCategory.AUTRES, readOnly = true)

/** Display order for [SettingCategory] sections in the settings screen. */
val CATEGORY_ORDER: List<SettingCategory> = listOf(
    SettingCategory.VIDEO,
    SettingCategory.SYSTEME,
    SettingCategory.STOCKAGE,
    SettingCategory.ETAT,
    SettingCategory.AUTRES,
)

fun groupSettings(settings: Map<String, String>): Map<SettingCategory, List<Pair<String, String>>> =
    settings.entries
        .groupBy { specFor(it.key).category }
        .mapValues { (_, entries) -> entries.map { it.key to it.value } }

private val SENSITIVE_KEY_MARKERS = listOf("password", "pwd", "psk", "secret", "serial")

/** Heuristic only — covers keys whose name suggests a credential or device-identifying value
 * (e.g. `sta_connect_password`, a camera serial number key), so the settings screen masks them
 * by default instead of showing them in plaintext. Not exhaustive: refine as real keys are seen. */
fun isSensitiveKey(key: String): Boolean =
    SENSITIVE_KEY_MARKERS.any { key.contains(it, ignoreCase = true) }
