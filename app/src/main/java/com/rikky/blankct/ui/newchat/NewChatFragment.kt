package com.rikky.blankct.ui.newchat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rikky.blankct.R

class NewChatFragment : Fragment(R.layout.fragment_new_chat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etFriendId = view.findViewById<EditText>(R.id.et_friend_id)
        val btnStartChat = view.findViewById<Button>(R.id.btn_start_chat)
        val btnNewGroup = view.findViewById<Button>(R.id.btn_new_group)

        btnStartChat.setOnClickListener {
            val friendId = etFriendId.text.toString().trim().uppercase()
            if (friendId.isEmpty()) {
                Toast.makeText(requireContext(), "Enter an ID first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), "Looking up $friendId...", Toast.LENGTH_SHORT).show()
            // Chat creation logic will connect here in the next step
        }

        btnNewGroup.setOnClickListener {
            Toast.makeText(requireContext(), "Group creation coming next", Toast.LENGTH_SHORT).show()
        }
    }
}
