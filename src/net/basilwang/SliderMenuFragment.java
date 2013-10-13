package net.basilwang;

import net.basilwang.utils.NetworkUtils;
import net.upol.CurriculumFragment;
import net.upol.MessageFragment;
import net.upol.MyScoreFragement;
import net.upol.TeachPlanFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.SubMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class SliderMenuFragment extends ListFragment {

	public static final int EXIT_APPLICATION = 0x0001;
	private SlidingMenu menu;
	SubMenu subMenuForNetwork;

	public SliderMenuFragment(SlidingMenu menu) {
		this.menu = menu;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] menuNames = getResources().getStringArray(R.array.menu_name);
		SampleAdapter adapter = new SampleAdapter(getActivity());
		for (int i = 0; i < menuNames.length; i++) {
			adapter.add(new SampleItem(menuNames[i], getIconResc(i)));
			setListAdapter(adapter);
		}
	}

	private int getIconResc(int position) {
		int[] iconResc = { R.drawable.admin, R.drawable.message,
				R.drawable.grade, R.drawable.curriculmn, R.drawable.set,
				R.drawable.message, R.drawable.shared, R.drawable.exit };
		return iconResc[position];

	}

	private class SampleItem {
		public String tag;
		public int iconRes;

		public SampleItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView
					.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView
					.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return convertView;
		}

	}

	// @曹洪�自己改写的，用于返回是否联网
	private boolean isNetAvailable() {
		return NetworkUtils.isConnect(this.getActivity()) ? true : false;
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		if (isNetAvailable()) {
			Fragment newContent = null;
			StringBuffer url = new StringBuffer(
					"http://xueli.upol.cn/M4/upol/platform/");
			switch (position) {
			case 1:
				url.append("zxgg/zxgg_01.jsp?pageInt=");
				newContent = new MessageFragment(url);
				break;
			case 2:
				newContent = new MyScoreFragement();
				break;
			case 3:
				newContent = new CurriculumFragment();
				break;
			case 4:
				newContent = new TeachPlanFragment();
				break;
			case 5:
				newContent = new DownloadCurriculumFragment();
				break;
			case 6:
				url.append("ksap/ksap_01.jsp?pageInt=");
				newContent = new MessageFragment(url);
				break;
			case 7:
				exit();
				break;
			}
			if (newContent != null)
				switchFragment(newContent);
		} else
			Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();

	}

	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;
		if (getActivity() instanceof StaticAttachmentActivity) {
			StaticAttachmentActivity fca = (StaticAttachmentActivity) getActivity();
			fca.switchContent(fragment);
		}
	}

	public void exit() {
		Intent mIntent = new Intent();
		mIntent.setClass(this.getActivity(), StaticAttachmentActivity.class);
		// 这里设置flag还是比较重要�
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// 发出退出程序指�
		mIntent.putExtra("flag", EXIT_APPLICATION);
		startActivity(mIntent);
	}

}
