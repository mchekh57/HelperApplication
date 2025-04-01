package com.example.helperapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.helperapplication.services.PropertyHelperService
import com.example.helperapplication.services.SettingHelperService
import java.util.TreeMap

class FragmentSettings : Fragment() {
    private val fullResult: LinkedHashMap<String, TreeMap<String, String>> = LinkedHashMap()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resultTextView: TextView = view.findViewById(R.id.resultTextView)
        val filterButton: Button = view.findViewById(R.id.filterButton)
        val filterEditText: EditText = view.findViewById(R.id.filterEditText)
        val leftButtonSystem: Button = view.findViewById(R.id.leftButtonSystem)
        val leftButtonGlobal: Button = view.findViewById(R.id.leftButtonGlobal)
        val leftButtonSecure: Button = view.findViewById(R.id.leftButtonSecure)
        val leftButtonProperty: Button = view.findViewById(R.id.leftButtonProperty)

        fun formatMapOutput(map: TreeMap<String, String>): String {
            return map.entries.joinToString("\n") { (key, value) -> "$key: $value" }
        }

        fun updateResultView() {
            val formattedResult = fullResult.entries.reversed().joinToString("\n\n") { (category, map) ->
                "$category:\n${formatMapOutput(map)}"
            }
            resultTextView.text = formattedResult
        }
        fun addCategoryAndUpdate(category: String, data: TreeMap<String, String>) {
            if (fullResult.containsKey(category)) {
                fullResult.remove(category)
            }
            fullResult[category] = data
            updateResultView()
        }

        filterButton.setOnClickListener {
            val filterKey = filterEditText.text.toString().trim()

            if (filterKey.isEmpty()) {
                updateResultView()
            } else {
                val filteredResults = fullResult.entries.reversed().mapNotNull { (category, map) ->
                    val filteredMap = map.filterKeys { it.contains(filterKey, ignoreCase = true) }
                    if (filteredMap.isNotEmpty()) "$category:\n${formatMapOutput(TreeMap(filteredMap))}" else null
                }
                resultTextView.text = filteredResults.joinToString("\n\n")
            }
        }

        leftButtonSystem.setOnClickListener {
            val systemSettings = SettingHelperService.getSystemSettings(requireContext())
            addCategoryAndUpdate("SYSTEM", systemSettings)
        }

        leftButtonGlobal.setOnClickListener {
            val globalSettings = SettingHelperService.getGlobalSettings(requireContext())
            addCategoryAndUpdate("GLOBAL", globalSettings)
        }

        leftButtonSecure.setOnClickListener {
            val secureSettings = SettingHelperService.getSecureSettings(requireContext())
            addCategoryAndUpdate("SECURE", secureSettings)
        }

        leftButtonProperty.setOnClickListener{
            addCategoryAndUpdate("PROPERTY", PropertyHelperService.getProperties())
        }
    }
}
