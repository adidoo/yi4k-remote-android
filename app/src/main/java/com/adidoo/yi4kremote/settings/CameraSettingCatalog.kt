package com.adidoo.yi4kremote.settings

import com.adidoo.yi4k.sdk.YiProtocol

/**
 * Presentation-only catalog for the raw settings map the camera returns (~90 keys on the Yi 4K,
 * most of them undocumented). Lives in the app, not the SDK, because it's UI labels/grouping,
 * not protocol knowledge — and it's necessarily incomplete: only keys seen and confirmed so far
 * (during a live test against a real Yi 4K, firmware Z16V13L_1.10.9) are curated here, everything
 * else falls into [SettingCategory.AUTRES].
 */
enum class SettingCategory(val label: String) {
    VIDEO("Vidéo"),
    PHOTO("Photo"),
    IMAGE("Qualité d'image"),
    SON("Son"),
    RESEAU("Réseau"),
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
    // Vidéo
    YiProtocol.KEY_VIDEO_RESOLUTION to SettingSpec("Résolution vidéo", SettingCategory.VIDEO),
    YiProtocol.KEY_VIDEO_QUALITY to SettingSpec("Qualité vidéo", SettingCategory.VIDEO),
    YiProtocol.KEY_VIDEO_STANDARD to SettingSpec("Standard vidéo", SettingCategory.VIDEO),
    "video_stamp" to SettingSpec("Horodatage vidéo", SettingCategory.VIDEO),
    "video_rotate" to SettingSpec("Rotation vidéo", SettingCategory.VIDEO),
    "video_sharpness" to SettingSpec("Netteté vidéo", SettingCategory.VIDEO),
    "video_flat_color" to SettingSpec("Couleurs plates (Flat)", SettingCategory.VIDEO),
    "video_file_max_size" to SettingSpec("Taille max. fichier vidéo", SettingCategory.VIDEO),
    "timelapse_video" to SettingSpec("Vidéo timelapse activée", SettingCategory.VIDEO),
    "timelapse_video_duration" to SettingSpec("Durée timelapse", SettingCategory.VIDEO),
    "timelapse_video_resolution" to SettingSpec("Résolution timelapse", SettingCategory.VIDEO),
    "video_photo_resolution" to SettingSpec("Résolution photo pendant vidéo", SettingCategory.VIDEO),
    "slow_motion_rate" to SettingSpec("Facteur ralenti", SettingCategory.VIDEO),
    "slow_motion_res" to SettingSpec("Résolution ralenti", SettingCategory.VIDEO),
    "loop_rec_duration" to SettingSpec("Durée enregistrement en boucle", SettingCategory.VIDEO),
    "rec_mode" to SettingSpec("Mode d'enregistrement", SettingCategory.VIDEO),
    "record_photo_time" to SettingSpec("Intervalle photo pendant vidéo", SettingCategory.VIDEO),
    "timelapse_photo_shutter" to SettingSpec("Vitesse d'obturation timelapse", SettingCategory.VIDEO),
    "long_shutter_define" to SettingSpec("Comportement pose longue", SettingCategory.VIDEO),

    // Photo
    "capture_mode" to SettingSpec("Mode de capture", SettingCategory.PHOTO),
    "photo_size" to SettingSpec("Taille photo", SettingCategory.PHOTO),
    "photo_stamp" to SettingSpec("Horodatage photo", SettingCategory.PHOTO),
    "photo_quality" to SettingSpec("Qualité photo", SettingCategory.PHOTO),
    "photo_sharpness" to SettingSpec("Netteté photo", SettingCategory.PHOTO),
    "photo_file_type" to SettingSpec("Format de fichier photo", SettingCategory.PHOTO),
    "photo_file_type_settable" to SettingSpec("Format de fichier modifiable", SettingCategory.PHOTO, readOnly = true),
    "precise_cont_time" to SettingSpec("Durée rafale précise", SettingCategory.PHOTO),
    "precise_cont_poweroff" to SettingSpec("Extinction après rafale précise", SettingCategory.PHOTO),
    "precise_cont_poweroff_settable" to SettingSpec("Extinction après rafale modifiable", SettingCategory.PHOTO, readOnly = true),
    "precise_cont_capturing" to SettingSpec("Rafale précise en cours", SettingCategory.PHOTO, readOnly = true),
    "precise_selftime" to SettingSpec("Retardateur", SettingCategory.PHOTO),
    "precise_self_running" to SettingSpec("Retardateur en cours", SettingCategory.PHOTO, readOnly = true),
    "precise_self_remain_time" to SettingSpec("Temps restant retardateur", SettingCategory.PHOTO, readOnly = true),
    "piv_enable" to SettingSpec("PIV (photo pendant vidéo)", SettingCategory.PHOTO),

