package com.example.afinal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.afinal.data.repository.PlantRepository

/**
 * ViewModel工厂类：用于创建带参数的PlantViewModel实例
 * 背景：Android默认的ViewModel创建方式不支持带参数的构造函数（比如需要传入PlantRepository）
 * 作用：
 * 1. 解决ViewModel依赖注入问题（把数据仓库传递给ViewModel）
 * 2. 解耦ViewModel和数据层，便于测试和维护
 */
class PlantViewModelFactory(private val repository: PlantRepository) : ViewModelProvider.Factory {

    /**
     * 创建ViewModel实例的核心方法
     * @param modelClass 要创建的ViewModel类类型（比如PlantViewModel::class.java）
     * @return 构建好的ViewModel实例
     * @throws IllegalArgumentException 如果传入的ViewModel类型不支持（不是PlantViewModel）
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 检查请求创建的ViewModel是否是PlantViewModel（或其子类）
        if (modelClass.isAssignableFrom(PlantViewModel::class.java)) {
            // 抑制类型转换警告：明确将PlantViewModel转为泛型T
            @Suppress("UNCHECKED_CAST")
            // 创建PlantViewModel实例，并传入数据仓库依赖
            return PlantViewModel(repository) as T
        }
        // 如果请求的是其他类型的ViewModel，抛出异常（表示不支持）
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}