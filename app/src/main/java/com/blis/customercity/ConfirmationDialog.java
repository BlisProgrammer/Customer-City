package com.blis.customercity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

public class ConfirmationDialog {

    /**
     * Create a confirmation dialog and show on the screen.
     * @param context Context of application: {@code requireContext()}
     * @param title Title of dialog
     * @param message Message of dialog
     * @param positiveClickListener action when user presses 確認
     * @param negativeClickListener action when user presses 取消
     */
    public static void showConfirmationDialog(Context context, String title, String message,
                                              DialogInterface.OnClickListener positiveClickListener,
                                              DialogInterface.OnClickListener negativeClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton("確認", positiveClickListener);

        builder.setNegativeButton("取消", negativeClickListener);

        AlertDialog dialog = builder.create();
        dialog.show();
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTypeface(null, Typeface.BOLD);
        }
    }
}