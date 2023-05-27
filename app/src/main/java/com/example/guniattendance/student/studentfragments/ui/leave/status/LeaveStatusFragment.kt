package com.example.guniattendance.student.studentfragments.ui.leave.status

import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import androidx.fragment.app.Fragment
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentLeaveStatusBinding
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.utils.BasicUtils
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.leave.LeaveListAdapter
import com.example.guniattendance.utils.leave.LeaveListData
import com.example.guniattendance.utils.leave.LeaveURL
import com.example.guniattendance.utils.snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class LeaveStatusFragment : Fragment(R.layout.fragment_leave_status) {

    private lateinit var binding: FragmentLeaveStatusBinding
    private val TAG = "LeaveStatusFragment"
    private val url = LeaveURL.getLeaveURL()
    private var customProgressDialog: CustomProgressDialog?=null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLeaveStatusBinding.bind(view)

        if(customProgressDialog==null)
        {
            customProgressDialog= CustomProgressDialog(requireContext())
        }

        val localStudentData = requireActivity().getSharedPreferences("studentData", 0)
        val studentEnrolment = localStudentData.getString("studentEnrolment", "")
        /*val studentEnrolment = "21012022022"*/
        val dataList = ArrayList<LeaveListData>()

        binding.applicationReason.setMovementMethod(ScrollingMovementMethod());

        binding.checkboxApproved.setOnCheckedChangeListener { buttonView, isChecked ->
            customProgressDialog!!.start("Loading....")
            filterListView(dataList)
            customProgressDialog!!.stop()
        }
        binding.checkboxPending.setOnCheckedChangeListener { buttonView, isChecked ->
            customProgressDialog!!.start("Loading....")
            filterListView(dataList)
            customProgressDialog!!.stop()
        }
        binding.checkboxRejected.setOnCheckedChangeListener { buttonView, isChecked ->
            customProgressDialog!!.start("Loading....")
            filterListView(dataList)
            customProgressDialog!!.stop()
        }

        binding.checkboxDelivered.setOnCheckedChangeListener { buttonView, isChecked ->
            customProgressDialog!!.start("Loading....")
            filterListView(dataList)
            customProgressDialog!!.stop()
        }

        binding.checkboxCanceled.setOnCheckedChangeListener { buttonView, isChecked ->
            customProgressDialog!!.start("Loading....")
            filterListView(dataList)
            customProgressDialog!!.stop()
        }

        binding.btnCancel.setOnClickListener {
            MainScope().launch {
                try {
                    customProgressDialog!!.start("Loading....")
                    val reqID = binding.txtRequestID.text.toString()
                    val request: StringRequest = object : StringRequest(Method.POST, url, Response.Listener { response: String? ->
                        try {
                            customProgressDialog!!.stop()
                            Log.d(TAG,response!!)
                            val jsonObject = JSONObject(response)
                            if (jsonObject.has("success"))
                            {
                                binding.leaveStatus.text="Canceled"
                                binding.leaveStatus.setTextColor(Color.parseColor("#FF2531"))
                                binding.btnCancelLayout.visibility=View.GONE
                                snackbar("Application Canceled Successfully")
                            }
                            else{
                                binding.btnCancelLayout.visibility=View.VISIBLE
                                snackbar("Something error in canceling application")
                            }
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
                            params["function_name"] = "leave_cancel_leave_request_by_request_id"
                            params["request_id"] = reqID
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
            }
        }


        MainScope().launch {
            try{
                customProgressDialog!!.start("Loading....")
                val userData = MoodleConfig.getModelRepo(requireContext()).getUserInfo(studentEnrolment.toString())
                val request: StringRequest = object : StringRequest(Method.POST, url, Response.Listener { response: String? ->
                            try {
                                customProgressDialog!!.stop()
                                Log.d(TAG,response!!)
                                if(!BasicUtils.isStringJSONArray(response))
                                {
                                    if(BasicUtils.isStringJSONObject(response))
                                    {
                                        val error = JSONObject(response)
                                        if(error.has("error"))
                                        {
                                            snackbar(error.getString("error"))
                                        }
                                    }
                                    return@Listener
                                }
                                val jsonArray = JSONArray(response)
                                for (i in 0 until jsonArray.length())
                                {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val reason = String(android.util.Base64.decode(jsonObject.getString("reason"), android.util.Base64.DEFAULT),charset("UTF-8"))
                                    val leaveListData = LeaveListData(
                                        jsonObject.getString("id"),
                                        studentEnrolment.toString(),
                                        userData.lastname,
                                        jsonObject.getString("proctor_id"),
                                        jsonObject.getString("type"),
                                        jsonObject.getString("start_date"),
                                        jsonObject.getString("end_date"),
                                        reason,
                                        jsonObject.getString("attachment"),
                                        jsonObject.getString("status"),
                                        jsonObject.getString("request_date"),
                                        jsonObject.getString("request_time")
                                    )
                                    if(i==(jsonArray.length()-1))
                                    {
                                        binding.applicationType.text=leaveListData.type
                                        binding.startDate.text=leaveListData.startDate
                                        binding.endDate.text=leaveListData.endDate

                                        binding.applicationReason.text=leaveListData.reason
                                        /*binding.proofData.text=leaveListData.attachment*/

                                        if(leaveListData.status=="Approved")
                                        {
                                            binding.leaveStatus.setTextColor(Color.parseColor("#45C560"))
                                            binding.btnCancelLayout.visibility=View.GONE
                                        }
                                        else if(leaveListData.status == "Rejected")
                                        {
                                            binding.leaveStatus.setTextColor(Color.parseColor("#FF2531"))
                                            binding.btnCancelLayout.visibility=View.GONE
                                        }
                                        else if(leaveListData.status=="Canceled")
                                        {
                                            binding.leaveStatus.setTextColor(Color.parseColor("#FF2531"))
                                            binding.btnCancelLayout.visibility=View.GONE
                                        }
                                        else{
                                            binding.leaveStatus.setTextColor(Color.parseColor("#EDA200"))
                                            binding.btnCancelLayout.visibility=View.VISIBLE
                                        }
                                        binding.leaveStatus.text=leaveListData.status

                                        binding.txtRequestID.text=leaveListData.id
                                    }
                                    else{
                                        dataList.add(leaveListData)
                                    }

                                }
                                dataList.reverse()
                                val adapter = LeaveListAdapter(requireContext(), dataList)
                                binding.leaveHistory.adapter = adapter
                                customProgressDialog!!.stop()
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
                            params["function_name"] = "leave_get_leave_request_by_student_id"
                            params["enrollment"] = studentEnrolment.toString()
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
        }



    }

    fun clearListView()
    {
        val emptyAdapter: ListAdapter = ArrayAdapter(requireContext(), R.layout.leave_history_items_view, ArrayList<LeaveListData>())
        binding.leaveHistory.adapter = emptyAdapter
    }

    fun filterListView(dataList:ArrayList<LeaveListData>)
    {
        clearListView()
        val myList = ArrayList<String>()
        if(binding.checkboxApproved.isChecked)
        {
            myList.add("Approved")
        }
        if(binding.checkboxPending.isChecked)
        {
            myList.add("Pending")
        }
        if(binding.checkboxRejected.isChecked)
        {
            myList.add("Rejected")
        }
        if(binding.checkboxCanceled.isChecked)
        {
            myList.add("Canceled")
        }
        if(binding.checkboxDelivered.isChecked)
        {
            myList.add("Delivered")
        }
        if(myList.size==0)
        {
            val adapter = LeaveListAdapter(requireContext(), dataList)
            binding.leaveHistory.adapter = adapter
            return
        }

        val filterDataList = ArrayList<LeaveListData>()
        for (item in dataList) {
            if(myList.contains(item.status))
            {
                filterDataList.add(item)
            }
        }
        val adapter = LeaveListAdapter(requireContext(), filterDataList)
        binding.leaveHistory.adapter = adapter
    }
}