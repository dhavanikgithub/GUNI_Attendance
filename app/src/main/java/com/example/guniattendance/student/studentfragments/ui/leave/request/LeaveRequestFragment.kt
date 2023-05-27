package com.example.guniattendance.student.studentfragments.ui.leave.request

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentLeaveRequestBinding
import com.example.guniattendance.utils.BasicUtils.Companion.convertDateToString
import com.example.guniattendance.utils.BasicUtils.Companion.getDaysBetween
import com.example.guniattendance.utils.BasicUtils.Companion.validateDateRange
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.leave.LeaveURL
import com.example.guniattendance.utils.snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class LeaveRequestFragment : Fragment(R.layout.fragment_leave_request) {

    private lateinit var binding: FragmentLeaveRequestBinding
    private lateinit var progress: LottieAnimationView
    val PICK_FILE_REQUEST_CODE = 1 // arbitrary request code
    private var fileURI:Uri? = null
    private val url = LeaveURL.getLeaveURL()
    private val TAG = "LeaveRequestFragment"
    private var customProgressDialog: CustomProgressDialog?=null
    private var leaveType:String=""


    private lateinit var viewModel: LeaveRequestViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLeaveRequestBinding.bind(view)
        if(customProgressDialog==null)
        {
            customProgressDialog=CustomProgressDialog(requireContext())
        }
        binding.apply {
            startDateEditText.setOnClickListener {
                showStartDatePickerDialog()
            }
            endDateEditText.setOnClickListener {
                showEndDatePickerDialog()
            }

            btnAttachProof.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*" // allow any file type to be selected
                }
                startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
            }

            val typeDataList = ArrayList<String>()
            typeDataList.add("Sick Leave")
            typeDataList.add("Internship/Co-op Leave")
            typeDataList.add("Athletic Leave")
            typeDataList.add("Professional Development Leave")
            typeDataList.add("Mental Health Leave")
            typeDataList.add("Bereavement Leave")
            typeDataList.add("Volunteer Leave")
            typeDataList.add("Emergency Leave")
            typeDataList.add("Academic Conferences Leave")
            typeDataList.add("Medical Leave")
            typeDataList.add("Personal Leave")

            val adapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typeDataList)
            autoLeaveType.setAdapter(adapter1)

            autoLeaveType.setOnItemClickListener { parent, view, position, id ->
                binding.autoLeaveType.error=null
                leaveType=typeDataList.get(position)
            }

            inpLeaveReason.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(inpLeaveReason.text.toString().isEmpty())
                    {
                        inpLeaveReason.error="Leave Reason Require"
                    }
                    else{
                        inpLeaveReason.error=null
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })


            btnRemoveProof.setOnClickListener {
                attachedProof.text = ""
                fileURI=null
                btnRemoveProof.visibility = View.GONE
                btnAttachProof.setOnClickListener {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*" // allow any file type to be selected
                    }
                    startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
                }
            }

            btnSubmitApplication.setOnClickListener {
                var validateField=ArrayList<String>()
                val dateValidate  = validateDateRange(binding.startDateEditText.text.toString(),binding.endDateEditText.text.toString())
                if( leaveType == "")
                {
                    autoLeaveType.error="Please Select Leave Type"
                    validateField.add("ErrorLeaveType")
                }
                else{
                    autoLeaveType.error=null
                    validateField.remove("ErrorLeaveType")
                }
                if(startDateEditText.text.toString() =="")
                {
                    startDateEditText.error="Start Date Require"
                    validateField.add("ErrorStartDate")
                }
                else{
                    startDateEditText.error=null
                    validateField.remove("ErrorStartDate")
                }
                if(endDateEditText.text.toString() == "")
                {
                    endDateEditText.error="End Date Require"
                    validateField.add("ErrorEndDate")
                }
                else{
                    endDateEditText.error=null
                    validateField.remove("ErrorEndDate")
                }

                if(!dateValidate) {
                    validateField.add("ErrorDateRange")
                } else{
                    validateField.remove("ErrorDateRange")
                }
                if(getDaysBetween(binding.startDateEditText.text.toString(),binding.endDateEditText.text.toString())>60L)
                {
                    validateField.add("ErrorLeaveDays")
                }
                else{
                    validateField.remove("ErrorLeaveDays")
                }
                if(getDaysBetween(binding.startDateEditText.text.toString(),binding.endDateEditText.text.toString())==-1L)
                {
                    validateField.add("ErrorCountDays")
                }
                else{
                    validateField.remove("ErrorCountDays")
                }
                if(inpLeaveReason.text.toString() == "")
                {
                    inpLeaveReason.error="Leave Reason Require"
                    validateField.add("ErrorLeaveReason")
                }
                else{
                    inpLeaveReason.error=null
                    validateField.remove("ErrorLeaveReason")
                }

                if(validateField.size>0)
                {
                    snackbar("Please fill require field")
                    validateField.clear()
                    return@setOnClickListener
                }
                customProgressDialog!!.start("Submitting Application.....")
                var fileBase64: String? = null
                if(fileURI!=null)
                {
                    fileBase64 = URIToBase64String(fileURI!!)
                }

                MainScope().launch {
                    val localStudentData = requireActivity().getSharedPreferences("studentData", 0)
                    val studentEnrolment = localStudentData.getString("studentEnrolment", "")
                    val request: StringRequest =
                        object : StringRequest(
                            Method.POST, url, Response.Listener { response: String? ->
                            try{
                                val jsonObjUserInfo = JSONObject(response!!)
                                if(!jsonObjUserInfo.has("proctor_id"))
                                {
                                    customProgressDialog!!.stop()
                                    if(jsonObjUserInfo.has("error"))
                                    {
                                        snackbar("You are not registered for leave system. Please Contact Admin")
                                    }
                                    else{
                                        snackbar(response.toString())
                                    }
                                    Log.d(TAG,response.toString())
                                    return@Listener
                                }
                                val proctorID = jsonObjUserInfo.getString("proctor_id")
                                val request: StringRequest = object : StringRequest(Method.POST, url,
                                    Response.Listener { response: String? ->
                                        customProgressDialog!!.stop()
                                        try{
                                            Log.d(TAG,response!!)
                                            val jsonObj = JSONObject(response)
                                            if(jsonObj.has("success"))
                                            {
                                                snackbar("Request submitted")
                                                return@Listener
                                            }
                                            else{
                                                Log.d(TAG,response.toString())
                                                snackbar(response.toString())
                                                return@Listener
                                            }
                                        }
                                        catch (ex:Exception)
                                        {
                                            snackbar(ex.message.toString())
                                            Log.e(TAG,ex.toString())
                                        }


                                    }, Response.ErrorListener { error: VolleyError ->
                                        customProgressDialog!!.stop()
                                        snackbar(error.message.toString())
                                        Log.d(TAG, error.toString())
                                    }){
                                    @Throws(AuthFailureError::class)
                                    override fun getParams(): Map<String, String>? {
                                        val currentTime = LocalTime.now()
                                        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
                                        val formattedTime = currentTime.format(timeFormatter)

                                        val currentDate = LocalDate.now()
                                        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                        val formattedDate = currentDate.format(dateFormatter)

                                        val params: MutableMap<String, String> = HashMap()
                                        params["function_name"] = "leave_set_leave_request_data"
                                        params["student_id"] = studentEnrolment.toString()
                                        params["proctor_id"] = proctorID
                                        params["type"] = leaveType
                                        params["start_date"] = startDateEditText.text.toString()
                                        params["end_date"] = endDateEditText.text.toString()
                                        params["reason"] = inpLeaveReason.text.toString()
                                        params["status"] = "Pending"
                                        if(fileBase64!=null)
                                        {
                                            params["file_content"] = fileBase64
                                            params["file_type"] = File(getFileName(fileURI!!).toString()).extension
                                        }
                                        params["current_date"] = formattedDate
                                        params["current_time"] = formattedTime
                                        return params
                                    }
                                }
                                val queue = Volley.newRequestQueue(requireContext())
                                queue.add(request)
                            }
                            catch (ex:Exception)
                            {
                                customProgressDialog!!.stop()
                                snackbar(ex.message.toString())
                                Log.e(TAG,ex.toString())
                            }

                        },
                            Response.ErrorListener { error: VolleyError ->
                                customProgressDialog!!.stop()
                                snackbar(error.message.toString())
                                Log.d(TAG, error.toString())
                            }){
                            @Throws(AuthFailureError::class)
                            override fun getParams(): Map<String, String>? {
                                val params: MutableMap<String, String> = HashMap()
                                params["function_name"] = "leave_get_user_info"
                                params["enrollment"] = studentEnrolment.toString()
                                return params
                            }
                        }
                    val queue = Volley.newRequestQueue(requireContext())
                    queue.add(request)

                }
            }
        }
    }


    fun URIToBase64String(uri: Uri):String {
        val uriString = uri.toString()
        Log.d("data", "onActivityResult: uri$uriString")
        try {
            val inputSrtream = requireActivity().contentResolver.openInputStream(uri)
            val bytes = getBytes(inputSrtream)
            Log.d("data", "onActivityResult: bytes size=" + bytes.size)
            Log.d(
                "data",
                "onActivityResult: Base64string=" + Base64.encodeToString(bytes, Base64.DEFAULT)
            )

            val ansValue: String = Base64.encodeToString(bytes, Base64.DEFAULT)
            return ansValue
        } catch (e: Exception) {
            snackbar(e.message.toString())
            Log.d("error", "onActivityResult: $e")
            return "null"
        }
    }

    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream?): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream!!.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            try{
                val uri = data.data
                if (uri != null) {
                    // update the TextView to display the selected file name
                    val fileDescriptor = requireContext().getContentResolver().openAssetFileDescriptor(uri, "r")
                    val fileSize: Long = fileDescriptor!!.getLength()
                    if(fileSize>2097152)
                    {
                        snackbar("Maximum file size 2MB")
                        binding.attachedProof.text=""
                        return
                    }
                    fileURI = uri
                    val fileName = getFileName(uri)
                    binding.attachedProof.text = fileName
                    binding.btnRemoveProof.visibility = View.VISIBLE
                }
            }
            catch (ex:Exception)
            {
                snackbar(ex.message.toString())
            }

        }
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String?{
        var result: String? = null
        if (uri.scheme == "content") {
            val contentResolver = context?.contentResolver
            val cursor = contentResolver?.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }


    fun showStartDatePickerDialog() {
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val dayOfMonth: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(requireContext(),
            { datePicker, year, month, day -> // Set the selected date on the start date edit text
                val date = LocalDate.of(year, month+1, day)
                val dateString = convertDateToString(date)
                if(binding.endDateEditText.text.toString()!="")
                {
                    val dateValidate = validateDateRange(dateString,binding.endDateEditText.text.toString())
                    if(!dateValidate)
                    {
                        snackbar("StartDate is not Valid")
                        binding.startDateEditText.error="Date is not Valid"
                        binding.startDateEditText.text!!.clear()
                        return@DatePickerDialog
                    }
                    else if(getDaysBetween(dateString,binding.endDateEditText.text.toString())>60)
                    {
                        snackbar("Maximum 60Days Leave allow")
                        binding.startDateEditText.error="Maximum 60Days Leave allow"
                        binding.startDateEditText.text!!.clear()
                        return@DatePickerDialog
                    }
                    else if(getDaysBetween(dateString,binding.endDateEditText.text.toString())==-1L)
                    {
                        snackbar("Something Error in Days Count")
                        binding.startDateEditText.text!!.clear()
                        return@DatePickerDialog
                    }
                    binding.startDateEditText.error=null
                    binding.startDateEditText.setText(dateString)
                }
                else{
                    binding.startDateEditText.error=null
                    binding.startDateEditText.setText(dateString)
                }

            }, year, month, dayOfMonth)
        datePickerDialog.show()
    }

    fun showEndDatePickerDialog() {
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val dayOfMonth: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(requireContext(),
            { datePicker, year, month, day -> // Set the selected date on the end date edit text
                val date = LocalDate.of(year, month+1, day)
                val dateString = convertDateToString(date)
                if(binding.startDateEditText.text.toString()!="")
                {
                    val dateValidate = validateDateRange(binding.startDateEditText.text.toString(),dateString)
                    if(!dateValidate)
                    {
                        snackbar("EndDate is not Valid")
                        binding.endDateEditText.error="EndDate is not Valid"
                        binding.endDateEditText.text!!.clear()
                        return@DatePickerDialog
                    }
                    else if(getDaysBetween(binding.startDateEditText.text.toString(),dateString)>60)
                    {
                        snackbar("Maximum 60Days Leave allow")
                        binding.endDateEditText.error="Maximum 60Days Leave allow"
                        binding.endDateEditText.text!!.clear()
                        return@DatePickerDialog
                    }
                    else if(getDaysBetween(binding.startDateEditText.text.toString(),dateString)==-1L)
                    {
                        snackbar("Something Error in Days Count")
                        binding.endDateEditText.text!!.clear()
                        return@DatePickerDialog
                    }
                    binding.endDateEditText.error=null
                    binding.endDateEditText.setText(dateString)
                }
                else{
                    binding.endDateEditText.error=null
                    binding.endDateEditText.setText(dateString)
                }
            }, year, month, dayOfMonth
        )
        datePickerDialog.show()
    }



}