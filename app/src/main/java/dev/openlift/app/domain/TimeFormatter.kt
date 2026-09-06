package dev.openlift.app.domain

object TimeFormatter {

    fun duration(startMs: Long, endMs: Long?): String {
        val totalSeconds = ((endMs ?: System.currentTimeMillis()) - startMs) / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val secondsStr = seconds.toString().padStart(2, '0')
        if (minutes >= 60) {
            val hours = minutes / 60
            val mins = minutes % 60
            return "${hours}h ${mins}m"
        }
        return "${minutes}:${secondsStr}"
    }

    fun countdown(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    fun date(epochMs: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = epochMs
        return java.text.SimpleDateFormat("EEE, MMM d, yyyy").format(cal.time)
    }

    fun dateTime(epochMs: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = epochMs
        return java.text.SimpleDateFormat("MMM d, yyyy · HH:mm").format(cal.time)
    }
}
