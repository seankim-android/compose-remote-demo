package dev.seankim.composeremote.client

// Action IDs are the contract between server and client. The server emits
// these on tappable elements via HostAction(id); the client decides what each
// id means. Keep these in sync with server/.../compositions/Variants.kt.
object RemoteActions {
    // 1xxx - In-app intents on the home tree.
    const val READ_FEATURED = 1001
    const val SAVE_FOR_LATER = 1002
    const val CATCH_ME_UP = 1003
    const val REFRESH = 1004

    // 2xxx - Navigate to item N (id - ITEM_OFFSET).
    private const val ITEM_OFFSET = 2000
    val ITEM_RANGE: IntRange = 2001..2099
    fun itemIdFor(actionId: Int): Int = actionId - ITEM_OFFSET

    // 3xxx - Intents on item detail.
    const val MARK_READ = 3001
    const val OPEN_IN_SOURCE = 3002
}
