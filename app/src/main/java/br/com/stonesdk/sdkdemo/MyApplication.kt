package br.com.stonesdk.sdkdemo

import android.app.Application
import br.com.stonesdk.sdkdemo.di.baseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(baseModule)
        }
    }
}