package com.first.kotlinmessenger

import User.User
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.first.kotlinmessenger.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import messges.LatestMessagesActivity
import registration.LoginActivity
import java.util.*

class RegisterActivity : AppCompatActivity() {
    companion object {
        private const val GALLERY_REQUEST_CODE: Int = 123
        val TAG="RegisterActivity"

    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)




        binding.buttonRegister.setOnClickListener() {
            val email = binding.emailEdittextRegister.text.toString()
            val password = binding.passwordEdittextRegister.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter text in email/password", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }



            Log.d("Main", "Email is:" + email)
            Log.d("Main", "Password: $password")

            //Firebase authentication to create user with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener() {
                    if (!it.isSuccessful) return@addOnCompleteListener


                    Log.d("Main", "Successfully created user with uid: ${it.result?.user?.uid}")
                    Toast.makeText(this, "Successfully created user", Toast.LENGTH_SHORT).show()

                    uploadImageToFirebaseStorage()

                }
                .addOnFailureListener {
                    Log.d("Main", "Failed to create user: ${it.message}")
                }
        }



        binding.aldreadyHaveAccount.setOnClickListener {
            Log.d("Main", "Try to show login activity")


            //launch login activity somehow
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }



        binding.selectphotobutton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityIfNeeded(
                Intent.createChooser(intent, "Pick an image"),
                GALLERY_REQUEST_CODE
            )
        }
    }

    var imagedata:Uri?=null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imagedata= data.data
            binding.selectphotobutton.setImageURI(imagedata)
        }
    }

    private fun uploadImageToFirebaseStorage(){
        if(imagedata==null) return

        val filename=UUID.randomUUID().toString()
        val ref= FirebaseStorage.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/images/$filename")

        ref.putFile(imagedata!!)
            .addOnSuccessListener {
                Log.d("Main","Successfully uploaded the image ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Main","File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid=FirebaseAuth.getInstance().uid ?: ""
        val ref=FirebaseDatabase.getInstance("https://kotlinmessenger-a3530-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/users/$uid")
        val user= User(uid,binding.usernameEdittextRegister.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG,"Finally we saved the user to Firebase Database")
                val intent=Intent(this, LatestMessagesActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d(TAG,"Failed to set value to databse: ${it.message}")
            }

    }

}
















