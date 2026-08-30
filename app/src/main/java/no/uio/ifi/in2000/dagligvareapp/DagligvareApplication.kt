package no.uio.ifi.in2000.dagligvareapp

import android.app.Application
import no.uio.ifi.in2000.dagligvareapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DagligvareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DagligvareApplication)
            modules(appModule)
        }
    }
}
