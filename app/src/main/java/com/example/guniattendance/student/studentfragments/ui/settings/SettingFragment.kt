package com.example.guniattendance.student.studentfragments.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
//import com.example.guniattendance.SettingsActivity.SettingsFragmentDirections
import com.example.guniattendance.databinding.FragmentSettingsBinding
import com.example.guniattendance.utils.showProgress
import com.example.guniattendance.utils.snackbar
import com.uvpce.attendance_moodle_api_library.model.MoodleBasicUrl
import com.uvpce.attendance_moodle_api_library.repo.ModelRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Application Settings"
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    companion object {
        fun newInstance() = SettingFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        val savedURLPref: SharedPreferences = requireActivity().getSharedPreferences("savedURL", 0)
        val savedURLEditor: SharedPreferences.Editor = savedURLPref.edit()

        val checkboxTogglePref: SharedPreferences = requireActivity().getSharedPreferences("buttonToggle", 0)
        val checkboxToggleEditor: SharedPreferences.Editor = checkboxTogglePref.edit()

        lateinit var urlList: List<MoodleBasicUrl>

        binding.apply {
            MainScope().launch{
                requireActivity().runOnUiThread {
                    showProgress(activity = requireActivity(), bool = true, parentLayout = parentLayout, loading = lottieAnimation)
                }

                urlList = ModelRepository.getMoodleUrlList(requireActivity())

                val urlStringArrayList=ArrayList<String>()

                for(item in urlList)
                {
                    urlStringArrayList.add(item.url)
                }

                requireActivity().runOnUiThread {
                    val checkboxCheck = checkboxTogglePref.getBoolean("buttonToggle", true)
                    toggleUrlDialog.isChecked = checkboxCheck

                    val adapter = ArrayAdapter(
                        requireActivity(),
                        R.layout.url_list_spinner_item,
                        urlStringArrayList
                    )
                    adapter.setDropDownViewResource(R.layout.url_list_spinner_item)
                    s1UrlList.adapter = adapter
//                    val savedURL = ModelRepository.getStoredMoodleUrl(requireActivity()).url

                    if (savedURLPref.contains("url")) {
                        val setUrl = savedURLPref.getString("url", null)
                        if (setUrl != null) {
                            s1UrlList.setSelection(adapter.getPosition(setUrl))
                            showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)
                        }
                        showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)
                    }
                    showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)

                }

            }
            saveBtn.setOnClickListener {
                showProgress(activity = requireActivity(), bool = true, parentLayout = parentLayout, loading = lottieAnimation)
                if(!toggleUrlDialog.isChecked){
                    checkboxToggleEditor.putBoolean("buttonToggle", toggleUrlDialog.isChecked)
                    checkboxToggleEditor.apply()
                }else {
                    checkboxToggleEditor.putBoolean("buttonToggle", toggleUrlDialog.isChecked)
                    checkboxToggleEditor.apply()
                }

                if(s1UrlList.selectedItem != null)
                {
                    savedURLEditor.putString("url", s1UrlList.selectedItem.toString())
                    savedURLEditor.apply()
                    for (i in urlList.indices)
                    {
                        if(urlList[i].url==s1UrlList.selectedItem.toString())
                        {
                            ModelRepository.setMoodleUrlSetting(requireContext(), urlList[i])
                            showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)
                            findNavController().navigate(
                                SettingFragmentDirections.actionSettingFragmentToLauncherScreenFragment()
                            )
                            break
                        }
                    }

                }
                else{
                    showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)
                    snackbar("URL not selected")
                }
            }
        }
    }

}