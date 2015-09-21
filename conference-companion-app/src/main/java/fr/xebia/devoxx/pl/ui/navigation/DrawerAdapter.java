package fr.xebia.devoxx.pl.ui.navigation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.xebia.devoxx.pl.R;

public class DrawerAdapter extends BaseAdapter {

    public static final int MENU_MY_AGENDA = 0;
    public static final int MENU_TALKS = 1;
    public static final int MENU_SPEAKERS = 2;
    public static final int MENU_TIMELINE = 3;
    public static final int MENU_SETTINGS = 4;
    public static final int MENU_INVALID = -1;
    private final int mSelectedPosition;

    private LayoutInflater mInflater;

    private List<DrawerItem> mItems;

    public DrawerAdapter(Context context) {
        mInflater = LayoutInflater.from(context);

        mSelectedPosition = 0;
        mItems = new ArrayList<>();
        mItems.add(new DrawerItem(R.drawable.ic_schedule, R.string.schedule));
        mItems.add(new DrawerItem(R.drawable.ic_talk, R.string.talks));
        mItems.add(new DrawerItem(R.drawable.ic_speaker, R.string.speakers));
        mItems.add(new DrawerItem(R.drawable.ic_twitter, R.string.timeline));
        mItems.add(new DrawerItem(R.drawable.ic_drawer_settings, R.string.settings));
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public DrawerItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = mInflater.inflate(R.layout.drawer_item, parent, false);
            holder = new ViewHolder(view);
            if (view != null) {
                view.setTag(holder);
            }
        }

        DrawerItem item = mItems.get(position);

        Drawable icon = view.getContext().getResources().getDrawable(item.getIconRes()).mutate();
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        holder.mText.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        holder.mText.setText(item.getTextRes());
        holder.mText.setSelected(position == mSelectedPosition);
        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.drawer_item_text) TextView mText;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
