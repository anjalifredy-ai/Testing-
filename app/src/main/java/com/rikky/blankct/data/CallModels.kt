package com.rikky.blankct.data

data class CallInfo(
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val calleeId: String = "",
    val status: String = "ringing", // ringing, accepted, rejected, ended
    val offerSdp: String = "",
    val answerSdp: String = "",
    val timestamp: Long = 0L
)

data class IceCandidateData(
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val candidate: String = ""
)
