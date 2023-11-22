package com.example.guniattendance.student.studentfragments.ui.leave.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentLeaveLoginBinding
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.leave.LeaveURL
import com.example.guniattendance.utils.snackbar
import org.json.JSONObject

class LeaveLoginFragment : Fragment() {

    companion object {
        fun newInstance() = LeaveLoginFragment()
    }

    private lateinit var viewModel: LeaveLoginViewModel
    private lateinit var binding:FragmentLeaveLoginBinding
    private val url = LeaveURL.getLeaveURL()
    private lateinit var customProgressDialog: CustomProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leave_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentLeaveLoginBinding.bind(view)

        customProgressDialog= CustomProgressDialog(requireActivity())

        val sharedPreferences = requireActivity().getSharedPreferences("student", Context.MODE_PRIVATE)

        val studentEnrolment = sharedPreferences.getString("enrollment", "")

        if(studentEnrolment!="")
        {
            findNavController().popBackStack()
            findNavController().navigate(R.id.leaveHomeFragment)
        }

        binding.apply {
            btnLogin.setOnClickListener {
                customProgressDialog.start("Loading....")
                val request: StringRequest =
                    object : StringRequest(
                        Method.POST, url, Response.Listener { response: String? ->
                            try{
                                Log.i("Login",response.toString())
                                val jsonObjUserInfo = JSONObject(response!!)
                                if(jsonObjUserInfo.has("success"))
                                {
                                    val res = jsonObjUserInfo.getString("success")
                                    val resName = jsonObjUserInfo.getString("name")
                                    Log.i("Login",res.toString())
                                    if(res=="true")
                                    {
                                        // Obtain a reference to SharedPreferences
                                        val sharedPreferences = requireActivity().getSharedPreferences("student", Context.MODE_PRIVATE)

                                        val editor = sharedPreferences.edit()

                                        editor.putString("enrollment", et1StudId.text.toString())
                                        editor.putString("name", resName)

                                        editor.apply()
                                        customProgressDialog.stop()
                                        findNavController().popBackStack()
                                        findNavController().navigate(R.id.leaveHomeFragment)
                                    }
                                    else{
                                        customProgressDialog.stop()
                                        snackbar("Student not found")
                                        return@Listener
                                    }
                                }

                            }
                            catch (ex:Exception)
                            {
                                customProgressDialog.stop()
                                snackbar(ex.message.toString())
                            }

                        },
                        Response.ErrorListener { error: VolleyError ->
                            customProgressDialog.stop()
                            snackbar(error.message.toString())
                        }){
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String>? {
                            val params: MutableMap<String, String> = HashMap()
                            params["function_name"] = "leave_user_auth"
                            params["student_id"] = et1StudId.text.toString()
                            params["password"] = et1Password.text.toString()
                            return params
                        }
                    }
                val queue = Volley.newRequestQueue(requireContext())
                queue.add(request)
            }
        }
    }

}