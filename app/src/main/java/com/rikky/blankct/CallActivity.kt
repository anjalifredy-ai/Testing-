package com.rikky.blankct

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rikky.blankct.data.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class CallActivity : AppCompatActivity() {

    private lateinit var webRTCClient: WebRTCClient
    private lateinit var audioManager: AudioManager

    private var callId: String = ""
    private var myUid: String = ""
    private var otherUid: String = ""
    private var otherName: String = ""
    private var isCaller: Boolean = false
    private var isMuted: Boolean = false
    private var isSpeakerOn: Boolean = true
    private var isConnected: Boolean = false

    private var secondsElapsed = 0
    private var timerJob: CountDownTimer? = null

    private lateinit var tvName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var incomingActions: LinearLayout
    private lateinit var ongoingActions: LinearLayout
    private lateinit var btnAccept: ImageView
    private lateinit var btnReject: ImageView
    private lateinit var btnEndCall: ImageView
    private lateinit var btnMute: ImageView
    private lateinit var btnSpeaker: ImageView

    private val RECORD_AUDIO_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        callId = intent.getStringExtra("callId") ?: ""
        otherUid = intent.getStringExtra("otherUid") ?: ""
        otherName = intent.getStringExtra("otherName") ?: "Unknown"
        isCaller = intent.getBooleanExtra("isCaller", false)
        myUid = AuthManager.currentUid() ?: ""

        tvName = findViewById(R.id.tv_call_name)
        tvStatus = findViewById(R.id.tv_call_status)
        incomingActions = findViewById(R.id.layout_incoming_actions)
        ongoingActions = findViewById(R.id.layout_ongoing_actions)
        btnAccept = findViewById(R.id.btn_accept)
        btnReject = findViewById(R.id.btn_reject)
        btnEndCall = findViewById(R.id.btn_end_call)
        btnMute = findViewById(R.id.btn_mute)
        btnSpeaker = findViewById(R.id.btn_speaker)

        tvName.text = otherName

        btnAccept.setOnClickListener { acceptIncomingCall() }
        btnReject.setOnClickListener { rejectCall() }
        btnEndCall.setOnClickListener { endCallAndFinish() }
        btnMute.setOnClickListener { toggleMute() }
        btnSpeaker.setOnClickListener { toggleSpeaker() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE
            )
        } else {
            setupCall()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCall()
            } else {
                Toast.makeText(this, "Mic permission needed to call", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupCall() {
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        setSpeaker(true)

        webRTCClient = WebRTCClient(
            context = this,
            onIceCandidate = { candidate ->
                val side = if (isCaller) "caller" else "callee"
                CallRepository.sendIceCandidate(
                    callId, side,
                    IceCandidateData(candidate.sdpMid ?: "", candidate.sdpMLineIndex, candidate.sdp)
                )
            },
            onRemoteStream = {
                runOnUiThread { onCallConnected() }
            }
        )
        webRTCClient.init()
        webRTCClient.createPeerConnection()

        val otherSide = if (isCaller) "callee" else "caller"
        CallRepository.listenIceCandidates(callId, otherSide) { ice ->
            webRTCClient.addIceCandidate(IceCandidate(ice.sdpMid, ice.sdpMLineIndex, ice.candidate))
        }

        if (isCaller) {
            startAsCaller()
        } else {
            startAsCallee()
        }
    }

    private fun startAsCaller() {
        incomingActions.visibility = LinearLayout.GONE
        ongoingActions.visibility = LinearLayout.VISIBLE
        tvStatus.text = "Ringing..."

        webRTCClient.createOffer { offer ->
            CallRepository.startCall(callId, myUid, "Me", otherUid, offer.description)
        }

        CallRepository.listenToCall(callId) { call ->
            when (call.status) {
                "accepted" -> {
                    if (call.answerSdp.isNotEmpty()) {
                        webRTCClient.setRemoteDescription(SessionDescription(SessionDescription.Type.ANSWER, call.answerSdp))
                    }
                    runOnUiThread { tvStatus.text = "Connecting..." }
                }
                "rejected" -> runOnUiThread { finishCall("Call declined") }
                "ended" -> runOnUiThread { finishCall("Call ended") }
            }
        }
    }

    private fun startAsCallee() {
        incomingActions.visibility = LinearLayout.VISIBLE
        ongoingActions.visibility = LinearLayout.GONE
        tvStatus.text = "Incoming call..."

        CallRepository.listenToCall(callId) { call ->
            if (call.status == "ended") {
                runOnUiThread { finishCall("Call ended") }
            }
        }
    }

    private fun acceptIncomingCall() {
        incomingActions.visibility = LinearLayout.GONE
        ongoingActions.visibility = LinearLayout.VISIBLE
        tvStatus.text = "Connecting..."

        CallRepository.listenToCall(callId) { call ->
            if (call.offerSdp.isNotEmpty()) {
                webRTCClient.setRemoteDescription(SessionDescription(SessionDescription.Type.OFFER, call.offerSdp))
                webRTCClient.createAnswer { answer ->
                    CallRepository.sendAnswer(callId, answer.description)
                }
            }
        }
    }

    private fun rejectCall() {
        CallRepository.rejectCall(callId)
        finishCall("Declined")
    }

    private fun onCallConnected() {
        if (isConnected) return
        isConnected = true
        tvStatus.text = "00:00"
        startTimer()
    }

    private fun startTimer() {
        timerJob = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsElapsed++
                val mins = secondsElapsed / 60
                val secs = secondsElapsed % 60
                tvStatus.text = String.format("%02d:%02d", mins, secs)
            }
            override fun onFinish() {}
        }.start()
    }

    private fun toggleMute() {
        isMuted = !isMuted
        webRTCClient.setMuted(isMuted)
        btnMute.alpha = if (isMuted) 0.5f else 1.0f
    }

    private fun toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn
        setSpeaker(isSpeakerOn)
        btnSpeaker.alpha = if (isSpeakerOn) 1.0f else 0.5f
    }

    private fun setSpeaker(on: Boolean) {
        audioManager.isSpeakerphoneOn = on
    }

    private fun endCallAndFinish() {
        CallRepository.endCall(callId)
        finishCall("Call ended")
    }

    private fun finishCall(reason: String) {
        timerJob?.cancel()
        if (::webRTCClient.isInitialized) webRTCClient.close()
        if (::audioManager.isInitialized) audioManager.mode = AudioManager.MODE_NORMAL
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        if (::webRTCClient.isInitialized) webRTCClient.close()
        if (::audioManager.isInitialized) audioManager.mode = AudioManager.MODE_NORMAL
    }
}
