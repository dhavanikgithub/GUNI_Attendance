package com.example.guniattendance.student

import android.content.Context
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
            val newIp = binding.etMoodle.text
            if(newIp == null){
                Toast.makeText(this,"Please Enter Moodle Url",Toast.LENGTH_SHORT).show()
            }
            else{
                val client = ClientAPI(this)
                val ogUrl = client.url
                val ip = ogUrl.split("/")[2]
                val updatedUrl = ogUrl.replace(ip,newIp.toString())
                client.url = updatedUrl
                val sharedPref = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString("moodle_url", updatedUrl)
                    commit()
                }
                Toast.makeText(this,"OG : $ogUrl\nUpdated : $updatedUrl", Toast.LENGTH_SHORT).show()
            }
        }

    }
}