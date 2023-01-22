package com.example.guniattendance.student

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.guniattendance.databinding.ActivitySettingsBinding
import com.example.guniattendance.utils.ClientAPI

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.moodleButton.setOnClickListener {
            val ip = binding.etMoodle.text
            if(ip == null){
                Toast.makeText(this,"Please Enter Moodle Url",Toast.LENGTH_SHORT).show()
            }
            else{
                val client = ClientAPI()
                val ogUrl = client.url
                val updatedUrl = ogUrl.replace("202.131.126.214",ip.toString())
                client.url = updatedUrl
                Toast.makeText(this,"OG : $ogUrl\nUpdated : $updatedUrl", Toast.LENGTH_SHORT).show()
            }
        }
    }
}