package com.example.guniattendance

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.guniattendance.databinding.FragmentLeaveBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.net.URI
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [LeaveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LeaveFragment : Fragment() {
    private lateinit var binding: FragmentLeaveBinding
    private lateinit var progress: LottieAnimationView
    val PICK_FILE_REQUEST_CODE = 1 // arbitrary request code

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLeaveBinding.bind(view)

        val startDateEditText: TextInputEditText = binding.startDateEditText
        val endDateEditText: TextInputEditText = binding.endDateEditText
//        val startDateInputLayout: TextInputLayout = binding.startDateLayout
//        val endDateInputLayout: TextInputLayout = binding.endDateLayout

        startDateEditText.setOnClickListener {
            showStartDatePickerDialog(view)
        }
        endDateEditText.setOnClickListener {
            showEndDatePickerDialog(view)
        }

        binding.btnAttachProof.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*" // allow any file type to be selected
            }
            startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
        }

        binding.btnRemoveProof.setOnClickListener {
            binding.attachedProof.text = ""
            binding.btnRemoveProof.visibility = View.GONE
            binding.btnAttachProof.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*" // allow any file type to be selected
                }
                startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                // update the TextView to display the selected file name
                val fileName = getFileName(uri)
                binding.attachedProof.text = fileName
                binding.btnRemoveProof.visibility = View.VISIBLE
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leave, container, false)
    }

    companion object {
        fun newInstance() = LeaveFragment()
    }

    fun showStartDatePickerDialog(view: View?) {
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val dayOfMonth: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(requireContext(),
            OnDateSetListener { datePicker, year, month, day -> // Set the selected date on the start date edit text
                val dateString = (month + 1).toString() + "/" + day + "/" + year
                binding.startDateEditText.setText(dateString)
            }, year, month, dayOfMonth
        )
        datePickerDialog.show()
    }

    fun showEndDatePickerDialog(view: View?) {
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val dayOfMonth: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(requireContext(),
            OnDateSetListener { datePicker, year, month, day -> // Set the selected date on the end date edit text
                val dateString = (month + 1).toString() + "/" + day + "/" + year
                binding.endDateEditText.setText(dateString)
            }, year, month, dayOfMonth
        )
        datePickerDialog.show()
    }
}