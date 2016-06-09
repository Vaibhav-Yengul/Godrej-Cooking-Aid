package godrejapp.vaibhav.com.godrejcookingaid;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 5/27/2016.
 */
public class MyListAdapter extends BaseAdapter {

    private Activity mContext;
    private List<String> mList;
    private LayoutInflater mLayoutInflater = null;

    public MyListAdapter(Activity context, List<String> list) {
        mContext = context;
        mList = list;
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        CompleteListViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.mainactivity_listview, null);
            viewHolder = new CompleteListViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (CompleteListViewHolder) v.getTag();
        }
        String temp = mList.get(position);
        List<String> t = new ArrayList<String>(Arrays.asList(temp.toString().split(" ")));
        try {
            viewHolder.mTVId.setText(t.get(0));
            String title = "";
            for(int i = 1; i < t.size() ;i++){
                title += t.get(i) + " " ;
            }
            viewHolder.mTVTitle.setText(title);
        }
        catch (Exception e){
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            viewHolder.mTVId.setText(t.get(0));
        }
        return v;
    }

    public void swapItems(List<String> items) {
        this.mList = items;
        notifyDataSetChanged();
    }

    class CompleteListViewHolder {
        public TextView mTVId,mTVTitle;
        public CompleteListViewHolder(View base) {
            mTVId = (TextView) base.findViewById(R.id.listTV);
            mTVTitle = (TextView) base.findViewById(R.id.listTitle);
        }
    }
}
