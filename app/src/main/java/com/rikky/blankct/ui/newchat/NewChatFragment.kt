package com.rikky.blankct.ui.newchat

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

class NewChatFragment : Fragment(R.layout.fragment_new_chat) {

    private var groupIconBase64: String = ""
    private lateinit var ivGroupIcon: ImageView

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val base64 = ImageUtils.uriToCompressedBase64(requireContext(), uri)
                if (base64 != null) {
                    groupIconBase64 = base64
                    ivGroupIcon.setImageBitmap(ImageUtils.base64ToBitmap(base64))
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etFriendId = view.findViewById<EditText>(R.id.et_friend_id)
        val btnStartChat = view.findViewById<Button>(R.id.btn_start_chat)
        val btnSelfChat = view.findViewById<Button>(R.id.btn_self_chat)
        val etGroupName = view.findViewById<EditText>(R.id.et_group_name)
        val btnNewGroup = view.findViewById<Button>(R.id.btn_new_group)
        val tvPickIcon = view.findViewById<TextView>(R.id.tv_pick_icon)
        ivGroupIcon = view.findViewById(R.id.iv_group_icon)

        val myUid = AuthManager.currentUid()

        ivGroupIcon.setOnClickListener { openImagePicker() }
        tvPickIcon.setOnClickListener { openImagePicker() }

        btnStartChat.setOnClickListener {
            val friendId = etFriendId.text.toString().trim().uppercase()
            if (friendId.isEmpty() || myUid == null) {
                Toast.makeText(requireContext(), "Enter an ID first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ChatRepository.findUidByShortId(friendId) { otherUid ->
                if (otherUid == null) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "No user with that ID", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    ChatRepository.startDirectChat(myUid, otherUid) { chatId ->
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Chat started!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        btnSelfChat.setOnClickListener {
            if (myUid == null) return@setOnClickListener
            ChatRepository.startSelfChat(myUid) {
                Toast.makeText(requireContext(), "Self chat ready", Toast.LENGTH_SHORT).show()
            }
        }

        btnNewGroup.setOnClickListener {
            val groupName = etGroupName.text.toString().trim()
            if (groupName.isEmpty() || myUid == null) {
                Toast.makeText(requireContext(), "Enter a group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ChatRepository.createGroup(myUid, groupName, groupIconBase64) {
                Toast.makeText(requireContext(), "Group created!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
}
