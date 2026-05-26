package com.example.afinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.afinal.data.repository.PlantRepository

/**
 * PlantViewModel Factory class used to create ViewModel instances with parameters.
 * The default ViewModelProvider.Factory doesn't support constructor arguments like PlantRepository.
 * This implementation enables dependency injection and decouples the ViewModel from the data layer.
 */
class PlantViewModelFactory(private val repository: PlantRepository) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given ViewModel class.
     * Validates the target ViewModel type and returns the instance with the injected Repository.
     * @param modelClass The class type of the ViewModel to create.
     * @return A constructed ViewModel instance.
     * @throws IllegalArgumentException if the ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested class is PlantViewModel
        if (modelClass.isAssignableFrom(PlantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantViewModel(repository) as T
        }
        // Unsupported ViewModel type
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
