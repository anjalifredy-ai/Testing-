package com.rikky.blankct.ui.chats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rikky.blankct.R
import com.rikky.blankct.data.AuthManager

class ChatsListFragment : Fragment(R.layout.fragment_chats_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvId = view.findViewById<TextView>(R.id.tv_your_id_label)
        val btnCopy = view.findViewById<View>(R.id.btn_copy_id)
        val rv = view.findViewById<RecyclerView>(R.id.rv_chats)
        rv.layoutManager = LinearLayoutManager(requireContext())

        var currentId = ""

        AuthManager.ensureSignedIn { shortId ->
            currentId = shortId
            activity?.runOnUiThread {
                tvId.text = "Your ID: $shortId"
            }
        }

        btnCopy.setOnClickListener {
            if (currentId.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("BLANK CT ID", currentId))
                Toast.makeText(requireContext(), "ID copied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
