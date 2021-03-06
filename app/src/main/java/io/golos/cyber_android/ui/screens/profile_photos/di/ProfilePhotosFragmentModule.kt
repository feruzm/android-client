package io.golos.cyber_android.ui.screens.profile_photos.di

import dagger.Module
import dagger.Provides
import io.golos.cyber_android.ui.dto.ProfileItem
import io.golos.domain.dependency_injection.Clarification
import javax.inject.Named

@Module
class ProfilePhotosFragmentModule(private val place: ProfileItem, private val imageUrl: String?) {
    @Provides
    fun providePlace(): ProfileItem = place

    @Provides
    @Named(Clarification.IMAGE_URL)
    fun provideImageUrl(): String? = imageUrl
}