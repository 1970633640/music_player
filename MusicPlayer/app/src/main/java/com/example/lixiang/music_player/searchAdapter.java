package com.example.lixiang.music_player;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiang on 2017/8/15.
 */

public class searchAdapter extends RecyclerView.Adapter<searchAdapter.ViewHolder> implements Filterable {
    private Context mContext;
    private MyFilter myFilter;
    private ArrayList<music_title> previous;
    private ArrayList<music_title> filtered;
    private ProgressDialog dialog;
    private View rootView;

public searchAdapter(){
    previous = new ArrayList<music_title>();
    int size = MyApplication.getMusicListNow().size();
    List<musicInfo> listNow = MyApplication.getMusicListNow();
    if (size != 0 ) {
        for (int i = 0; i < size; i++) {
            previous.add(new music_title(i, listNow.get(i).getMusicTitle()));
        }
    }
    filtered = previous;
    }

    @Override
    public searchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        rootView = parent;
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.search, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final int Position = filtered.get(position).getPosition();
        final musicInfo musicNow = MyApplication.getMusicListNow().get(position);
        holder.song.setText(musicNow.getMusicTitle());
        holder.singer.setText(musicNow.getMusicArtist());
        //设置歌曲封面
        if (musicNow.getMusicLink() != "") {//网络
            Glide
                    .with(mContext)
                    .load(musicNow.getMusicAlbum())
                    .placeholder(R.drawable.default_album)
                    .into(holder.cover);
        }else{
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, musicNow.getMusicAlbumId());
            Glide
                    .with(mContext)
                    .load(uri)
                    .placeholder(R.drawable.default_album)
                    .into(holder.cover);
        }
        //处理整个点击事件
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicNow.getMusicLink() != ""){//网络
                    dialog = ProgressDialog.show(mContext,"请稍后","正在玩命加载中");
                    dialog.setOnKeyListener(new Dialog.OnKeyListener() {

                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode,
                                             KeyEvent event) {
                            // TODO Auto-generated method stub
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                Intent intent = new Intent("service_broadcast");
                                intent.putExtra("ACTION", MyConstant.resetAction);
                                mContext.sendBroadcast(intent);
                                dialog.dismiss();
                            }
                            return true;
                        }
                    });
                }
                //播放
                MyApplication.setPositionNow(position);
                Intent intent = new Intent("service_broadcast");
                intent.putExtra("ACTION", MyConstant.playAction);
                mContext.sendBroadcast(intent);
            }
        });
        //处理菜单点击
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出菜单
                if(MyApplication.getMusicListNow().get(position).getMusicLink() != ""){
                    menu_util.popupNetMenu((Activity) mContext,view,Position);
                }else {
                    menu_util.popupMenu((Activity) mContext, view, Position,"");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    public Filter getFilter() {
        if (myFilter == null) {
            myFilter = new MyFilter();
        }
        return myFilter;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        ImageView button;
        TextView song;
        TextView singer;
        RelativeLayout relativeLayout;

        public ViewHolder(View view) {
            super(view);
            relativeLayout = (RelativeLayout) view;
            cover = (ImageView) view.findViewById(R.id.search_cover);
            button = (ImageView) view.findViewById(R.id.searchView_button);
            song = (TextView) view.findViewById(R.id.search_song);
            singer = (TextView) view.findViewById(R.id.search_singer);
        }
    }

    class MyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filtered =new ArrayList<music_title>();
            if (constraint != null && constraint.toString().trim().length() > 0) {
                List<musicInfo> listNow = MyApplication.getMusicListNow();
                for (int i = 0; i < previous.size(); i++) {
                    musicInfo musicNow = listNow.get(i);
                    String title = musicNow.getMusicTitle();
                    String singer = musicNow.getMusicArtist();
                    if (title.contains(constraint) || singer.contains(constraint)) {
                        filtered.add(new music_title(i,title));
                    }
                }

            }else {
                filtered=previous;
            }
            FilterResults filterResults = new FilterResults();
            filterResults.count = filtered.size();
            filterResults.values = filtered;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered = (ArrayList<music_title>) results.values;
                notifyDataSetChanged();
        }
    }

    public class music_title {
        public  int mPosition;
        public String mTitle;
        public music_title(int position,String title){
            mPosition = position;
            mTitle = title;
        }
        public int getPosition(){
            return mPosition;
        }
        public String getTitle(){
            return mTitle;
        }
    }
    public void dismissDialog(){
        if (dialog !=null) {
            dialog.dismiss();
            Snackbar.make(rootView,"在线播放仅作为预览，请在来源网站下载正版音乐后播放",Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {@Override public void onClick(View view) {}}).show();
        }
    }
}
