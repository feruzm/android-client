package io.golos.cyber_android.ui.screens.post_edit.fragment.view.image_picker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import io.golos.cyber_android.BuildConfig
import io.golos.cyber_android.ui.shared.base.FragmentBaseCoroutines
import io.golos.domain.FileSystemHelper
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val REQUEST_IMAGE_CAPTURE = 200
private const val REQUEST_GALLERY_IMAGE = 201

/**
 * Base fragment for screens that updates some image of the user profile.
 * Incapsulates all logic of the image picking, fileproviders etc (but not permissions).
 * Child should only override [onImagePicked] method and use images and [getInitialImageSource] to provide source of
 * the image (like camera or gallery)
 */
abstract class ImagePickerFragmentBase : FragmentBaseCoroutines() {
    enum class ImageSource {
        CAMERA, GALLERY, NONE
    }

    private var currentImageFile: Uri? = null

    @Inject
    internal lateinit var fileSystem: FileSystemHelper

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        clearCache(requireContext())

        if (savedInstanceState == null) {

            when (getInitialImageSource()) {
                ImageSource.CAMERA -> takeCameraPhoto()
                ImageSource.GALLERY -> pickGalleryPhoto()
                else -> {
                    //noop
                }
            }
        }
    }

    /**
     * Override this to provide initial source of the image to pick.
     * Use [ImageSource.NONE] when there is no need to pick image on activity start. This way
     * you can always use [pickGalleryPhoto] and [takeCameraPhoto] later.
     */
    abstract fun getInitialImageSource(): ImageSource

    protected fun pickGalleryPhoto() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickPhoto,
            REQUEST_GALLERY_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> currentImageFile
            REQUEST_GALLERY_IMAGE -> data?.data
            else -> null
        }
        ?.let { imageUri ->
            if (resultCode == Activity.RESULT_OK) {
                launch {
                    setLoadingVisibility(true)
                    val imageFile = fileSystem.copyImageToJunkFolder(imageUri)
                    setLoadingVisibility(false)

                    if(imageFile != null) {
                        onImagePicked(imageFile)
                    } else {
                        onImagePickingCancel()
                    }
                }
            } else {
                onImagePickingCancel()
            }
        }
    }

    /**
     * Called when image was successfully picked
     */
    abstract fun onImagePicked(uri: Uri)

    open fun onImagePickingCancel() {
        requireActivity().setResult(Activity.RESULT_CANCELED)
        requireActivity().finish()
    }

    protected fun takeCameraPhoto() {
        currentImageFile = getCacheImagePath(System.currentTimeMillis().toString() + ".jpg")
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageFile)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent,
                REQUEST_IMAGE_CAPTURE
            )
        }
    }

    private fun getCacheImagePath(fileName: String): Uri {
        val file = File.createTempFile(
            "JPG_",
            fileName,
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        return FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file)
    }

    /**
     * Calling this will delete the images from cache directory
     * useful to clear some memory
     */
    private fun clearCache(context: Context) {
        val path = File(context.externalCacheDir, "camera")
        if (path.exists() && path.isDirectory) {
            for (child in path.listFiles()) {
                child.delete()
            }
        }
    }
}
