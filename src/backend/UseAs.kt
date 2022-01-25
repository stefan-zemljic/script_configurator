package backend

internal inline fun <T : AutoCloseable, R> T.useAs(block: T.() -> R): R{
    @Suppress("ConvertTryFinallyToUseCall")
    try {
        return block()
    } finally {
        close()
    }
}