package iti.yousef.skymood.data.repository

import android.app.Application
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.Exception

class LocationRepositoryTest {

    private lateinit var application: Application
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var repository: LocationRepository

    @Before
    fun setup() {
        application = mockk()
        fusedLocationClient = mockk()
        
        mockkStatic(LocationServices::class)
        every { LocationServices.getFusedLocationProviderClient(any<Application>()) } returns fusedLocationClient
        
        repository = LocationRepository(application)
    }

    @After
    fun teardown() {
        unmockkStatic(LocationServices::class)
    }

    @Test
    fun `getCurrentLocation should return location when successful`() = runTest {
        val mockLocation = mockk<Location>()
        val mockTask = mockk<Task<Location>>()
        
        every { fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, any()) } returns mockTask
        every { mockTask.isComplete } returns true
        every { mockTask.isCanceled } returns false
        every { mockTask.exception } returns null
        every { mockTask.result } returns mockLocation

        val result = repository.getCurrentLocation()
        
        assertEquals(mockLocation, result)
        verify { fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, any()) }
    }

    @Test
    fun `getCurrentLocation should return null when task fails`() = runTest {
        val mockTask = mockk<Task<Location>>()
        
        every { fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, any()) } returns mockTask
        every { mockTask.isComplete } returns true
        every { mockTask.isCanceled } returns false
        every { mockTask.exception } returns Exception("Location Error")
        
        val result = repository.getCurrentLocation()
        
        assertEquals(null, result)
    }
}
