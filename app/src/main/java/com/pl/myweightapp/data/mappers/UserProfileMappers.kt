package com.pl.myweightapp.data.mappers

import com.pl.myweightapp.data.local.UserProfileEntity
import com.pl.myweightapp.domain.UserProfile


fun UserProfile.toUserProfileEntity(): UserProfileEntity {
    return UserProfileEntity(
        age = age,
        height = height,
        heightUnit = heightUnit,
        targetWeight = targetWeight,
        weightUnit = weightUnit,
        gender = gender,
        photoPath = photoPath,
        name = name,
    )
}

fun UserProfileEntity.toUserProfile(): UserProfile {
    return UserProfile(
        age = age,
        height = height,
        heightUnit = heightUnit,
        targetWeight = targetWeight,
        weightUnit = weightUnit,
        gender = gender,
        photoPath = photoPath,
        name = name,
    )
}