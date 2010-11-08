package g.qmq;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class eAdapter extends BaseAdapter {

	// API LIST14
	private LayoutInflater mInflater;

	public eAdapter(Context context) {
		// Cache the LayoutInflate to avoid asking for a new one each time.
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return DATA.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid
		// unneccessary calls
		// to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is
		// no need
		// to reinflate it. We only inflate a new View when the convertView
		// supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_score, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.tv_rank = (TextView) convertView.findViewById(R.id.ls_rank);
			holder.tv_time = (TextView) convertView.findViewById(R.id.ls_time);
			holder.tv_date = (TextView) convertView.findViewById(R.id.ls_date);

			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		holder.tv_rank.setText(String.valueOf(position + 1)
				+ suffix(position + 1));
		if (position == myRank - 1) {
			holder.tv_rank.setBackgroundColor(Color.BLUE);
		} else {
			holder.tv_rank.setBackgroundColor(Color.TRANSPARENT);
		}
		switch (position) {
		case 0:
			holder.tv_rank.setTextColor(Color.RED);
			holder.tv_rank.setTextSize(20f);

			break;
		case 1:
			holder.tv_rank.setTextColor(Color.BLUE);
			holder.tv_rank.setTextSize(19f);
			break;
		case 2:
			holder.tv_rank.setTextColor(Color.GREEN);
			holder.tv_rank.setTextSize(18f);
			break;
		default:
			holder.tv_rank.setTextColor(Color.WHITE);
			holder.tv_rank.setTextSize(16f);
			break;
		}
		holder.tv_time.setText(DATA.get(position).get(1)[1]);
		holder.tv_date.setText(DATA.get(position).get(2)[1]);

		return convertView;
	}

	class ViewHolder {
		TextView tv_rank, tv_time, tv_date;
	}

	/**
	 * 生成数字后缀.
	 * 
	 * @param num int 任何整数
	 * @return String 后缀"st","nd","rd","th".
	 */
	private String suffix(int num) {
		// Handle special case 11,12,13
		if (num == 11 || num == 12 || num == 13)
			return "th";
		switch (num % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	private void addData(ArrayList<ArrayList<String[]>> data) {
		DATA.addAll(data);
	}

	private ArrayList<ArrayList<String[]>> DATA = new ArrayList<ArrayList<String[]>>();
	private int myRank = -1;

}
