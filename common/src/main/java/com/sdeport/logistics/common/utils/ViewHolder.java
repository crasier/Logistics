package com.sdeport.logistics.common.utils;

import android.util.SparseArray;
import android.view.View;

@SuppressWarnings("unchecked")
public class ViewHolder {
    public static <T extends View> T get (View convertView, int resId) {
        SparseArray<View> viewHolder = (SparseArray<View>) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<>();
            convertView.setTag(viewHolder);
        }
        View view = viewHolder.get(resId);
        if (view == null) {
            view = convertView.findViewById(resId);
            viewHolder.put(resId, view);
        }

        return (T) view;
    }
}
