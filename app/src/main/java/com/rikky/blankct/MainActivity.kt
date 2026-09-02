package com.rikky.blankct

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.database.ValueEventListener
import com.rikky.blankct.data.AuthManager
import com.rikky.blankct.data.CallRepository
import com.rikky.blankct.ui.chats.ChatsListFragment
import com.rikky.blankct.ui.newchat.NewChatFragment
import com.rikky.blankct.ui.you.YouFragment
import com.rikky.blankct.ui.yfeed.YFeedFragment
import com.rikky.blankct.ui.ifeed.IFeedFragment

class MainActivity : AppCompatActivity() {

    private var incomingCallListener: ValueEventListener? = null
    private var currentHandledCallId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            loadFragment(ChatsListFragment())
        }

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_chats -> ChatsListFragment()
                R.id.nav_new -> NewChatFragment()
                R.id.nav_y -> YFeedFragment()
                R.id.nav_i -> IFeedFragment()
                R.id.nav_you -> YouFragment()
                else -> ChatsListFragment()
            }
            loadFragment(fragment)
            true
        }

        AuthManager.ensureSignedIn { _ ->
            val myUid = AuthManager.currentUid() ?: return@ensureSignedIn
            incomingCallListener = CallRepository.listenForIncomingCalls(myUid) { call ->
                if (call.callId != currentHandledCallId) {
                    currentHandledCallId = call.callId
                    runOnUiThread {
                        val intent = Intent(this, CallActivity::class.java)
                        intent.putExtra("callId", call.callId)
                        intent.putExtra("otherUid", call.callerId)
                        intent.putExtra("otherName", call.callerName)
                        intent.putExtra("isCaller", false)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        incomingCallListener?.let {
            com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("calls").removeEventListener(it)
        }
    }
}
