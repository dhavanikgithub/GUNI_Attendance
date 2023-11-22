package com.example.guniattendance.utils.leave

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.guniattendance.R
import org.json.JSONObject


class LeaveListAdapter(private val context: Context, private val dataList: ArrayList<LeaveListData>) : BaseAdapter() {

    private val url = LeaveURL.getLeaveURL()
    private val TAG = "LeaveListAdapter"

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.leave_history_items_view, parent, false)
        }
        val data = getItem(position) as LeaveListData
//        val enrollNoTextView = view!!.findViewById<TextView>(R.id.enroll_no)
//        val fullNameTextView = view.findViewById<TextView>(R.id.full_name)
        val leaveReasonTextView = view!!.findViewById<TextView>(R.id.leave_reason)
        val startDateTextView = view.findViewById<TextView>(R.id.start_date)
        val endDateTextView = view.findViewById<TextView>(R.id.end_date)
        val leaveStatusTextView = view.findViewById<TextView>(R.id.leaveStatus)
        val requestDate = view.findViewById<TextView>(R.id.requestDate)
        val requestTime = view.findViewById<TextView>(R.id.requestTime)
        val requestType = view.findViewById<TextView>(R.id.requestType)
        val itemId = view.findViewById<TextView>(R.id.itemId)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        itemId.text=position.toString()


//        enrollNoTextView.text=data.studentId
//        fullNameTextView.text=data.studentName

        leaveReasonTextView.text=data.reason
        startDateTextView.text=data.startDate
        endDateTextView.text=data.endDate
        if(data.status.contains("Approved"))
        {
            leaveStatusTextView.setTextColor(Color.parseColor("#45C560"))
            btnCancel.visibility=View.GONE
        }
        else if(data.status.contains("Rejected"))
        {
            leaveStatusTextView.setTextColor(Color.parseColor("#FF2531"))
            btnCancel.visibility=View.GONE
        }
        else if(data.status.contains("Canceled"))
        {
            leaveStatusTextView.setTextColor(Color.parseColor("#FF2531"))
            btnCancel.visibility=View.GONE
        }
        else{
            leaveStatusTextView.setTextColor(Color.parseColor("#EDA200"))
            btnCancel.visibility=View.VISIBLE
        }

        btnCancel.setOnClickListener {
            cancelApplicationRequest(position)
        }
        leaveStatusTextView.text=data.status

        requestDate.text=data.requestDate
        requestTime.text=data.requestTime
        requestType.text=data.type

        return view
    }
    fun cancelApplicationRequest(pos:Int)
    {
        val progress = ProgressDialog(context)
        progress.setMessage("Loading....")
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress.isIndeterminate = true
        progress.show()
        val request: StringRequest = object : StringRequest(Method.POST, url, Response.Listener { response: String? ->
            try {
                Log.d(TAG,response.toString())
                progress.dismiss()
                val jsonObject = JSONObject(response)
                if (jsonObject.has("success"))
                {
                    dataList[pos].status="Canceled"
                    notifyDataSetChanged()
                }
            }
            catch (ex:Exception)
            {
                progress.dismiss()
                Log.e(TAG,ex.toString())
            }
        }, Response.ErrorListener { error: VolleyError ->
            progress.dismiss()
            Log.d(TAG, error.toString())
        }){
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["function_name"] = "leave_cancel_leave_request_by_request_id"
                params["request_id"] = dataList[pos].id
                return params
            }
        }
        val queue = Volley.newRequestQueue(context)
        queue.add(request)
    }
}
