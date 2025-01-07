package com.example.snaplapse.settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.snaplapse.R
import com.example.snaplapse.login.LoginActivity

class LogOutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(context)
            val sharedPref = activity?.getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
            builder.setTitle(R.string.log_out_dialog_title)
            builder
                .setPositiveButton(R.string.log_out_dialog_confirm, DialogInterface.OnClickListener { dialog, id ->
                    with(sharedPref?.edit()) {
                        this?.remove("session")
                        this?.remove("id")
                        this?.remove("joined")
                        this?.apply()
                    }
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                })
                .setNegativeButton(R.string.log_out_dialog_cancel, DialogInterface.OnClickListener { dialog, id ->

                })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
