package id.web.idm.forcerotation.domain

enum class OrientationMode {
    AUTO,
    PORTRAIT,
    LANDSCAPE;

    fun next(): OrientationMode = when (this) {
        AUTO -> PORTRAIT
        PORTRAIT -> LANDSCAPE
        LANDSCAPE -> AUTO
    }

    val displayName: String
        get() = when (this) {
            AUTO -> "Auto"
            PORTRAIT -> "Portrait"
            LANDSCAPE -> "Landscape"
        }
}
