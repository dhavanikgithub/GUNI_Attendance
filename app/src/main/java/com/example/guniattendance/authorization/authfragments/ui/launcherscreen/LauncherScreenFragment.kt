package com.example.guniattendance.authorization.authfragments.ui.launcherscreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.ScannerActivity
import com.example.guniattendance.databinding.FragmentLauncherScreenBinding
import com.example.guniattendance.utils.BitmapUtils.Companion.convertUrlToBase64
import com.example.guniattendance.utils.BitmapUtils.Companion.finalizeURL
import com.example.guniattendance.utils.BitmapUtils.Companion.getBitmapFromUri
import com.example.guniattendance.utils.ClientAPI
import com.example.guniattendance.utils.snackbar
import com.uvpce.attendance_moodle_api_library.MoodleController
import com.uvpce.attendance_moodle_api_library.ServerCallback
import org.json.JSONArray

class LauncherScreenFragment : Fragment(R.layout.fragment_launcher_screen) {

    private lateinit var binding: FragmentLauncherScreenBinding


    companion object{
        lateinit var studentEnrolment: String
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLauncherScreenBinding.bind(view)
        binding.apply {
            btnCheckEnrol.setOnClickListener{

                if(et1Enrollment.text.toString().isEmpty()){
                    context?.let { it1 -> ClientAPI.showErrorBox(it1, "Error", "Enrollment number is empty!", "OK") }
                }else{
                    studentEnrolment = et1Enrollment.text.toString()
                    lateinit var fetchedProfileURL: String
                    val attRepo = MoodleController.getAttendanceRepository(ClientAPI().Url,ClientAPI().coreToken,ClientAPI().attandanceToken,ClientAPI().uploadToken)
                    activity?.let { it1 ->
                        attRepo.getUserInfoMoodle(it1, et1Enrollment.text.toString(), object: ServerCallback {
                            override fun onSuccess(result: JSONArray) {
                                try {
                                //Enrollment exists.
                                (0 until result.length()).forEach {
                                    val item = result.getJSONObject(it)
                                    fetchedProfileURL = item.get("profileimageurl").toString()
                                    Log.i("fetched", "${fetchedProfileURL}")
                                }

                                    var finalFetchedProfileURL = finalizeURL(fetchedProfileURL, "8d29dd97dd7c93b0e3cdd43d4b797c87")
                                    Log.i("finalFetchedProfileURL:", "${finalFetchedProfileURL}")

                                    var convertedfetchedProfileImage = convertUrlToBase64(finalFetchedProfileURL)
                                    Log.i("convertedfetchedProfileImage:", "${convertedfetchedProfileImage}")

                                    var converteduserDefaultPic = convertUrlToBase64(ClientAPI().userDefaultPicURL)
                                    Log.i("converteduserDefaultPic:", "${converteduserDefaultPic}")

                                    if(convertedfetchedProfileImage == converteduserDefaultPic){
                                        findNavController().navigate(
                                            LauncherScreenFragmentDirections
                                                .actionLauncherScreenFragmentToLoginFragment()
                                        )
                                    }
                                    else{
                                        //OPEN CAMERA FOR ATTENDANCE

                                        Intent(context, ScannerActivity::class.java).also{
                                            startActivity(it)
                                        }
                                    }
                                    } catch (e: Exception){
                                        Log.i("Exception", "$e")
                                    }

                            }

                            override fun onError(result: String) {
                                //Enrollment does not exists.
                                Log.i("TAG", "onError: ${result}")
                                snackbar("Invalid Enrollment Number!")
                            }

                        })
                    }
                }
            }

        }

    }
}