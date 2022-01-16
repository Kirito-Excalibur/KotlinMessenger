package messges

import User.User
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.first.kotlinmessenger.NewMessage
import com.first.kotlinmessenger.R
import com.first.kotlinmessenger.databinding.ActivityChatLogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ChatLog : AppCompatActivity() {

    companion object{
        val TAG="Chatlog"
    }

    val adapter=GroupAdapter<GroupieViewHolder>()
    var toUser: User?=null


    private lateinit var binding: ActivityChatLogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

    binding.recyclerviewChatlog.adapter=adapter

        toUser=intent.getParcelableExtra<User>(NewMessage.USER_KEY)
        supportActionBar?.title=toUser?.username


       listenForMessages()

        binding.sendButtonChatlog.setOnClickListener{
            Log.d(TAG,"Attempt to send message")
            performSendMessage()
        }

}

    private fun listenForMessages(){
        val fromId=FirebaseAuth.getInstance().uid
        val toId=toUser?.uid
        val ref=FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val chatMessage=snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG,chatMessage.text)

                    if (chatMessage.fromId==FirebaseAuth.getInstance().uid) {
                        val currentUser=LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text,currentUser!!))
                    }else{

                             adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })



    }
    class ChatMessage(val id: String, val text:String, val fromId: String, val toId: String, val timestamp: Long)
    {
        constructor(): this("","","","",-1)
    }

    private fun performSendMessage() {

    val text=binding.editTextChatlog.text.toString()
    val fromId=FirebaseAuth.getInstance().uid
    val user=intent.getParcelableExtra<User>(NewMessage.USER_KEY)
    val toId=user?.uid
    // val reference=FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/messages").push()
        val reference=FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/user-messages/$fromId/$toId").push()
        val toReference=FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/user-messages/$toId/$fromId").push()
    val chatMessage=ChatMessage(reference.key!!, text, fromId!!, toId!!, System.currentTimeMillis()/1000)

    reference.setValue(chatMessage)
        .addOnSuccessListener {
            Log.d(TAG,"Saved our chat message: ${reference.key}")
            binding.editTextChatlog.text.clear()
            binding.recyclerviewChatlog.scrollToPosition(adapter.itemCount-1)
        }

        toReference.setValue(chatMessage)

        val latestMessageRef= FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef=FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
}


    }

class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun  bind(viewHolder: GroupieViewHolder,position: Int){
        viewHolder.itemView.findViewById<TextView>(R.id.textview_fromchat).text=text

        val uri=user.profileImageUrl
        val targetImageView=viewHolder.itemView.findViewById<ImageView>(R.id.chatfrom_imageview)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun  bind(viewHolder: GroupieViewHolder,position: Int){
viewHolder.itemView.findViewById<TextView>(R.id.textview_tochat).text="text"

        //load image
        val uri=user.profileImageUrl
        val targetImageView=viewHolder.itemView.findViewById<ImageView>(R.id.chat_to_imageview)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}