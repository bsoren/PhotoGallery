package com.bijay.android.photogallery;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bsoren on 06-Jun-16.
 */
public class PhotoGalleryFragment extends VisibleFragment{

    private static final String TAG = PhotoGalleryFragment.class.getSimpleName();
    public static final String IMAGE_DRAWABLE = "sent_zoomed_image" ;

    private List<GalleryItem> mItems;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    public static Fragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mItems = new ArrayList<>();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_view, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG,"onQueryTextSubmit : "+query);
                QueryPreferences.setStoredQuery(getActivity(),query);
                emptyTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                updateItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG,"onQueryTextChange "+newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query,false);
            }
        });

        MenuItem toggleItem =  menu.findItem(R.id.menu_item_toggle_polling);
        boolean isAlarmServiceOn = PollService.isServiceAlarmOn(getActivity());
        Log.d(TAG,"isAlarmServiceOn : "+isAlarmServiceOn);
        if(isAlarmServiceOn){
            toggleItem.setTitle(R.string.stop_polliing);
        }else{
            toggleItem.setTitle(R.string.start_polling);
        }
    }


    private void updateItems(String query){

        if(query == null){
            query =  QueryPreferences.getStoredQuery(getContext());
            Log.d(TAG,"Stored Search Query : "+query);
        }
        new FetchItemsTask(query).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_item_clear :
                QueryPreferences.setStoredQuery(getActivity(),null);
                updateItems(null);
                return true;
            case R.id.menu_item_toggle_polling :
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                Log.d(TAG,"shouldStartAlarm : "+shouldStartAlarm);
                PollService.setServiceAlarm(getActivity(),shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        //recyclerView.setVisibility(View.GONE);
        progressBar = (ProgressBar) view.findViewById(R.id.fragment_photo_gallery_progress_bar);
       // progressBar.setVisibility(View.VISIBLE);
        emptyTextView = (TextView)view.findViewById(R.id.empty_recycler_view_text_view);
       // emptyTextView.setVisibility(View.GONE);

        updateItems(null);
        //setupAdapter();
        return view;
    }

    private void setupAdapter() {
        progressBar.setVisibility(View.GONE);
        if(mItems.size() == 0){
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        if(isAdded()){
            recyclerView.setAdapter(new PhotoAdapter(mItems));
        }


    }


    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>>{

        String mQuery;

        public FetchItemsTask(String query) {
            mQuery =  query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            Log.i(TAG,"Sending Request to Flickr ");
           // String query = "robot";

            if(mQuery == null){
                return new FlickrFetchr().fetchRecentPhotos();
            }else{
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
            Log.d(TAG,"onPostExecute mItems Size : "+mItems.size());
            setupAdapter();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{

        private ImageView mImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView =  (ImageView)itemView.findViewById(R.id.gallery_item_image_view);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"Image Clicked");
                    Intent intent = new Intent(getActivity(),ZoomedImageActivity.class);
                    intent.putExtra(IMAGE_DRAWABLE,mGalleryItem.getUrl());
                    startSharedElementTransition(getActivity(),intent,mImageView);

//                    final Dialog nagDialog = new Dialog(getActivity(),android.R.style.Theme_Translucent);
//                    nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                    nagDialog.setCancelable(true);
//                    nagDialog.setContentView(R.layout.zoomed_image);
//                    //Button btnClose = (Button)nagDialog.findViewById(R.id.btnIvClose);
//                    ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.zoomed_image);
//
//                    Picasso.with(getActivity())
//                            .load(mGalleryItem.getUrl())
//                            .error(R.drawable.no_image)
//                            .placeholder(R.drawable.image_loading_animation)
//                            .into(ivPreview);

//                    btnClose.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View arg0) {
//
//                            nagDialog.dismiss();
//                        }
//                    });
//                    nagDialog.show();
                }
            });

            mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Uri webPageUri =  mGalleryItem.getPhotoPageUri();
                    Intent browserIntent = PhotoPageActivity.newIntent(getActivity(),webPageUri);
                    startActivity(browserIntent);
                    return true;
                }
            });
        }

        public void bindPhoto(GalleryItem photo){
            mGalleryItem = photo;
            Picasso.with(getActivity())
                    .load(photo.getUrl())
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.image_loading_animation)
                    .into(mImageView);
        }


    }

    //Empty View
    private class EmptyViewHolder extends RecyclerView.ViewHolder{

        public EmptyViewHolder(View itemView) {
            super(itemView);
        }

    }

    private void startSharedElementTransition(Activity activity, Intent intent,
                                              View sourceView){

        ViewCompat.setTransitionName(sourceView,"zoomed");

        ActivityOptionsCompat options =  ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity,sourceView,"zoomed");

        activity.startActivity(intent, options.toBundle());
    }

    private class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<GalleryItem> mItems;

        public PhotoAdapter(List<GalleryItem> items){
            mItems =  items;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == EMPTY_VIEW) {
                View v = LayoutInflater.from(getActivity())
                        .inflate(R.layout.empty_view, parent,false);
                return new EmptyViewHolder(v);
            }
            View view = LayoutInflater.from(getActivity())
                    .inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if(holder instanceof PhotoHolder){
                GalleryItem item =  mItems.get(position);
                ((PhotoHolder)holder).bindPhoto(item);
            }

        }

        @Override
        public int getItemCount() {
            return mItems.size() > 0? mItems.size():1;
        }

        @Override
        public int getItemViewType(int position) {
            if(mItems.size()== 0){
                return EMPTY_VIEW;
            }
            return super.getItemViewType(position);
        }

        private static final int EMPTY_VIEW = 10;
    }
}
