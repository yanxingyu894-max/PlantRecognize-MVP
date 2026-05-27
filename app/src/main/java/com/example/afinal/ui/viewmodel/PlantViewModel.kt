package com.example.afinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.model.Plant
import com.example.afinal.data.repository.PlantRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class PlantViewModel(private val repository: PlantRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _loggedInUserId = MutableStateFlow<String?>(null)
    val loggedInUserId: StateFlow<String?> = _loggedInUserId.asStateFlow()

    // 观察仓库中的当前用户 ID
    val currentUserId: StateFlow<String> = repository.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlantRepository.GUEST_USER_ID)

    // Persistent Daily Plant to prevent refreshing on every HomeScreen recomposition
    private val _dailyPlant = MutableStateFlow<Plant?>(null)
    val dailyPlant: StateFlow<Plant?> = _dailyPlant.asStateFlow()

    val hasPendingDetails: StateFlow<Boolean> = repository.hasPendingDetails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _isSyncingDetails = MutableStateFlow(false)
    val isSyncingDetails: StateFlow<Boolean> = _isSyncingDetails.asStateFlow()

    private var syncJob: Job? = null

    private val _externalSearchResults = MutableStateFlow<List<Plant>>(emptyList())
    val externalSearchResults: StateFlow<List<Plant>> = _externalSearchResults.asStateFlow()

    private val _isExternalSearching = MutableStateFlow(false)
    val isExternalSearching: StateFlow<Boolean> = _isExternalSearching.asStateFlow()

    val allPlants: StateFlow<List<Plant>> = combine(
        _searchQuery,
        _selectedCategory,
        repository.allPlants, // 这是来自数据库的响应式流
        _externalSearchResults
    ) { query, category, localPlants, external ->
        val localFiltered = localPlants.filter { plant ->
            val hasImage = plant.imageUrl.isNotBlank()
            val matchesQuery = plant.name.contains(query, ignoreCase = true) ||
                    plant.alias.contains(query, ignoreCase = true)
            val matchesCategory = category == null || plant.category == category
            hasImage && matchesQuery && matchesCategory
        }

        // 修改：当进行搜索时，合并本地过滤结果和外部搜索结果
        val baseList = if (query.isNotBlank()) {
            (localFiltered + external).distinctBy { it.id }
        } else {
            localFiltered
        }

        // 核心修复：将 baseList 中的收藏状态与数据库中的最新状态同步
        val favoriteStatusMap = localPlants.associate { it.id to it.isFavorite }
        baseList.map { plant ->
            val currentFavorite = favoriteStatusMap[plant.id] ?: plant.isFavorite
            plant.copy(isFavorite = currentFavorite)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoritePlants: StateFlow<List<Plant>> = repository.favoritePlants
        .map { list -> 
            list.filter { it.imageUrl.isNotBlank() } 
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val initialPlants: StateFlow<List<Plant>> = repository.allPlants
        .map { list ->
            val filtered = list.filter { it.imageUrl.isNotBlank() }
            if (filtered.size <= 100) filtered else filtered.take(100)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _recognitionResult = MutableStateFlow<Result<Plant>?>(null)
    val recognitionResult: StateFlow<Result<Plant>?> = _recognitionResult.asStateFlow()

    private val _recognitionInProgress = MutableStateFlow(false)
    val recognitionInProgress: StateFlow<Boolean> = _recognitionInProgress.asStateFlow()

    private val _selectedImagePath = MutableStateFlow<String?>(null)
    val selectedImagePath: StateFlow<String?> = _selectedImagePath.asStateFlow()

    init {
        loadInitialPlants()

        viewModelScope.launch {
            repository.allPlants.collect { list ->
                val validPlants = list.filter { it.imageUrl.isNotBlank() }
                if (validPlants.isNotEmpty() && _dailyPlant.value == null) {
                    _dailyPlant.value = validPlants.random()
                }
            }
        }
    }

    private var externalSearchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _externalSearchResults.value = emptyList()
            externalSearchJob?.cancel()
        }
    }

    fun performExternalSearch() {
        val query = _searchQuery.value
        if (query.isBlank()) return

        externalSearchJob?.cancel()
        externalSearchJob = viewModelScope.launch {
            _isExternalSearching.value = true
            val result = repository.searchExternalPlant(query)
            if (result.isSuccess) {
                _externalSearchResults.value = result.getOrDefault(emptyList())
            }
            _isExternalSearching.value = false
        }
    }

    fun onCategorySelect(category: String?) {
        _selectedCategory.value = category
    }

    fun loadInitialPlants() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val list = repository.allPlants.first()
            if (list.isEmpty()) {
                repository.refreshPlants(pageSize = 100)
            }
            _isRefreshing.value = false
            startGradualDetailSync()
        }
    }

    fun refreshInitialPlants() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshPlants(pageSize = 100)
            _isRefreshing.value = false
            startGradualDetailSync()
        }
    }

    private fun startGradualDetailSync() {
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            _isSyncingDetails.value = true
            repository.fetchPendingDetails(immediate = false)
            _isSyncingDetails.value = false
        }
    }

    fun syncAllPendingDetailsNow() {
        syncJob?.cancel() 
        syncJob = viewModelScope.launch {
            _isSyncingDetails.value = true
            repository.fetchPendingDetails(immediate = true)
            _isSyncingDetails.value = false
        }
    }

    suspend fun registerUser(username: String, password: String, agree: Boolean): Result<Unit> {
        return repository.registerUser(username, password, agree)
    }

    suspend fun loginUser(username: String, password: String): Result<Unit> {
        val result = repository.loginUser(username, password)
        if (result.isSuccess) {
            _loggedInUserId.value = username
            // 登录后尝试加载新用户的数据（如果为空则抓取）
            loadInitialPlants()
        }
        return result
    }

    fun logout() {
        // 1. 重置搜索和过滤状态，防止因筛选条件不匹配导致列表为空
        _searchQuery.value = ""
        _selectedCategory.value = null
        _externalSearchResults.value = emptyList()

        // 2. 调用仓库登出，切换 currentUserId 为 guest_user
        repository.logout()
        _loggedInUserId.value = null

        // 3. 清空每日推荐，让它在切换到游客数据后重新随机选择
        _dailyPlant.value = null

        // 4. 重新加载/同步游客数据
        loadInitialPlants()
    }

    fun toggleFavorite(plant: Plant) {
        viewModelScope.launch { repository.updateFavoriteStatus(plant.id, !plant.isFavorite) }
    }

    fun setSelectedImagePath(path: String?) {
        _selectedImagePath.value = path
    }

    fun identifyPlant(imageFile: File) {
        _selectedImagePath.value = imageFile.absolutePath
        _recognitionInProgress.value = true
        viewModelScope.launch {
            _recognitionResult.value = repository.identifyAndFetchDetails(imageFile)
            _recognitionInProgress.value = false
        }
    }

    fun resetRecognition() {
        _recognitionResult.value = null
        _recognitionInProgress.value = false
        _selectedImagePath.value = null
    }

    suspend fun getPlantById(id: String): Plant? {
        return repository.getPlantBySlug(id)
    }
}
