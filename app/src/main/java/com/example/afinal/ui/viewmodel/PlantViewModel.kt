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

/**
 * 植物ViewModel：管理所有页面的植物数据和业务逻辑
 * 角色：连接UI页面和数据仓库（Repository），处理数据请求、状态管理、用户交互逻辑
 * 特点：
 * 1. 生命周期感知：跟随页面/活动生命周期，避免内存泄漏
 * 2. 协程支持：通过viewModelScope处理异步操作（比如网络请求、数据库读写）
 * 3. 状态流（StateFlow）：让UI能实时监听数据变化（比如搜索结果、收藏状态）
 * @param repository 数据仓库：提供数据读写的统一接口（本地+远程）
 */
class PlantViewModel(private val repository: PlantRepository) : ViewModel() {
    // ===== 私有可修改的状态（MutableStateFlow）：仅内部能修改 =====
    // 搜索关键词：用户在搜索框输入的内容
    private val _searchQuery = MutableStateFlow("")
    // 公开只读的状态（StateFlow）：UI只能监听，不能修改
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 选中的分类：用户选择的植物分类（比如“花卉”、“树木”）
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // 登录用户ID：当前登录的用户账号（未登录则为null）
    private val _loggedInUserId = MutableStateFlow<String?>(null)
    val loggedInUserId: StateFlow<String?> = _loggedInUserId.asStateFlow()

