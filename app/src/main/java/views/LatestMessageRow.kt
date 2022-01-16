package views

import User.User
import android.widget.ImageView
import android.widget.TextView
import com.first.kotlinmessenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import messges.ChatLog

class LatestMessageRow(val chatMessage: ChatLog.ChatMessage): Item<GroupieViewHolder>() {
    var chatPartnerUser: User?=null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.message_latest_textview).text=chatMessage.text
        val chatPartnerId: String
        if (chatMessage.fromId== FirebaseAuth.getInstance().uid) {
            chatPartnerId=chatMessage.toId
        }
        else
        {
            chatPartnerId=chatMessage.fromId
        }

        val ref= FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser=snapshot.getValue(User::class.java)
                viewHolder.itemView.findViewById<TextView>(R.id.username_latest_textview).text=chatPartnerUser?.username

                val targetImageView=viewHolder.itemView.findViewById<ImageView>(R.id.imageView_latest_message)
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
    override fun getLayout(): Int{
        return R.layout.latest_messages_row
    }
}