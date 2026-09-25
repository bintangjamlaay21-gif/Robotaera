package com.aera.robot.robot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class RobotState {
    SLEEPING, IDLE, LISTENING, THINKING, SPEAKING, HAPPY, SAD, SURPRISED, CONFUSED, CURIOUS
}

class RobotController {
    var robotState by mutableStateOf(RobotState.SLEEPING)
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    fun wakeUp(onComplete: () -> Unit = {}) {
        scope.launch {
            robotState = RobotState.SLEEPING
            delay(800)
            robotState = RobotState.IDLE
            onComplete()
        }
    }

    fun setState(state: RobotState) {
        robotState = state
    }

    fun temporaryState(state: RobotState, durationMs: Long = 2000L, then: RobotState = RobotState.IDLE) {
        scope.launch {
            robotState = state
            delay(durationMs)
            robotState = then
        }
    }
}