    // Qualité d'image
    "iq_eis_enable" to SettingSpec("Stabilisation électronique (EIS)", SettingCategory.IMAGE),
    "iq_photo_iso_min" to SettingSpec("ISO min. photo", SettingCategory.IMAGE),
    "iq_photo_iso" to SettingSpec("ISO photo", SettingCategory.IMAGE),
    "iq_video_iso" to SettingSpec("ISO vidéo", SettingCategory.IMAGE),
    "iq_photo_shutter" to SettingSpec("Vitesse d'obturation photo", SettingCategory.IMAGE),
    "iq_photo_ev" to SettingSpec("Exposition photo (EV)", SettingCategory.IMAGE),
    "iq_video_ev" to SettingSpec("Exposition vidéo (EV)", SettingCategory.IMAGE),
    "iq_photo_wb" to SettingSpec("Balance des blancs photo", SettingCategory.IMAGE),
    "iq_video_wb" to SettingSpec("Balance des blancs vidéo", SettingCategory.IMAGE),
    "protune" to SettingSpec("Protune", SettingCategory.IMAGE),
    "ev_enable" to SettingSpec("Correction d'exposition activée", SettingCategory.IMAGE),
    "stamp_enable" to SettingSpec("Horodatage activé", SettingCategory.IMAGE),
    "fov" to SettingSpec("Champ de vision (FOV)", SettingCategory.IMAGE),
    "support_fov" to SettingSpec("FOV réglable disponible", SettingCategory.IMAGE, readOnly = true),
    "support_iso" to SettingSpec("ISO réglable disponible", SettingCategory.IMAGE, readOnly = true),
    "support_wb" to SettingSpec("Balance des blancs réglable disponible", SettingCategory.IMAGE, readOnly = true),
    "support_flat_color" to SettingSpec("Couleurs plates disponibles", SettingCategory.IMAGE, readOnly = true),
    "support_sharpness" to SettingSpec("Netteté réglable disponible", SettingCategory.IMAGE, readOnly = true),
    "dewarp_support_status" to SettingSpec("Correction de distorsion disponible", SettingCategory.IMAGE, readOnly = true),
    "warp_enable" to SettingSpec("Correction de distorsion activée", SettingCategory.IMAGE),
    "auto_low_light" to SettingSpec("Basse lumière automatique", SettingCategory.IMAGE),
    "support_auto_low_light" to SettingSpec("Basse lumière auto disponible", SettingCategory.IMAGE, readOnly = true),
    "meter_mode" to SettingSpec("Mode de mesure", SettingCategory.IMAGE),

    // Son
    "buzzer_volume" to SettingSpec("Volume du bip", SettingCategory.SON),
    "buzzer_ring" to SettingSpec("Bip sonore", SettingCategory.SON),
    "sound_effect" to SettingSpec("Effet sonore", SettingCategory.SON),
    "sound_effect_support" to SettingSpec("Effet sonore disponible", SettingCategory.SON, readOnly = true),
    "rec_audio_support" to SettingSpec("Enregistrement audio disponible", SettingCategory.SON, readOnly = true),

    // Réseau — lecture seule par prudence : la SDK suppose une IP AP fixe (192.168.42.1),
    // modifier wifi_mode/sta_* depuis l'app pourrait faire basculer la caméra en mode client
    // et casser cette hypothèse (perte de connexion).
    "sta_connect_password" to SettingSpec("Mot de passe Wi-Fi (mode station)", SettingCategory.RESEAU, readOnly = true),
    "sta_ip" to SettingSpec("IP (mode station)", SettingCategory.RESEAU, readOnly = true),
    "wifi_mode" to SettingSpec("Mode Wi-Fi", SettingCategory.RESEAU, readOnly = true),
    "wifi_country" to SettingSpec("Pays Wi-Fi", SettingCategory.RESEAU, readOnly = true),
    "wifi_country_editable" to SettingSpec("Pays Wi-Fi modifiable", SettingCategory.RESEAU, readOnly = true),

    // Système
    YiProtocol.KEY_SYSTEM_MODE to SettingSpec("Mode système", SettingCategory.SYSTEME),
    "sw_version" to SettingSpec("Version logicielle", SettingCategory.SYSTEME, readOnly = true),
    "hw_version" to SettingSpec("Version matérielle", SettingCategory.SYSTEME, readOnly = true),
    "product_name" to SettingSpec("Modèle", SettingCategory.SYSTEME, readOnly = true),
    "dev_functions" to SettingSpec("Fonctions développeur", SettingCategory.SYSTEME, readOnly = true),
    "system_default_mode" to SettingSpec("Mode par défaut au démarrage", SettingCategory.SYSTEME),
    "sdcard_need_format" to SettingSpec("Formatage carte SD nécessaire", SettingCategory.SYSTEME, readOnly = true),
    "camera_clock" to SettingSpec("Horloge caméra", SettingCategory.SYSTEME, readOnly = true),
    "screen_auto_lock" to SettingSpec("Verrouillage écran automatique", SettingCategory.SYSTEME),
    "auto_power_off" to SettingSpec("Extinction automatique", SettingCategory.SYSTEME),
    "quick_view" to SettingSpec("Aperçu rapide", SettingCategory.SYSTEME),
    "dual_stream_status" to SettingSpec("Flux double", SettingCategory.SYSTEME, readOnly = true),
    "led_mode" to SettingSpec("Mode LED", SettingCategory.SYSTEME),
    "language" to SettingSpec("Langue", SettingCategory.SYSTEME),

    // Stockage / État
    YiProtocol.KEY_SD_CARD_STATUS to SettingSpec("État carte SD", SettingCategory.STOCKAGE, readOnly = true),
    YiProtocol.KEY_APP_STATUS to SettingSpec("État caméra", SettingCategory.ETAT, readOnly = true),
)

/** Falls back to [SettingCategory.AUTRES] read-only for any key not in [KNOWN_SETTINGS]. */
fun specFor(key: String): SettingSpec =
    KNOWN_SETTINGS[key] ?: SettingSpec(key, SettingCategory.AUTRES, readOnly = true)

/** Display order for [SettingCategory] sections in the settings screen. */
val CATEGORY_ORDER: List<SettingCategory> = listOf(
    SettingCategory.VIDEO,
    SettingCategory.PHOTO,
    SettingCategory.IMAGE,
    SettingCategory.SON,
    SettingCategory.RESEAU,
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
