package com.example.guniattendance.student.studentfragments.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentSettingsBinding
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.snackbar
import com.guni.uvpce.moodleapplibrary.model.MoodleBasicUrl
import com.guni.uvpce.moodleapplibrary.repo.ModelRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val TAG = "SettingFragment"
    private var selectedURL:String?=null
    private var customProgressDialog:CustomProgressDialog?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Application Settings"
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        if(customProgressDialog==null)
        {
            customProgressDialog=CustomProgressDialog(requireContext())
        }
//        val callback = object : OnBackPressedCallback(true)
//        {
//            override fun handleOnBackPressed() {
//                findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToLauncherScreenFragment())
//            }
//        }
//
//        requireActivity().onBackPressedDispatcher.addCallback(callback)

        val checkboxTogglePref = requireActivity().getSharedPreferences("buttonToggle", 0)
        val checkboxToggleEditor = checkboxTogglePref.edit()


        lateinit var urlList: List<MoodleBasicUrl>

        binding.apply {
            s1UrlList.keyListener=null
            MainScope().launch{
                requireActivity().runOnUiThread {
                    customProgressDialog!!.start("Please wait URL is fetching...")
//                    showProgress(activity = requireActivity(), bool = true, parentLayout = parentLayout, loading = lottieAnimation)
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
                    s1UrlList.setAdapter(adapter)
                    s1UrlList.setText(urlStringArrayList[0],false)
//                    val savedURL = ModelRepository.getStoredMoodleUrl(requireActivity()).url

                    if (checkboxTogglePref.contains("url")) {
                        val setUrl = checkboxTogglePref.getString("url", null)
                        if (setUrl != null) {
                            s1UrlList.setText(setUrl,false)
                            customProgressDialog!!.stop()
//                            showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)
                        }
                        customProgressDialog!!.stop()

//                        showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)
                    }
                    customProgressDialog!!.stop()

//                    showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)
                    selectedURL = s1UrlList.text.toString()

                }

            }
            s1UrlList.setOnItemClickListener { parent, view, position, id ->
                selectedURL = parent.getItemAtPosition(position).toString()
            }
            saveBtn.setOnClickListener {
                MainScope().launch {
                    customProgressDialog!!.start("")
//                    showProgress(activity = requireActivity(), bool = true, parentLayout = parentLayout, loading = lottieAnimation)
                    if(!toggleUrlDialog.isChecked){
                        checkboxToggleEditor.putBoolean("buttonToggle", toggleUrlDialog.isChecked)
                        checkboxToggleEditor.apply()
                    }else {
                        checkboxToggleEditor.putBoolean("buttonToggle", toggleUrlDialog.isChecked)
                        checkboxToggleEditor.apply()
                    }

                    if(selectedURL != null)
                    {
                        for (i in urlList.indices)
                        {
                            if(urlList[i].url==selectedURL)
                            {
                                try
                                {
                                    ModelRepository.setMoodleUrlSetting(requireContext(), urlList[i])
                                    checkboxToggleEditor.putString("url", urlList[i].url)
                                    checkboxToggleEditor.apply()
                                    customProgressDialog!!.stop()
//                                    showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)
                                    findNavController().popBackStack()
                                }
                                catch (ex:Exception)
                                {
                                    Log.e(TAG,"$ex")
                                    snackbar("Error in set url try again")
                                }
                                break
                            }
                        }

                    }
                    else{
                        customProgressDialog!!.stop()
//                        showProgress(activity = requireActivity(), bool = false, parentLayout = parentLayout, loading = lottieAnimation)
                        snackbar("URL not selected")
                    }
                }

            }
        }
    }

}