package com.blis.customercity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

public class ConfirmationDialog {

    public static void showConfirmationDialog(Context context, String title, String message,
                                              DialogInterface.OnClickListener positiveClickListener,
                                              DialogInterface.OnClickListener negativeClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true); // Allows dismissing the dialog by tapping outside or pressing back

        builder.setPositiveButton("Confirm", positiveClickListener);

        builder.setNegativeButton("Cancel", negativeClickListener);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}