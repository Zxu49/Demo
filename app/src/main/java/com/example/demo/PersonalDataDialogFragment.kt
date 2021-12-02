import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.demo.R

class PersonalDataDialogFragment : DialogFragment() {
    interface LoginInputListener {
        fun onLoginInputComplete(input:String)
    }
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            var dialogView = inflater.inflate(R.layout.sign_personal_data_view, null)
            var personalInput = dialogView.findViewById<EditText>(R.id.personalDataInput)
            builder.setView(dialogView)
                .setPositiveButton("send",
                    DialogInterface.OnClickListener { _, _ ->
                        var input = personalInput.text.toString()
                        (activity as LoginInputListener).onLoginInputComplete(input)
                    })
                .setNegativeButton("cancel",
                    DialogInterface.OnClickListener { _, _ ->
                        dialog?.cancel()
                    }
                )
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}