package com.rikky.blankct.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object CallRepository {

    private val db = FirebaseDatabase.getInstance().reference

    fun startCall(callId: String, callerId: String, callerName: String, calleeId: String, offerSdp: String) {
        val call = CallInfo(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            calleeId = calleeId,
            status = "ringing",
            offerSdp = offerSdp,
            timestamp = System.currentTimeMillis()
        )
        db.child("calls").child(callId).setValue(call)
    }

    fun listenForIncomingCalls(myUid: String, onIncoming: (CallInfo) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val call = child.getValue(CallInfo::class.java) ?: continue
                    if (call.calleeId == myUid && call.status == "ringing") {
                        onIncoming(call)
                    }
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        db.child("calls").addValueEventListener(listener)
        return listener
    }

    fun listenToCall(callId: String, onUpdate: (CallInfo) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val call = snapshot.getValue(CallInfo::class.java) ?: return
                onUpdate(call)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        db.child("calls").child(callId).addValueEventListener(listener)
        return listener
    }

    fun sendAnswer(callId: String, answerSdp: String) {
        db.child("calls").child(callId).child("answerSdp").setValue(answerSdp)
        db.child("calls").child(callId).child("status").setValue("accepted")
    }

    fun rejectCall(callId: String) {
        db.child("calls").child(callId).child("status").setValue("rejected")
    }

    fun endCall(callId: String) {
        db.child("calls").child(callId).child("status").setValue("ended")
        db.child("calls").child(callId).removeValue()
    }

    fun sendIceCandidate(callId: String, side: String, candidate: IceCandidateData) {
        db.child("calls").child(callId).child("ice_$side").push().setValue(candidate)
    }

    fun listenIceCandidates(callId: String, side: String, onCandidate: (IceCandidateData) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val ice = child.getValue(IceCandidateData::class.java) ?: continue
                    onCandidate(ice)
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        db.child("calls").child(callId).child("ice_$side").addValueEventListener(listener)
        return listener
    }

    fun stopListening(ref: String, listener: ValueEventListener) {
        db.child(ref).removeEventListener(listener)
    }
}
