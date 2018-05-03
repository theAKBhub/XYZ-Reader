package com.example.xyzreader.ui.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ArticleLoader.Query;
import com.example.xyzreader.ui.fragments.ArticleDetailFragment;
import com.squareup.picasso.Picasso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ArticleDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleDetailActivity.class.getSimpleName();
    public static final String KEY_ITEM_POSITION = "item_position";

    final Context mContext = this;
    private DetailPagerAdapter mDetailPagerAdapter;
    private Cursor mCursor;
    private long mSelectedItemId;
    private long mStartId;
    private String mArticleTitle;
    private Date mArticleDate;
    private String mArticleAuthor;
    private String mArticleContent;
    private Animation mShowAnimation;

    private int mPosition;
    private int temp;
    private int start;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.appbar_layout)
    AppBarLayout mAppbarLayout;
    @BindView(R.id.imageview_photo)
    ImageView mImageViewPhoto;
    @BindView(R.id.toolbar_detail_activity)
    Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.textview_title)
    TextView mTextViewTitle;
    @BindView(R.id.textview_subtitle)
    TextView mTextViewSubtitle;
    @BindView(R.id.fab_share_article)
    FloatingActionButton mFabShareArticle;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH);
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);

        mPosition = temp = start = 0;

        setSupportActionBar(mToolbar);
        if (mToolbar != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mShowAnimation = AnimationUtils.loadAnimation(mContext, R.anim.poster_anim);

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                if (getIntent().hasExtra(KEY_ITEM_POSITION)) {
                    mPosition = temp = getIntent().getExtras().getInt(KEY_ITEM_POSITION, 0);
                    Log.d("XXX YYY", "mPosition rcvd = " + mPosition);
                }
            }
        }

        mDetailPagerAdapter = new DetailPagerAdapter(getSupportFragmentManager());

        //mViewPager.setAdapter(mDetailPagerAdapter);
        mViewPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));

        mViewPager.addOnPageChangeListener(new SimpleOnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                Log.d("XXX YYY", "position = " + position);

                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }

                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                mPosition = position;
                displayArticleData();
                mDetailPagerAdapter.getItem(mPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        mAppbarLayout.addOnOffsetChangedListener(new OnOffsetChangedListener() {
            boolean isShowTitle = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                scrollRange = (scrollRange == -1) ? appBarLayout.getTotalScrollRange() : scrollRange;

                if ((scrollRange + verticalOffset) == 0) {
                    mCollapsingToolbar.setTitle(mArticleTitle);
                    isShowTitle = true;
                } else if (isShowTitle) {
                    mCollapsingToolbar.setTitle(" ");
                    isShowTitle = false;
                }
            }

        });


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;

        displayArticleData();
        mViewPager.setAdapter(mDetailPagerAdapter);
        mDetailPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mViewPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        mDetailPagerAdapter.notifyDataSetChanged();
    }

    public void displayArticleData() {
        mCursor.moveToPosition(mPosition);
        Picasso.get()
                .load(mCursor.getString(Query.PHOTO_URL))
                .placeholder(R.drawable.photo_background_protection)
                .error(R.drawable.wallpaper)
                .into(mImageViewPhoto);

        mImageViewPhoto.startAnimation(mShowAnimation);

        mArticleTitle = mCursor.getString(Query.TITLE);
        mTextViewTitle.setText(mArticleTitle);

        String dateParsed = "";
        Date date = parsePublishedDate(mCursor.getString(Query.PUBLISHED_DATE));

        if (!date.before(START_OF_EPOCH.getTime())) {

            dateParsed = DateUtils.getRelativeTimeSpanString(
                    date.getTime(),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString();
        }

        mTextViewSubtitle.setText(mCursor.getString(Query.AUTHOR) + " / " + dateParsed);

        mArticleContent = mCursor.getString(Query.BODY); Log.d("XXX YYY", mArticleContent.substring(0, 50));

    }


    private class DetailPagerAdapter extends FragmentStatePagerAdapter {

        public DetailPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            //mCursor.moveToPosition(mPosition);
            //return ArticleDetailFragment.newInstance(mCursor.getString(Query.BODY));
            return ArticleDetailFragment.newInstance(mArticleContent);
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }

    private Date parsePublishedDate(String date) {
        try {
            //String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

}