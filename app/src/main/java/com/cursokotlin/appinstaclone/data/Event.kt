package com.cursokotlin.appinstaclone.data

// I created this because of our MutableState in our ViewModel.
// When the screen is refreshed will still display the same message again and again.
//So, Event is a special data type that allow us to display a message once.
/**
 * This generic class represents an event that can be consumed only once.
 *
 * @param T The type of content that the event holds.
 * @property content The content of the event.
 */
open class Event<out T>(private val content: T) {

    // Flag to track whether the event has been handled
    var hasBeenHandled = false
        private set

    /**
     * Retrieves the content of the event if it hasn't been handled yet.
     * Once the content is retrieved, the event is marked as handled.
     *
     * @return The content of the event, or null if the event has been handled.
     */
    fun getContentOrNull(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}