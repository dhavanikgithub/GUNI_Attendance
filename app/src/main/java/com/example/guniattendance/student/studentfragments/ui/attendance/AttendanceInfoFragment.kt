package com.example.guniattendance.student.studentfragments.ui.attendance

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentAttendanceInfoBinding

class AttendanceInfoFragment : Fragment(R.layout.fragment_attendance_info) {

    private lateinit var binding: FragmentAttendanceInfoBinding
//    companion object {
//        fun newInstance() = AttendanceInfoFragment()
//    }

    private lateinit var viewModel: AttendanceInfoViewModel
    lateinit var QRBtn: AppCompatButton

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        view:View
//        return super.onCreateView(inflater, container, savedInstanceState)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AttendanceInfoViewModel::class.java]

        binding = FragmentAttendanceInfoBinding.bind(view)

        binding.apply {
            QRBtn.setOnClickListener{
                it.findNavController().navigate(R.id.action_attendanceInfoFragment_to_scannerFragment)
            }
//            attendanceBtn.setOnClickListener {
//                it.findNavController().navigate()
//            }
        }
    }


}