/*
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.basilwang;

import static net.basilwang.dao.Preferences.LOGON_ACCOUNT_ID;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import net.basilwang.core.CurriculumPreferenceTask;
import net.basilwang.core.LoadCheckCodeTask;
import net.basilwang.core.LogOnPreferenceTask;
import net.basilwang.core.ScorePreferenceTask;
import net.basilwang.core.TAHelper;
import net.basilwang.dao.AccountService;
import net.basilwang.entity.Account;
import net.basilwang.utils.NetworkUtils;
import android.app.ProgressDialog;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class CheckCodeDialog extends SherlockActivity {
	static final String TAG = "CheckCodeDialog";
	TAHelper taHelper;
	LoadCheckCodeTask loadCheckCodeTask;
	ProgressDialog progressBar;
	HttpClient httpclient;
    public ProgressDialog getProgressBar() {
		return progressBar;
	}
	public void init() {
		setContentView(R.layout.checkcode);
		httpclient=new DefaultHttpClient();
		// 2012-10-25 basilwang make dialog full window
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		Window window = getWindow();
		lp.copyFrom(window.getAttributes());
		// This makes the dialog take up the full width
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		window.setAttributes(lp);
		getWindow().setGravity(Gravity.TOP);
		setTitle("请输入验证码");
		try {
		  taHelper = TAHelper.Instance();

		reloadCheckCode();

	    } catch (NotFoundException e) {
		  
		  e.printStackTrace();
	    }
	}
	@Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		final String taskName = this.getIntent().getExtras().getString("task");
		final EditText txtCheckCode = (EditText) findViewById(R.id.checkCode);
		final Button btnSave = (Button) findViewById(R.id.btnSave);
		final Button btnCancel = (Button) findViewById(R.id.btnCancel);
		final ImageView checkCodeImage = (ImageView) findViewById(R.id.score_checkcode_image);
 
		checkCodeImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				reloadCheckCode();
			}

		});
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String checkCode = txtCheckCode.getText().toString();
				int accountId = PreferenceManager.getDefaultSharedPreferences(
						v.getContext()).getInt(LOGON_ACCOUNT_ID, 0);
				Account account = new AccountService(v.getContext())
						.getAccountById(accountId);
				if (checkCode.equals("")) {
					Animation shake = AnimationUtils.loadAnimation(
							v.getContext(), R.anim.shake);
					txtCheckCode.startAnimation(shake);
				} else {
					btnSave.setClickable(false);
					AsyncTask<Object, Integer, String> stask = null;
					initsTask();
					if (taskName.equals("curriculum")) {
						stask = new CurriculumPreferenceTask(v.getContext(),httpclient);
					} else if (taskName.equals("score")) {
//						stask = new ScorePreferenceTask(v.getContext());
					} else {
						return;
					}
					
					progressBar = new ProgressDialog(v.getContext());
					progressBar.setCancelable(true);
					progressBar.setMessage("下载中......");
					progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressBar.setProgress(0);
					progressBar.setMax(100);
					progressBar.show();
					
					LogOnPreferenceTask task = new LogOnPreferenceTask(v
							.getContext(),stask,httpclient);
					task.execute(account.getUserno(), account.getPassword(),
							checkCode);
				}
			}

			private void initsTask() {
				// TODO Auto-generated method stub
				
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckCodeDialog.this.finish();
			}
		});
	}
	private void reloadCheckCode() {
	if (!NetworkUtils.isConnect(this)) {
		Toast.makeText(CheckCodeDialog.this,
				this.getResources().getString(R.string.nonetwork_toast),
				Toast.LENGTH_SHORT).show();
		return;
	}
	if (loadCheckCodeTask != null) {
		AsyncTask.Status diStatus = loadCheckCodeTask.getStatus();
		if (diStatus != AsyncTask.Status.FINISHED) {
			return;
		}
	}

	loadCheckCodeTask = new LoadCheckCodeTask(this,httpclient);
	loadCheckCodeTask.execute();
}
}