    // ===== 公开只读的状态：供UI监听 =====
    // 当前用户ID：从仓库监听用户状态（游客ID/登录用户ID）
    // stateIn：将仓库的数据流转换为ViewModel生命周期感知的状态流
    // SharingStarted.Eagerly：立即启动监听，保持活跃
    val currentUserId: StateFlow<String> = repository.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlantRepository.GUEST_USER_ID)

    // 每日推荐植物：避免首页每次重组都刷新（提升性能）
    private val _dailyPlant = MutableStateFlow<Plant?>(null)
    val dailyPlant: StateFlow<Plant?> = _dailyPlant.asStateFlow()

    // 是否有未同步的植物详情：监听仓库的未同步状态
    val hasPendingDetails: StateFlow<Boolean> = repository.hasPendingDetails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 订阅者取消后，5秒内保持活跃
            initialValue = false // 初始值：无未同步数据
        )

    // 是否正在同步植物详情：标记后台同步状态（用于显示加载动画）
    private val _isSyncingDetails = MutableStateFlow(false)
    val isSyncingDetails: StateFlow<Boolean> = _isSyncingDetails.asStateFlow()

    // 同步任务：管理后台同步的协程任务（用于取消/重启）
    private var syncJob: Job? = null

    // 外部搜索结果：从网络接口搜索到的植物列表
    private val _externalSearchResults = MutableStateFlow<List<Plant>>(emptyList())
    val externalSearchResults: StateFlow<List<Plant>> = _externalSearchResults.asStateFlow()

    // 是否正在外部搜索：标记网络搜索状态（用于显示加载动画）
    private val _isExternalSearching = MutableStateFlow(false)
    val isExternalSearching: StateFlow<Boolean> = _isExternalSearching.asStateFlow()

    /**
     * 所有植物列表（带筛选/搜索）：整合多个数据流，实时更新
     * 数据源：
     * 1. searchQuery：用户输入的搜索关键词
     * 2. selectedCategory：用户选择的分类
     * 3. repository.allPlants：本地数据库的所有植物
     * 4. externalSearchResults：网络搜索的植物结果
     * 功能：
     * - 过滤有图片的植物（避免显示无图项）
     * - 匹配搜索关键词（名称/别名）
     * - 匹配选中的分类
     * - 合并本地和网络结果（去重）
     * - 同步收藏状态（确保本地收藏状态最新）
     */
    val allPlants: StateFlow<List<Plant>> = combine(
        _searchQuery,
        _selectedCategory,
        repository.allPlants,
        _externalSearchResults
    ) { query, category, localPlants, external ->
        // 第一步：过滤本地植物（有图片+匹配关键词+匹配分类）
        val localFiltered = localPlants.filter { plant ->
            val hasImage = plant.imageUrl.isNotBlank() // 有图片
            val matchesQuery = plant.name.contains(query, ignoreCase = true) || // 名称匹配
                    plant.alias.contains(query, ignoreCase = true) // 别名匹配
            val matchesCategory = category == null || plant.category == category // 分类匹配（未选则全部）
            hasImage && matchesQuery && matchesCategory // 同时满足才保留
        }

        // 第二步：合并本地和网络结果（仅搜索时有网络结果）
        val baseList = if (query.isNotBlank()) {
            (localFiltered + external).distinctBy { it.id } // 按植物ID去重
        } else {
            localFiltered // 无搜索词时，仅显示本地结果
        }

        // 第三步：同步收藏状态（确保网络结果的收藏状态和本地一致）
        val favoriteStatusMap = localPlants.associate { it.id to it.isFavorite } // 本地收藏状态映射表
        baseList.map { plant ->
            // 优先用本地收藏状态，无则用植物自身的状态
            val currentFavorite = favoriteStatusMap[plant.id] ?: plant.isFavorite
            plant.copy(isFavorite = currentFavorite) // 复制植物对象，更新收藏状态（不可变对象）
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // 订阅者取消后5秒内保持活跃
        initialValue = emptyList() // 初始值：空列表
    )

    /**
     * 收藏植物列表：仅显示有图片的收藏植物
     * 数据源：仓库的收藏植物数据流，过滤无图项
     */
    val favoritePlants: StateFlow<List<Plant>> = repository.favoritePlants
        .map { list ->
            list.filter { it.imageUrl.isNotBlank() } // 过滤有图片的收藏植物
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 初始植物列表：首页加载的默认植物（最多100条）
     * 数据源：本地数据库的所有植物，过滤无图项，最多取100条（避免加载过多）
     */
    val initialPlants: StateFlow<List<Plant>> = repository.allPlants
        .map { list ->
            val filtered = list.filter { it.imageUrl.isNotBlank() } // 过滤有图片的植物
            if (filtered.size <= 100) filtered else filtered.take(100) // 最多100条
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 是否正在刷新：标记初始植物列表的刷新状态（用于下拉刷新动画）
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // 植物识别结果：AI识别后的植物结果（成功/失败）
    private val _recognitionResult = MutableStateFlow<Result<Plant>?>(null)
    val recognitionResult: StateFlow<Result<Plant>?> = _recognitionResult.asStateFlow()

    // 是否正在识别：标记植物识别的状态（用于显示加载动画）
    private val _recognitionInProgress = MutableStateFlow(false)
    val recognitionInProgress: StateFlow<Boolean> = _recognitionInProgress.asStateFlow()

    // 选中的图片路径：用户选择/拍摄的植物图片路径
    private val _selectedImagePath = MutableStateFlow<String?>(null)
    val selectedImagePath: StateFlow<String?> = _selectedImagePath.asStateFlow()

    /**
     * ViewModel初始化方法：创建时自动执行
     * 功能：
     * 1. 加载初始植物数据
     * 2. 监听本地植物列表，随机选择每日推荐植物（仅当无推荐时）
     */
    init {
        loadInitialPlants()

        // 启动协程监听本地植物列表
        viewModelScope.launch {
            repository.allPlants.collect { list ->
                // 过滤有图片的植物
                val validPlants = list.filter { it.imageUrl.isNotBlank() }
                // 无每日推荐且有有效植物时，随机选一个
                if (validPlants.isNotEmpty() && _dailyPlant.value == null) {
                    _dailyPlant.value = validPlants.random()
                }
            }
        }
    }

    // 外部搜索任务：管理网络搜索的协程任务（用于取消/重启）
    private var externalSearchJob: Job? = null

    /**
     * 更新搜索关键词
     * @param query 用户输入的搜索内容（比如“玫瑰”）
     * 功能：
     * 1. 更新搜索状态
     * 2. 无关键词时，清空网络搜索结果并取消搜索任务
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _externalSearchResults.value = emptyList() // 清空网络结果
            externalSearchJob?.cancel() // 取消正在进行的搜索
        }
    }

    /**
     * 执行外部网络搜索
     * 功能：
     * 1. 无关键词时不执行
     * 2. 取消之前的搜索任务（避免重复请求）
     * 3. 启动协程执行网络搜索，更新搜索结果和状态
     */
    fun performExternalSearch() {
        val query = _searchQuery.value
        if (query.isBlank()) return // 无关键词，直接返回

        externalSearchJob?.cancel() // 取消之前的搜索
        externalSearchJob = viewModelScope.launch {
            _isExternalSearching.value = true // 标记开始搜索
            val result = repository.searchExternalPlant(query) // 调用仓库的网络搜索方法
            if (result.isSuccess) {
                // 搜索成功：更新网络结果（无结果则为空列表）
                _externalSearchResults.value = result.getOrDefault(emptyList())
            }
            _isExternalSearching.value = false // 标记搜索结束
        }
    }

    /**
     * 更新选中的分类
     * @param category 选中的分类名称（比如“花卉”，null表示取消分类）
     */
    fun onCategorySelect(category: String?) {
        _selectedCategory.value = category
    }

    /**
     * 加载初始植物数据
     * 功能：
     * 1. 标记刷新状态（显示加载动画）
     * 2. 本地无数据时，从网络刷新100条植物
     * 3. 结束刷新状态，启动后台详情同步
     */
    fun loadInitialPlants() {
        viewModelScope.launch {
            _isRefreshing.value = true // 开始刷新
            val list = repository.allPlants.first() // 获取本地植物列表的第一个快照
            if (list.isEmpty()) {
                repository.refreshPlants(pageSize = 100) // 本地无数据，从网络刷新
            }
            _isRefreshing.value = false // 结束刷新
            startGradualDetailSync() // 启动后台详情同步
        }
    }

    /**
     * 刷新初始植物数据（下拉刷新时调用）
     * 功能：强制从网络刷新100条植物，更新本地数据
     */
    fun refreshInitialPlants() {
        viewModelScope.launch {
            _isRefreshing.value = true // 开始刷新
            repository.refreshPlants(pageSize = 100) // 强制刷新
            _isRefreshing.value = false // 结束刷新
            startGradualDetailSync() // 启动后台详情同步
        }
    }

    /**
     * 启动渐进式详情同步（后台缓慢同步，不阻塞UI）
     * 功能：避免一次性同步大量数据导致UI卡顿
     */
    private fun startGradualDetailSync() {
        // 已有活跃的同步任务，直接返回
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            _isSyncingDetails.value = true // 标记开始同步
            repository.fetchPendingDetails(immediate = false) // 渐进式同步
            _isSyncingDetails.value = false // 标记同步结束
        }
    }

    /**
     * 立即同步所有未同步的植物详情（用户主动触发时调用）
     * 功能：取消现有同步任务，强制立即同步所有数据
     */
    fun syncAllPendingDetailsNow() {
        syncJob?.cancel() // 取消现有任务
        syncJob = viewModelScope.launch {
            _isSyncingDetails.value = true // 标记开始同步
            repository.fetchPendingDetails(immediate = true) // 立即同步
            _isSyncingDetails.value = false // 标记同步结束
        }
    }

    /**
     * 用户注册
     * @param username 用户名（账号）
     * @param password 密码（明文，仓库内会加密存储）
     * @param agree 是否同意用户协议（true=同意）
     * @return 注册结果（成功/失败）
     */
    suspend fun registerUser(username: String, password: String, agree: Boolean): Result<Unit> {
        return repository.registerUser(username, password, agree)
    }

    /**
     * 用户登录
     * @param username 用户名（账号）
     * @param password 密码（明文，仓库内会加密验证）
     * @return 登录结果（成功/失败）
     * 功能：
     * 1. 调用仓库的登录方法
     * 2. 登录成功后，更新登录用户ID，加载用户数据
     */
    suspend fun loginUser(username: String, password: String): Result<Unit> {
        val result = repository.loginUser(username, password)
        if (result.isSuccess) {
            _loggedInUserId.value = username // 更新登录用户ID
            loadInitialPlants() // 加载用户专属数据
        }
        return result
    }

    /**
     * 用户登出
     * 功能：
     * 1. 重置搜索/分类/网络结果（避免游客数据受筛选影响）
     * 2. 调用仓库登出，切换为游客ID
     * 3. 清空每日推荐，重新加载游客数据
     */
    fun logout() {
        // 1. 重置筛选状态
        _searchQuery.value = ""
        _selectedCategory.value = null
        _externalSearchResults.value = emptyList()

        // 2. 仓库登出，切换为游客
        repository.logout()
        _loggedInUserId.value = null

        // 3. 清空每日推荐
        _dailyPlant.value = null

        // 4. 加载游客数据
        loadInitialPlants()
    }

    /**
     * 切换植物收藏状态（收藏/取消收藏）
     * @param plant 要操作的植物对象
     * 功能：在协程中调用仓库方法，更新植物的收藏状态
     */
    fun toggleFavorite(plant: Plant) {
        viewModelScope.launch { repository.updateFavoriteStatus(plant.id, !plant.isFavorite) }
    }

    /**
     * 设置选中的图片路径（拍照/选图后调用）
     * @param path 图片的本地路径（null表示清空）
     */
    fun setSelectedImagePath(path: String?) {
        _selectedImagePath.value = path
    }

    /**
     * 识别植物（从图片文件）
     * @param imageFile 植物图片文件
     * 功能：
     * 1. 记录图片路径
     * 2. 标记识别中状态
     * 3. 启动协程调用仓库的识别方法，更新识别结果
     * 4. 识别结束后，标记识别完成
     */
    fun identifyPlant(imageFile: File) {
        _selectedImagePath.value = imageFile.absolutePath // 记录图片路径
        _recognitionInProgress.value = true // 标记识别中
        viewModelScope.launch {
            // 调用仓库的识别方法，获取结果
            _recognitionResult.value = repository.identifyAndFetchDetails(imageFile)
            _recognitionInProgress.value = false // 标记识别完成
        }
    }

    /**
     * 重置植物识别状态（识别完成/取消后调用）
     * 功能：清空识别结果、识别状态、图片路径
     */
    fun resetRecognition() {
        _recognitionResult.value = null
        _recognitionInProgress.value = false
        _selectedImagePath.value = null
    }

    /**
     * 根据植物ID获取植物详情
     * @param id 植物ID（唯一标识）
     * @return 植物对象（null表示未找到）
     */
    suspend fun getPlantById(id: String): Plant? {
        return repository.getPlantBySlug(id)
    }
}