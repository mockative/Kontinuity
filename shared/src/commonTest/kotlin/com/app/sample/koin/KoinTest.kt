package com.app.sample.koin

import com.app.sample.DefaultOtherService
import com.app.sample.OtherService
import io.mockative.kontinuity.koin.createKontinuityWrapper
import org.koin.core.annotation.KoinInternalApi
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.*

@OptIn(KoinInternalApi::class)
class KoinTest {
    @Test
    fun testModule() {
        val module = module {
            factory<OtherService> { DefaultOtherService() }
            factory { createKontinuityWrapper(OtherService::class) }
        }

        val app = koinApplication {
            modules(module)
        }

        val kontinuityWrapperKClass = StringKClass(
            qualifiedName = "com.app.sample.KOtherService",
            simpleName = "KOtherService"
        )

        val service = app.koin.get<OtherService>()
        assertFalse(kontinuityWrapperKClass.isInstance(service))

        val wrapper = app.koin.get<Any>(kontinuityWrapperKClass)
        assertNotNull(wrapper)
        assertIsNot<OtherService>(wrapper)
        assertTrue(kontinuityWrapperKClass.isInstance(wrapper))
    }
}
