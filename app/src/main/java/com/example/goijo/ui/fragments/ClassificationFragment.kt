package com.example.goijo.ui.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.goijo.core.UIState
import com.example.goijo.core.imageHelper.imageProcessing
import com.example.goijo.core.show
import com.example.goijo.databinding.FragmentClassificationBinding
import com.example.goijo.ui.viewmodels.ClassificationViewModel
import com.example.goijo.ui.viewmodels.UIClassifyViewModel
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ClassificationFragment : Fragment() {
    private var _binding: FragmentClassificationBinding? = null
    private val binding get() = _binding!!

    private val uiViewModel: UIClassifyViewModel by viewModels()
    private val classificationViewModel: ClassificationViewModel by viewModels()
    private var imageUri: Uri? = null
    private var croppedImageUri: Uri? = null
    private var currentPhotoPath: String? = null

    private val imageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            startUCrop(it)
            classificationViewModel.setImage(it)
        }
    }

    private val launcherCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            imageUri?.let { uri ->
                startUCrop(uri)
                binding.imagePlace.setImageURI(uri)
                classificationViewModel.setImage(uri)
            }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePictureFromCamera()
        } else {
            Toast.makeText(requireContext(), "Izin kamera diperlukan untuk mengambil gambar.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentClassificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFab()
        observeUI()
        binding.output.visibility = View.GONE
        binding.progressIndicator.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            croppedImageUri = UCrop.getOutput(data!!)
            binding.imagePlace.setImageURI(croppedImageUri)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Log.e(TAG, "UCrop Error: $cropError")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupFab() {
        binding.takeImage.setOnClickListener { showImageSourceDialog() }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(requireContext()).apply {
            setTitle("select image source")
            setItems(options) { _, which ->
                when (which) {
                    0 -> takePictureFromCamera()
                    1 -> pickImageFromGallery()
                }
            }
            show()
        }
    }

    private fun pickImageFromGallery() {
        imageFromGallery.launch("image/*")
    }

    private fun takePictureFromCamera() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                val photoFile = createImageFile()
                imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
                launcherCamera.launch(imageUri)
            }
            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun startUCrop(uri: Uri) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Edit Image")
            setMessage("Do you want to crop the image?")
            setPositiveButton("Yes") { _, _ ->
                val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "SAMPLE_CROPPED_IMAGE_NAME.jpg"))
                UCrop.of(uri, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(450, 450)
                    .start(requireContext(), this@ClassificationFragment)
            }
            setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            show()
        }
    }

    private fun observeUI() {
        classificationViewModel.image.observe(viewLifecycleOwner, Observer { uri ->
            uri?.let {
                binding.imagePlace.setImageURI(it)
                binding.imagePlace.show()
                binding.analyze.show()
                binding.analyze.setOnClickListener {
                    croppedImageUri?.let { croppedUri ->
                        val imageBitmap = imageProcessing(requireContext(), croppedUri)
                        onClassification(imageBitmap)
                    }
                }
            } ?: Log.e(TAG, "observeUI: image uri is null")
        })

        classificationViewModel.classification.observe(viewLifecycleOwner, Observer { uiState ->
            when (uiState) {
                is UIState.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is UIState.Success -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.progressIndicator.visibility = View.GONE
                    }, 2000)
                    binding.output.apply {
                        visibility = View.VISIBLE
                        text = uiState.data
                        setTextColor(if (uiState.data.equals("inorganic", ignoreCase = true)) Color.RED else Color.GREEN)
                    }
                }
                is UIState.Failure -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.progressIndicator.visibility = View.GONE
                    }, 2000)
                    binding.output.apply {
                        visibility = View.VISIBLE
                        text = uiState.message
                        setTextColor(Color.RED)
                    }
                }
            }
        })
    }

    private fun onClassification(imageBitmap: Bitmap?) {
        imageBitmap?.let {
            classificationViewModel.doClassification(it)
        }
    }

    companion object {
        private const val TAG = "ClassificationFragment::class"
    }
}
