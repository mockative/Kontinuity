package io.mockative.kontinuity.viewmodel

data class Task(val id: String)

class TaskViewModel : PlatformViewModel() {
    var tasks: List<Task> by mutableStateOf(emptyList())
        private set
}