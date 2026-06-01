package ar.edu.unlam.mobile.scaffolding.data.datasources.device.sensor

import android.app.Service
import android.content.Intent
import android.os.IBinder

class StepCounterService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
