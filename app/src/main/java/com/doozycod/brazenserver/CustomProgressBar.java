package com.doozycod.brazenserver;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ProgressBar;

import com.doozycod.brazenwatch.R;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;


public class CustomProgressBar {
	public Dialog popDialog;
	private Context context;
	ProgressBar progressBar;
public CustomProgressBar(Context context) {

		this.context = context;

	}

	/*
	 * This method display a message or alert for any functionality
	 */
//	show progress bar method
	public void showProgress() {
		popDialog = new Dialog(context);
		popDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		popDialog.setContentView(R.layout.progress_abr);
		popDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		popDialog.setCancelable(false);
		progressBar = (ProgressBar)popDialog.findViewById(R.id.spin_kit);
		Sprite doubleBounce = new DoubleBounce();
		progressBar.setIndeterminateDrawable(doubleBounce);
		popDialog.show();
	}

//	Hide progress bar method
	public void hideProgress() {

		if (popDialog != null) {
			popDialog.dismiss();

		}
	}

}
