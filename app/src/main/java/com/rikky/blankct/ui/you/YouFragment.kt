package com.rikky.blankct.ui.you

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rikky.blankct.R
import com.rikky.blankct.data.AuthManager

class YouFragment : Fragment(R.layout.fragment_you) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvId = view.findViewById<TextView>(R.id.tv_your_short_id)
        val btnYoutube = view.findViewById<Button>(R.id.btn_login_youtube)
        val btnInstagram = view.findViewById<Button>(R.id.btn_login_instagram)

        AuthManager.ensureSignedIn { shortId ->
            activity?.runOnUiThread {
                tvId.text = "Your BLANK CT ID: $shortId"
            }
        }

        btnYoutube.setOnClickListener {
            Toast.makeText(requireContext(), "Open the Y tab and sign in there", Toast.LENGTH_SHORT).show()
        }

        btnInstagram.setOnClickListener {
            Toast.makeText(requireContext(), "Open the I tab and sign in there", Toast.LENGTH_SHORT).show()
        }
    }
}
