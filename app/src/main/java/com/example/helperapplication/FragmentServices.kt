package com.example.helperapplication

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.helperapplication.services.ConverterHelperService
import com.example.helperapplication.services.SystemServiceHelper
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType


class FragmentServices : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val serviceSpinner: Spinner = view.findViewById(R.id.serviceSpinner)
        val methodContainer: LinearLayout = view.findViewById(R.id.methodsContainer)
        val resultContainer: LinearLayout = view.findViewById(R.id.resultContainer)
        val scrollView: ScrollView = view.findViewById(R.id.scrollView)

        val services = SystemServiceHelper.getAvailableSystemService()
        val serviceNames = services.map { it }
        initServiceSpinner(serviceSpinner, serviceNames)

        serviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedServiceName = serviceNames[position]
                val service = requireContext().getSystemService(selectedServiceName)
                if (service != null) {
                    val methods = SystemServiceHelper.getServiceMethods(requireContext(), selectedServiceName)
                    populateMethods(service, methods, methodContainer, resultContainer, scrollView, selectedServiceName)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    @SuppressLint("ResourceType")
    private fun initServiceSpinner(serviceSpinner: Spinner, serviceNames: List<String>) {
        val serviceAdapter = ArrayAdapter(requireContext(), R.layout.selected_item_spinner, serviceNames)
        serviceAdapter.setDropDownViewResource(R.layout.item_spinner)
        serviceSpinner.adapter = serviceAdapter
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun populateMethods(
        service: Any, methods: List<String>, methodContainer: LinearLayout,
        resultContainer: LinearLayout, scrollView: ScrollView, selectedServiceName: String
    ) {
        methodContainer.removeAllViews()
        methods.forEach { methodName ->
            val methodLayout = createMethodLayout(service, methodName, resultContainer, scrollView, selectedServiceName)
            methodContainer.addView(methodLayout)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createMethodLayout(
        service: Any, methodName: String, resultContainer: LinearLayout,
        scrollView: ScrollView, selectedServiceName: String
    ): LinearLayout {
        val methodLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8, 0, 8)
        }

        val rowLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 4, 16, 4)
            setBackgroundColor(Color.parseColor("#EAEEEA"))
        }

        val methodTextView = TextView(requireContext()).apply {
            text = methodName
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }

        val method = service.javaClass.methods.firstOrNull { it.name == methodName }
        val paramValues = mutableListOf<EditText>()

        rowLayout.addView(methodTextView)

        method?.parameters?.forEach { parameter ->
            val editText = createParameterInput(parameter)
            rowLayout.addView(editText)
            paramValues.add(editText)
        }

        val callButton = createCallButton(methodName, paramValues, service, method, resultContainer, scrollView, selectedServiceName)
        rowLayout.addView(callButton)

        methodLayout.addView(rowLayout)
        return methodLayout
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createParameterInput(parameter: Parameter): EditText {
        return EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textSize = 20f
            gravity = Gravity.START
            isFocusable = true
            isFocusableInTouchMode = true

            val type = parameter.parameterizedType
            hint = when {
                List::class.java.isAssignableFrom(parameter.type) -> {
                    val genericType = (type as? ParameterizedType)?.actualTypeArguments?.firstOrNull()
                    "${parameter.name}: List<${genericType?.typeName ?: "Unknown"}>"
                }
                Set::class.java.isAssignableFrom(parameter.type) -> {
                    val genericType = (type as? ParameterizedType)?.actualTypeArguments?.firstOrNull()
                    "${parameter.name}: Set<${genericType?.typeName ?: "Unknown"}>"
                }
                Map::class.java.isAssignableFrom(parameter.type) -> {
                    val keyType = (type as? ParameterizedType)?.actualTypeArguments?.getOrNull(0)
                    val valueType = (type as? ParameterizedType)?.actualTypeArguments?.getOrNull(1)
                    "${parameter.name}: Map<${keyType?.typeName ?: "Unknown"}, ${valueType?.typeName ?: "Unknown"}>"
                }
                parameter.type.isArray -> {
                    "${parameter.name}: Array<${parameter.type.componentType.simpleName}>"
                }
                else -> {
                    "${parameter.name}: ${parameter.type.simpleName}"
                }
            }
        }
    }

    private fun createCallButton(
        methodName: String, paramValues: List<EditText>, service: Any,
        method: Method?, resultContainer: LinearLayout, scrollView: ScrollView,
        selectedServiceName: String
    ): Button {
        return Button(requireContext()).apply {
            text = "\u25B6"
            textSize = 20f
            setTextColor((Color.parseColor("#2C872C")))
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(20, 0, 0, 0)
            }
            setBackgroundColor(Color.parseColor("#EDF3ED"))
            setOnClickListener {
                try {
                    val params = paramValues.mapIndexed { index, editText ->
                        ConverterHelperService.convertParameter(method!!.parameters[index], editText.text.toString())
                    }.toTypedArray()

                    val result = method?.invoke(service, *params)
                    displayResult(resultContainer, scrollView, selectedServiceName.uppercase(), methodName, params.joinToString(", "), result)
                } catch (e: Exception) {
                    displayResult(resultContainer, scrollView, selectedServiceName.uppercase(), methodName, "", "Error: ${e.message}")
                }
            }
        }
    }

    private fun displayResult(
        container: LinearLayout, scrollView: ScrollView, serviceName: String,
        methodName: String, params: String, result: Any?
    ) {
        val groupLayout = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        }

        val lineView = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5
            )
            setBackgroundColor(Color.parseColor("#D9D9D9"))
        }
        val textView = TextView(requireContext()).apply {
            text = "[$serviceName].$methodName($params):\n${ConverterHelperService.formatResult(result)}\n"
            textSize = 20f
            setTextColor(Color.BLACK)
            setPadding(20, 15, 20, 15)
        }
        groupLayout.addView(textView)
        groupLayout.addView(lineView)
        container.addView(groupLayout, 0)

        val maxResults = 50
        if (container.childCount > maxResults) {
            container.removeViewAt(container.childCount - 1)
        }

        scrollView.post {
            scrollView.smoothScrollTo(0, 0)
        }
    }
}