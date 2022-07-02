package com.example.myapplication.ui.admin

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.core.PRODUCT_EXTRA_KEY
import com.example.myapplication.core.model.CategoryEntity
import com.example.myapplication.core.model.ProductEntity
import com.example.myapplication.core.utils.GsonUtils
import com.example.myapplication.databinding.FragmentProductFormBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.SpinnerCategoryAdapter
import com.example.myapplication.viewmodel.AdminViewModel
import com.example.myapplication.wiget.BaseDialogFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ProductFormFragment : BaseDialogFragment(R.layout.fragment_product_form) {

    private var _binding: FragmentProductFormBinding? = null
    private val binding get() = _binding!!
    private var mode = NEW_MODE
    private var uri: Uri? = null
    private var product: ProductEntity? = null
    private val adminVM: AdminViewModel by lazy {
        ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        if (arguments == null) {
            mode = NEW_MODE
        } else {
            mode = EDIT_MODE
            val productGson = requireArguments().getString(PRODUCT_EXTRA_KEY)
            product = GsonUtils.getGsonParser().fromJson(productGson, ProductEntity::class.java)
            initViewEditMode()
        }
        binding.ivProduct.setOnClickListener {
            requestPermissionAndPickImage()
        }
        collectFlow(adminVM.listCategories) {
            setDataSpinner(it)
        }
        collectFlow(adminVM.loading) {
            if (it) {
                binding.llLoading.visibility = View.VISIBLE
                binding.llLoading.isClickable = true
            } else {
                binding.llLoading.visibility = View.INVISIBLE
                binding.llLoading.isClickable = false
            }
        }
        binding.tvEdit.setOnClickListener {
            createProduct()
        }
    }

    private fun initViewEditMode() {
        product?.let {
            binding.edtName.setText(it.name)
            binding.edtPrice.setText(it.price.toString())
            binding.edtContent.setText(it.content)
            binding.swStatus.isChecked = it.status.toString() == "0"
            binding.tvEdit.text = "Sửa"
            val url = it.image_url
            Glide.with(binding.ivProduct.context).load(url).circleCrop().into(binding.ivProduct)
        }
    }

    private fun createProduct() {
        val category = binding.snCategory.selectedItem as CategoryEntity
        val category_id = category.id.toString()
        val name = binding.edtName.text.toString()
        val price = binding.edtPrice.text.toString()
        val content = binding.edtContent.text.toString()
        val status = if (binding.swStatus.isChecked) "0" else "1"
        if (mode == NEW_MODE) {
            if (category_id.isBlank() || name.isBlank() || price.isBlank() || content.isBlank() || status.isBlank() || uri == null) {
                Toast.makeText(requireActivity(), "Nhập các trường bắt buộc", Toast.LENGTH_LONG).show()
            } else {
                adminVM.createProduct(
                    category_id,
                    name,
                    price,
                    content,
                    status,
                    uri!!
                ) { b, str, pr ->
                    if (b) {
                        Toast.makeText(requireActivity(), "Thành công", Toast.LENGTH_LONG).show()
                        dismiss()
                        GlobalScope.launch {

                            var list = mutableListOf<ProductEntity>()
                            list.addAll(adminVM.listProducts.value)
                            list.add(0, pr!!)
                            adminVM.listProducts.emit(list)
                            adminVM.setListProductByCategory(adminVM.categorySelected)
                        }

                    } else {
                        Toast.makeText(requireActivity(), str, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            if (category_id.isBlank() || name.isBlank() || price.isBlank() || content.isBlank() || status.isBlank()) {
                Toast.makeText(requireActivity(), "Nhập các trường bắt buộc", Toast.LENGTH_LONG).show()
            } else {
                adminVM.editProduct(
                    product!!.id.toString(),
                    category_id,
                    name,
                    price,
                    content,
                    status,
                    uri
                ) { b, str, pr ->
                    if (b) {
                        Toast.makeText(requireActivity(), "Cập nhập thành công", Toast.LENGTH_LONG).show()
                        dismiss()
                        GlobalScope.launch {
                            var list = mutableListOf<ProductEntity>()
                            list.addAll(adminVM.listProducts.value)
                            val index =
                                adminVM.listProducts.value.indexOfFirst { it.id == pr!!.id }
                            list.set(index, pr!!)
                            adminVM.listProducts.emit(list)
                            adminVM.setListProductByCategory(adminVM.categorySelected)
                        }
                    } else {
                        Toast.makeText(requireActivity(), str, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    }

    private fun setDataSpinner(mutableList: MutableList<CategoryEntity>) {
        val spinnerAdapter = SpinnerCategoryAdapter(requireActivity(), mutableList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.snCategory.apply {
            adapter = spinnerAdapter
            post {
                dropDownWidth = measuredWidth
            }
        }
        if (mode == EDIT_MODE) {
            val pos = mutableList.indexOfFirst {
                it.id == product!!.category_id
            }
            binding.snCategory.setSelection(pos)
        }
    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                pickImage()
            }
        }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri: Uri = data?.data!!
                    uri = fileUri
                    try {
                        if (Build.VERSION.SDK_INT < 28) {
                            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                fileUri
                            )
                            binding.ivProduct.setImageBitmap(bitmap)
                        } else {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                fileUri
                            )
                            val bitmap = ImageDecoder.decodeBitmap(source)
                            binding.ivProduct.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    private fun requestPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            pickImage()
            return
        }
        val result = ContextCompat.checkSelfPermission(
            requireActivity(),
            READ_EXTERNAL_STORAGE
        )
        if (result == PackageManager.PERMISSION_GRANTED) {
            pickImage()
        } else {
            permReqLauncher.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startForProfileImageResult.launch(intent)
    }

    companion object {
        const val NEW_MODE = 0
        const val EDIT_MODE = 1
        const val READ_EXTERNAL_REQUEST = 10
    }
}