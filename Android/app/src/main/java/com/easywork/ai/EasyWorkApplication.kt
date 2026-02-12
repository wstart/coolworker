package com.easywork.ai

import android.app.Application
import com.easywork.ai.di.AppContainer

/**
 * EasyWork with AI Application类
 */
class EasyWorkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化依赖注入容器
        AppContainer.init(applicationContext)
    }
}
