package com.rikky.blankct.ui.you

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.rikky.blankct.R
import com.rikky.blankct.data.AuthManager
import com.rikky.blankct.data.ChatRepository
import com.rikky.blankct.data.ImageUtils

class YouFragment : Fragment(R.layout.fragment_you) {

    private var myIconBase64: String = ""
    private lateinit var ivMyIcon: ImageView

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val base64 = ImageUtils.uriToCompressedBase64(requireContext(), uri)
                if (base64 != null) {
                    myIconBase64 = base64
                    ivMyIcon.setImageBitmap(ImageUtils.base64ToBitmap(base64))
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvId = view.findViewById<TextView>(R.id.tv_your_short_id)
        val btnYoutube = view.findViewById<Button>(R.id.btn_login_youtube)
        val btnInstagram = view.findViewById<Button>(R.id.btn_login_instagram)
        val etName = view.findViewById<EditText>(R.id.et_my_name)
        val btnSave = view.findViewById<Button>(R.id.btn_save_profile)
        val tvChangeIcon = view.findViewById<TextView>(R.id.tv_change_icon)
        ivMyIcon = view.findViewById(R.id.iv_my_icon)

        AuthManager.ensureSignedIn { shortId ->
            activity?.runOnUiThread {
                tvId.text = "Your BLANK CT ID: $shortId"
            }
            val myUid = AuthManager.currentUid()
            if (myUid != null) {
                ChatRepository.fetchUserProfile(myUid) { profile ->
                    activity?.runOnUiThread {
                        if (profile != null) {
                            if (profile.displayName.isNotEmpty()) {
                                etName.setText(profile.displayName)
                            }
                            if (profile.iconBase64.isNotEmpty()) {
                                myIconBase64 = profile.iconBase64
                                ivMyIcon.setImageBitmap(ImageUtils.base64ToBitmap(profile.iconBase64))
                            }
                        }
                    }
                }
            }
        }

        ivMyIcon.setOnClickListener { openImagePicker() }
        tvChangeIcon.setOnClickListener { openImagePicker() }

        btnSave.setOnClickListener {
            val myUid = AuthManager.currentUid() ?: return@setOnClickListener
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ChatRepository.saveMyProfile(myUid, name, myIconBase64)
            Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show()
        }

        btnYoutube.setOnClickListener {
            Toast.makeText(requireContext(), "Open the Y tab and sign in there", Toast.LENGTH_SHORT).show()
        }

        btnInstagram.setOnClickListener {
            Toast.makeText(requireContext(), "Open the I tab and sign in there", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
}
