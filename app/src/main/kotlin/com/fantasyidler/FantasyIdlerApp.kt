package com.fantasyidler

import android.app.Application
import com.fantasyidler.notification.SessionNotificationManager
import com.fantasyidler.repository.GameDataRepository
import com.fantasyidler.simulator.TypeRegistry
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FantasyIdlerApp : Application() {

    @Inject lateinit var notificationManager: SessionNotificationManager
    @Inject lateinit var gameDataRepository: GameDataRepository

    override fun onCreate() {
        super.onCreate()
        notificationManager.createChannels()
        TypeRegistry.init(gameDataRepository.typeEffectiveness)
    }
}
