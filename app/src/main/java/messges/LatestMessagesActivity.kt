package messges

import User.User
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.first.kotlinmessenger.NewMessage
import com.first.kotlinmessenger.R
import com.first.kotlinmessenger.RegisterActivity
import com.first.kotlinmessenger.databinding.ActivityLatestMessagesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import messges.ChatLog.ChatMessage
import views.LatestMessageRow


class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser: User?=null

    }


        private lateinit var binding: ActivityLatestMessagesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLatestMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerviewLatestMessages.adapter=adapter
        binding.recyclerviewLatestMessages.addItemDecoration(DividerItemDecoration(this,
        DividerItemDecoration.VERTICAL))


        adapter.setOnItemClickListener { item, view ->
            Log.d("LatestMessage","123")
            val intent=Intent(this,ChatLog::class.java)
            //we are missing the user

            val row= item as LatestMessageRow

            intent.putExtra(NewMessage.USER_KEY, row.chatPartnerUser )
            startActivity(intent)
        }

        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserIsLoggedIn()
    }


        val latestMessagesMap=HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }

    }
    private fun listenForLatestMessages(){
        val fromId=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val ChatMessage=snapshot.getValue(ChatMessage::class.java)

                latestMessagesMap[snapshot.key!!]=ChatMessage!!
                refreshRecyclerViewMessages()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val ChatMessage=snapshot.getValue(ChatMessage::class.java)
                adapter.add(LatestMessageRow(ChatMessage!!))

                latestMessagesMap[snapshot.key!!]=ChatMessage
                refreshRecyclerViewMessages()
            }


            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    val adapter=GroupAdapter<GroupieViewHolder>()




    private fun fetchCurrentUser(){
        val uid=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
            currentUser=snapshot.getValue(User::class.java)
                Log.d("LatestMessages","Current User ${currentUser?.profileImageUrl}")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        )
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_new_message ->{
                val intent=Intent(this, NewMessage::class.java)
                startActivity(intent)

            }
            R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}
