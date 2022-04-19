package com.app.sample.koin

import com.app.sample.DefaultOtherService
import com.app.sample.OtherService
import io.mockative.kontinuity.createKontinuityWrapper
import io.mockative.kontinuity.getKontinuityWrapperClass
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.*

class KoinTest {
    @Test
    fun testModule() {
        // Given
        val module = module {
            factory<OtherService> { DefaultOtherService() }
            factory { createKontinuityWrapper(get<OtherService>()) }
        }

        val app = koinApplication {
            modules(module)
        }

        val service = app.koin.get<OtherService>()
        val kontinuityWrapperClass = OtherService::class.getKontinuityWrapperClass()

        // When
        val wrapper = app.koin.get<Any>(kontinuityWrapperClass)

        // Then
        assertNotNull(wrapper)
        assertIsNot<OtherService>(wrapper)
        assertFalse(kontinuityWrapperClass.isInstance(service))
        assertTrue(kontinuityWrapperClass.isInstance(wrapper))
    }

    @Test
    fun testGetClass() {
        val unwrappedClass = OtherService::class
        val wrapperClass = unwrappedClass.getKontinuityWrapperClass()
        assertEquals("com.app.sample.KOtherService", wrapperClass.qualifiedName)
    }
}
