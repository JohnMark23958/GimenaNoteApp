package ph.edu.comteq.gimena

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object DateUtils {
    private val dateFormat =
        SimpleDateFormat("MM dd, yyyy", Locale.getDefault())
    private val timeFormat =
        SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateTimeFormat =
        SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
    fun formatDate(timestamp: Long) : String{
        return dateFormat.format(Date(timestamp))
    }
    fun formatTime(timestamp: Long) : String{
        return timeFormat.format(Date(timestamp))
    }
    fun formatDateTime(timestamp: Long) : String{
        return dateTimeFormat.format(Date(timestamp))
    }

}