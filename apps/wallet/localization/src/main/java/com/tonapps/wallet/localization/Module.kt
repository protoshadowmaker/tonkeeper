package com.tonapps.wallet.localization

import org.koin.dsl.module

val localizationModule = module {
    single { LocalizationRepository(get()) }
}