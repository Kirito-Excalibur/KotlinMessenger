package com.first.kotlinmessenger

import User.User
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.first.kotlinmessenger.databinding.ActivityNewMessageBinding
import com.first.kotlinmessenger.databinding.UserRowNewMessageBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import messges.ChatLog

class NewMessage : AppCompatActivity() {

    private lateinit var binding: ActivityNewMessageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title="Select User"


        fetchUsers()
    }

    companion object{
        val USER_KEY="USER_KEY"
    }
    private fun fetchUsers() {
       val ref= FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter=GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))

                    }

                }
                adapter.setOnItemClickListener{item,view->
                    val userItem=item as UserItem
                    val intent= Intent(view.context, ChatLog::class.java)
                    intent.putExtra(USER_KEY, userItem.user )
                    startActivity(intent)
                    finish()
                }
                binding.RecyclerViewNewmessage.adapter=adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        )
    }

}

class UserItem(val user: User): Item<GroupieViewHolder>(){
    private lateinit var binding: UserRowNewMessageBinding
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.findViewById<TextView>(R.id.Username_textview).text=user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageview_newmessage))
    }
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }


}