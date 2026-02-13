import kotlinx.datetime.Clock

actual fun currentDate(): DateString {
    return DateString(Clock.System.now().toString())
}
