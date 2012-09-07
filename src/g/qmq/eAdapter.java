package g.qmq;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class eAdapter extends BaseAdapter {

	// API LIST14
	private LayoutInflater mInflater;

	public eAdapter(Context context) {
		mContext = context;
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
			holder.tv_score = (TextView) convertView
					.findViewById(R.id.ls_score);
			holder.tv_player = (TextView) convertView
					.findViewById(R.id.ls_player);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		
		//Set fonts.
		try {
			Typeface tf = Typeface.createFromAsset(mContext.getAssets(),
					"fonts/oldengl.ttf");
			holder.tv_player.setTypeface(tf);
			holder.tv_rank.setTypeface(tf);
			holder.tv_score.setTypeface(tf);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("FONTS", "FONTS UNFOUND (CONTEXT ERROR?)");
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
			break;
		case 1:
			holder.tv_rank.setTextColor(Color.BLUE);
			break;
		case 2:
			holder.tv_rank.setTextColor(Color.GREEN);
			break;
		default:
			holder.tv_rank.setTextColor(Color.WHITE);
			break;
		}
		// Preset value for keyfield not found.
		// holder.tv_score.setText(DATA.get(position).get(4)[2]);
		for (int i = 1, j = DATA.get(position).size(); i < j; i++) {
			// Search for key field from 1 to size.
			if (Integer.valueOf(DATA.get(position).get(i)[0]) > 0) {
				String[] keyField = DATA.get(position).get(i);
				holder.tv_score.setText(keyField[2] + " " + keyField[3]);
				break;
			}
		}
		holder.tv_player.setText(DATA.get(position).get(0)[1]);

		return convertView;
	}

	class ViewHolder {
		TextView tv_rank, tv_score, tv_player;
	}

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

	public void addData(ArrayList<ArrayList<String[]>> data) {
		DATA = data;
	}

	private ArrayList<ArrayList<String[]>> DATA = new ArrayList<ArrayList<String[]>>();
	private int myRank = -1;
	private Context mContext;

}
