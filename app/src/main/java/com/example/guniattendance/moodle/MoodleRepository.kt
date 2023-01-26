package com.example.guniattendance.moodle

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.guniattendance.moodle.model.BaseUserInfo
import com.example.guniattendance.utils.BitmapUtils
import com.example.guniattendance.utils.snackbar
import com.uvpce.attendance_moodle_api_library.MoodleController
import com.uvpce.attendance_moodle_api_library.ServerCallback
import org.json.JSONArray
import java.nio.file.Path
import java.nio.file.Paths

class MoodleRepository(val context: Context) {
    val attRepo = MoodleController.getAttendanceRepository(
        ClientAPI.Url,
        ClientAPI.coreToken,
        ClientAPI.attandanceToken,
        ClientAPI.uploadToken)
    fun isStudentRegisterForFace(enollmentNo:String,onReceive:(Boolean)->Unit,onError:(String)->Unit){

            attRepo.getUserInfoMoodle(context, enollmentNo, object: ServerCallback {
                override fun onSuccess(result: JSONArray) {
                    try {
                        var fetchedProfileURL = ""
                        //Enrollment exists.
                        (0 until result.length()).forEach {
                            val item = result.getJSONObject(it)
                            fetchedProfileURL = item.get("profileimageurl").toString()
                            Log.i("fetched", "${fetchedProfileURL}")
                        }

                        var finalFetchedProfileURL = BitmapUtils.finalizeURL(
                            fetchedProfileURL,
                            "8d29dd97dd7c93b0e3cdd43d4b797c87"
                        )
                        Log.i("finalFetchedProfileURL:", "${finalFetchedProfileURL}")

                        var convertedfetchedProfileImage =
                            BitmapUtils.convertUrlToBase64(finalFetchedProfileURL)
                        Log.i("convertedfetchedProfileImage:", "${convertedfetchedProfileImage}")

                        var converteduserDefaultPic =
                            BitmapUtils.convertUrlToBase64(ClientAPI.userDefaultPicURL)
                        Log.i("converteduserDefaultPic:", "${converteduserDefaultPic}")

                        if(convertedfetchedProfileImage == converteduserDefaultPic){
                            onReceive(true)

                        }
                        else{
                            //OPEN CAMERA FOR ATTENDANCE
                            onReceive(false)

                        }
                    } catch (e: Exception){
                        Log.i("Exception", "$e")
                        onError("Error:$e")
                    }

                }

                override fun onError(result: String) {
                    onError(result)
                    //Enrollment does not exists.

                }

            })

    }
    fun getStudentInfo(enrollmentNo:String, onReceive:(BaseUserInfo)->Unit, onError:(String)->Unit){
        attRepo.getUserInfoMoodle( context, enrollmentNo, object : ServerCallback {
            override fun onSuccess(result: JSONArray) {

                    val item = result.getJSONObject(0)
                    val userid = item.get("id").toString()
                    val firstname = item.get("firstname").toString()
                    val lastname = item.get("lastname").toString()
                    val fullname = item.get("fullname").toString()
                    val emailaddr = item.get("email").toString()
                val image = item.get("profileimageurl").toString()
                onReceive(BaseUserInfo(userid,enrollmentNo,firstname,lastname,fullname,emailaddr,image))
            }

            override fun onError(result: String) {

            }

        })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadStudentPicture(userid:String, curImageUri: Uri, onReceive: (JSONArray) -> Unit, onError: (String) -> Unit) {
        //Update the selected photo in moodle
        val bitmap =
            context.let { it1 -> BitmapUtils.getBitmapFromUri(it1.contentResolver, curImageUri) }
        val bitmapStr = bitmap.let { it1 -> BitmapUtils.bitmapToString(it1) }

        //Create regex to get the filename from curImageUri
        val path: Path = Paths.get(curImageUri.toString())
        val filename: String = path.fileName.toString()
        //Uploading the correct chosen pic
        attRepo.uploadFileMoodle(
            context,
            "user",
            "draft",
            "0",
            "/",
            filename,
            "$bitmapStr",
            "user",
            "2",
            object : ServerCallback {
                override fun onSuccess(result: JSONArray) {
                    Log.i("Successfully uploaded the file:", "$result")
                    val item = result.getJSONObject(0)
                    //val contextid = item.get("contextid").toString()
                    val draftitemid = item.get("itemid").toString()
                    //Updated the uploaded picture.
                    attRepo.updatePictureMoodle(
                        context,
                        draftitemid,
                        userid,
                        object : ServerCallback {
                            override fun onSuccess(result: JSONArray) {
                                onReceive(result)
                            }

                            override fun onError(result: String) {
                                onError(result)
                            }
                        })
                }

                override fun onError(result: String) {
                    onError(result)
                }
            })
    }
}